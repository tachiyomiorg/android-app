/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import tachiyomi.domain.download.model.QueuedDownload
import java.io.File
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

internal class DownloadCompressor @Inject constructor(
  private val directoryProvider: DownloadDirectoryProvider
) {

  fun worker(
    scope: CoroutineScope,
    download: QueuedDownload,
    tmpChapterDir: File,
    compressionResult: SendChannel<Result>
  ) = scope.launch(Dispatchers.IO) {
    val chapterDir = directoryProvider.getChapterDir(download)
    val tmpZip = File(chapterDir.absolutePath + ".cbz.tmp")
    val result = try {
      compressChapter(tmpChapterDir, tmpZip)
      Result.Success(download, tmpZip)
    } catch (e: Throwable) {
      Result.Failure(download, e)
    }

    compressionResult.send(result)
  }

  private fun compressChapter(tmpChapterDir: File, tmpZip: File) {
    val files = checkNotNull(tmpChapterDir.listFiles()) { "Chapter directory does not exist" }

    val outStream = ZipOutputStream(tmpZip.outputStream())
    outStream.setLevel(Deflater.NO_COMPRESSION)

    outStream.use {
      for (file in files) {
        file.inputStream().use { input ->
          outStream.putNextEntry(ZipEntry(file.name))
          input.copyTo(outStream)
          outStream.closeEntry()
        }
      }
    }
  }

  sealed class Result {

    abstract val download: QueuedDownload

    val success get() = this is Success

    data class Success(override val download: QueuedDownload, val tmpZip: File) : Result()
    data class Failure(override val download: QueuedDownload, val error: Throwable) : Result()
  }

}
