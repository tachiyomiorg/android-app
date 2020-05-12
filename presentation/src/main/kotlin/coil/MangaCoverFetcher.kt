/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import coil.bitmappool.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import okio.source
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.library.service.LibraryCovers
import tachiyomi.source.HttpSource
import java.io.File

internal class LibraryMangaFetcher(
  private val defaultClient: OkHttpClient,
  private val libraryCovers: LibraryCovers,
  private val getLocalCatalog: GetLocalCatalog,
  private val coversCache: Cache
) : Fetcher<MangaCover> {

  override fun key(data: MangaCover): String? {
    return when (getResourceType(data.cover)) {
      Type.File -> {
        val cover = File(data.cover.substringAfter("file://"))
        "${data.cover}_${cover.lastModified()}"
      }
      Type.URL -> {
        val cover = libraryCovers.find(data.id)
        if (data.favorite && (!cover.exists() || cover.lastModified() == 0L)) {
          null
        } else {
          "${data.cover}_${cover.lastModified()}"
        }
      }
      null -> null
    }
  }

  override suspend fun fetch(
    pool: BitmapPool,
    data: MangaCover,
    size: Size,
    options: Options
  ): SourceResult {
    return when (getResourceType(data.cover)) {
      Type.File -> getFileLoader(data)
      Type.URL -> getUrlLoader2(data)
      null -> error("Not a valid image")
    }
  }

  private fun getFileLoader(manga: MangaCover): SourceResult {
    val file = File(manga.cover.substringAfter("file://"))
    return getFileLoader(file)
  }

  private fun getFileLoader(file: File): SourceResult {
    return SourceResult(
      source = file.source().buffer(),
      mimeType = "image/*",
      dataSource = DataSource.DISK
    )
  }

  private suspend fun getUrlLoader2(manga: MangaCover): SourceResult {
    val file = libraryCovers.find(manga.id)
    if (file.exists() && file.lastModified() != 0L) {
      return getFileLoader(file)
    }

    val call = getCall(manga)

    // TODO this crashes if using suspending call
    val response = withContext(Dispatchers.IO) { call.execute() }
    val body = checkNotNull(response.body) { "Null response source" }

    val shouldSave = manga.favorite && (!file.exists() || file.length() != body.contentLength())
    val coverUnchanged = manga.favorite && file.exists() && file.length() == body.contentLength()

    return if (shouldSave) {
      val tmpFile = File(file.absolutePath + "_tmp")
      body.source().use { input ->
        tmpFile.sink().buffer().use { output ->
          output.writeAll(input)
        }
      }
      tmpFile.renameTo(file)

      getFileLoader(file)
    } else if (coverUnchanged) {
      body.close()
      file.setLastModified(System.currentTimeMillis())
      getFileLoader(file)
    } else {
      SourceResult(
        source = body.source(),
        mimeType = "image/*",
        dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK
      )
    }
  }

  private fun getCall(manga: MangaCover): Call {
    val catalog = getLocalCatalog.get(manga.sourceId)
    val source = catalog?.source as? HttpSource
    val client = source?.client ?: defaultClient

    val newClient = client.newBuilder()
      .cache(coversCache)
      .build()

    val request = Request.Builder().url(manga.cover).also {
      if (source != null) {
        it.headers(source.headers)
      }
    }.build()

    return newClient.newCall(request)
  }

  private fun getResourceType(cover: String): Type? {
    return when {
      cover.isEmpty() -> null
      cover.startsWith("http") -> Type.URL
      cover.startsWith("/") || cover.startsWith("file://") -> Type.File
      else -> null
    }
  }

  private enum class Type {
    File, URL;
  }

}
