/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.library.service.LibraryCovers
import tachiyomi.source.HttpSource
import java.io.File
import java.io.InputStream

internal class MangaCoverModelLoaderDelegate(
  private val libraryCovers: LibraryCovers,
  private val getLocalCatalog: GetLocalCatalog,
  private val defaultClient: OkHttpClient,
  private val coversCache: Cache
) {

  fun build(manga: MangaCover): ModelLoader.LoadData<InputStream>? {
    return if (manga.favorite) {
      val customCover = libraryCovers.findCustom(manga.id)
      if (customCover.exists()) {
        return getFileLoader(customCover)
      }

      when (getResourceType(manga.cover)) {
        Type.Empty -> null
        Type.File -> getFileLoader(manga)
        Type.URL -> getUrlLoaderWithCopy(manga)
      }
    } else {
      when (getResourceType(manga.cover)) {
        Type.Empty -> null
        Type.File -> getFileLoader(manga)
        Type.URL -> getUrlLoader(manga)
      }
    }
  }

  private fun getFileLoader(manga: MangaCover): ModelLoader.LoadData<InputStream> {
    val file = File(manga.cover.substringAfter("file://"))
    return getFileLoader(file)
  }

  private fun getFileLoader(file: File): ModelLoader.LoadData<InputStream> {
    val key = ObjectKey("${file.absolutePath}/${file.lastModified()}")
    val fetcher = FileFetcher(file)
    return ModelLoader.LoadData(key, fetcher)
  }

  private fun getUrlLoaderWithCopy(manga: MangaCover): ModelLoader.LoadData<InputStream> {
    val file = libraryCovers.find(manga.id)
    val callFn = getCallFn(manga)
    val key = ObjectKey("${manga.id}/${file.lastModified()}")
    val fetcher = MangaCoverUrlFetcher(callFn, file)
    return ModelLoader.LoadData(key, fetcher)
  }

  private fun getUrlLoader(manga: MangaCover): ModelLoader.LoadData<InputStream> {
    val callFn = getCallFn(manga)
    val key = ObjectKey(manga.cover)
    val fetcher = MangaCoverUrlFetcher(callFn)
    return ModelLoader.LoadData(key, fetcher)
  }

  private fun getCallFn(manga: MangaCover): () -> Call {
    return {
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

      newClient.newCall(request)
    }
  }

  private fun getResourceType(cover: String): Type {
    return when {
      cover.isEmpty() -> Type.Empty
      cover.startsWith("http") -> Type.URL
      cover.startsWith("/") || cover.startsWith("file://") -> Type.File
      else -> Type.Empty
    }
  }

  private enum class Type {
    Empty, File, URL;
  }

}
