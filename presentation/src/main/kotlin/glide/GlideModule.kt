/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.Cache
import tachiyomi.core.di.AppScope
import tachiyomi.core.http.Http
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryCovers
import tachiyomi.domain.manga.model.Manga
import toothpick.ktp.delegate.inject
import java.io.File
import java.io.InputStream

/**
 * Class used to update Glide module settings
 */
@GlideModule
internal class TachiyomiGlideModule : AppGlideModule() {

  private val http by inject<Http>()

  private val getLocalCatalog by inject<GetLocalCatalog>()

  private val libraryCovers by inject<LibraryCovers>()

  init {
    AppScope.inject(this)
  }

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    builder.setDiskCache(InternalCacheDiskCacheFactory(context, 30 * 1024 * 1024))
  }

  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    val coversCache = File(context.cacheDir, "cover_cache").run {
      mkdirs()
      Cache(this, 50 * 1024 * 1024)
    }

    val mangaLoaderDelegate = MangaCoverModelLoaderDelegate(
      libraryCovers, getLocalCatalog, http.defaultClient, coversCache)

    val networkFactory = OkHttpUrlLoader.Factory(http.defaultClient)
    val mangaCoverFactory = MangaCoverModelLoader.Factory(mangaLoaderDelegate)
    val mangaFactory = MangaModelLoader.Factory()
    val libraryMangaFactory = LibraryMangaModelLoader.Factory()
    val remoteCatalogFactory = CatalogRemoteModelLoader.Factory()

    registry.replace(GlideUrl::class.java, InputStream::class.java, networkFactory)
    registry.append(MangaCover::class.java, InputStream::class.java, mangaCoverFactory)
    registry.append(Manga::class.java, InputStream::class.java, mangaFactory)
    registry.append(LibraryManga::class.java, InputStream::class.java, libraryMangaFactory)
    registry.append(CatalogRemote::class.java, InputStream::class.java, remoteCatalogFactory)
  }
}
