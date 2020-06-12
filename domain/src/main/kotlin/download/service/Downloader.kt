/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import okio.BufferedSource
import okio.buffer
import tachiyomi.core.http.awaitSuccess
import tachiyomi.core.http.saveTo
import tachiyomi.core.io.DataUriStringSource
import tachiyomi.core.io.saveTo
import tachiyomi.core.util.ImageUtil
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.download.model.QueuedDownload
import tachiyomi.source.HttpSource
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.ImageBase64
import tachiyomi.source.model.ImageUrl
import tachiyomi.source.model.Page
import tachiyomi.source.model.PageComplete
import tachiyomi.source.model.PageListEmpty
import tachiyomi.source.model.PageUrl
import tachiyomi.source.model.Text
import java.io.File
import javax.inject.Inject

internal open class Downloader @Inject constructor(
  private val directoryProvider: DownloadDirectoryProvider,
  private val getLocalCatalog: GetLocalCatalog
) {

  protected open val retryDelay = 1000L

  fun worker(
    scope: CoroutineScope,
    downloads: ReceiveChannel<QueuedDownload>,
    downloadResult: SendChannel<Result>
  ) = scope.launch {
    for (download in downloads) {
      val tmpChapterDir = directoryProvider.getTempChapterDir(download)

      val result = try {
        downloadChapter(download, tmpChapterDir)
        Result.Success(download, tmpChapterDir)
      } catch (e: Throwable) {
        Result.Failure(download, e)
      }

      downloadResult.send(result)
    }
  }

  private suspend fun downloadChapter(download: QueuedDownload, tmpChapterDir: File) {
    val catalog = getLocalCatalog.get(download.sourceId)
    checkNotNull(catalog) { "Catalog not found" }

    val source = catalog.source
    require(source is HttpSource) { "Source must be an HttpSource for downloading" }

    // TODO
    val chapter = ChapterInfo("", "")

    val pages = (download.pages as? MutableList)
      ?: flow { emit(source.getPageList(chapter)) }
        .retry(1) { delay(retryDelay); true }
        .single()
        .toMutableList()
        .also { download.pages = it }

    if (pages.isEmpty()) {
      throw PageListEmpty()
    }

    // List of downloaded files when the download starts
    val downloadedFilesWithoutExtensionOnStart = if (tmpChapterDir.exists()) {
      val allFiles = tmpChapterDir.listFiles().orEmpty().asSequence()
      val tmpFilesFilter: (File) -> Boolean = { it.name.endsWith(".tmp") }

      // Delete all temporary (unfinished) files
      allFiles
        .filter(tmpFilesFilter)
        .forEach { it.delete() }

      allFiles
        .filterNot(tmpFilesFilter)
        .map { it.nameWithoutExtension }
    } else {
      tmpChapterDir.mkdirs()
      emptySequence<File>()
    }.toSet()

    var anyError = false

    flowOf(*pages.toTypedArray())
      .withIndex()
      .flatMapConcat { (index, page) ->
        // Check if page was downloaded
        val nameWithoutExtension = String.format("%03d", index + 1)
        if (nameWithoutExtension in downloadedFilesWithoutExtensionOnStart) {
          return@flatMapConcat emptyFlow<Page>()
        }

        val tmpFile = tmpChapterDir.resolve("$nameWithoutExtension.tmp")

        // Retrieve complete page if needed
        val completePageFlow = when (page) {
          is PageUrl -> {
            flow { emit(source.getPage(page)) }
              .retry(1) { delay(retryDelay); true }
              .onEach { pages[index] = it }
              .catch {
                anyError = true
                emptyFlow<PageComplete>()
              }
          }
          is PageComplete -> flowOf(page)
        }

        // Download page
        completePageFlow.flatMapConcat { cpage ->
          flowOf(Unit)
            .onEach {
              when (cpage) {
                is ImageUrl -> {
                  val response = source.client.newCall(source.getImageRequest(cpage)).awaitSuccess()
                  val body = checkNotNull(response.body)
                  val finalFile = getImageFile(tmpFile, body.source())
                  response.saveTo(tmpFile)
                  tmpFile.renameTo(finalFile)
                }
                is ImageBase64 -> {
                  val dataSource = DataUriStringSource(cpage.data).buffer()
                  val finalFile = getImageFile(tmpFile, dataSource)
                  dataSource.saveTo(tmpFile)
                  tmpFile.renameTo(finalFile)
                }
                is Text -> {
                  val finalFile = tmpFile.resolveSibling("$nameWithoutExtension.txt")
                  tmpFile.writeText(cpage.text)
                  tmpFile.renameTo(finalFile)
                }
              }
            }
            .retry(2) { delay(retryDelay); true }
            .catch {
              anyError = true
              emptyFlow<Unit>()
            }
        }
      }
      .collect()

    if (anyError) {
      throw Exception("Incomplete download")
    }
  }

  private fun getImageFile(tmpFile: File, source: BufferedSource): File {
    val magic = source.peek().readByteArray(8)
    val imageType = ImageUtil.findType(magic)
    return if (imageType != null) {
      tmpFile.resolveSibling(tmpFile.nameWithoutExtension + ".${imageType.extension}")
    } else {
      tmpFile.resolveSibling(tmpFile.nameWithoutExtension)
    }
  }

  sealed class Result {

    abstract val download: QueuedDownload

    val success get() = this is Success

    data class Success(override val download: QueuedDownload, val tmpDir: File) : Result()
    data class Failure(override val download: QueuedDownload, val error: Throwable) : Result()
  }

}
