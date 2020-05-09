/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import android.app.Application
import coil.ImageLoader
import okhttp3.Cache
import tachiyomi.core.di.AppScope
import tachiyomi.core.http.Http
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.library.service.LibraryCovers
import java.io.File
import javax.inject.Inject

val CoilLoader = AppScope.getInstance<CoilLoaderFactory>().create()

class CoilLoaderFactory @Inject constructor(
  private val context: Application,
  private val http: Http,
  private val getLocalCatalog: GetLocalCatalog,
  private val libraryCovers: LibraryCovers
) {

  fun create(): ImageLoader {
    val coversCache = File(context.cacheDir, "cover_cache").run {
      mkdirs()
      Cache(this, 50 * 1024 * 1024)
    }
    val libraryFetcher = LibraryMangaFetcher(http.defaultClient, libraryCovers,
      getLocalCatalog, coversCache)

    return ImageLoader.Builder(context)
      .componentRegistry {
        add(libraryFetcher)
        add(CatalogRemoteMapper())
        add(CatalogInstalledFetcher(context))
      }
      .okHttpClient(http.defaultClient)
      .build()
  }

}
