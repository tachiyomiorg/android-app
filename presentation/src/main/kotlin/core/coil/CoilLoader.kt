/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.coil

import android.app.Application
import coil.ImageLoader
import coil.util.CoilUtils
import tachiyomi.core.di.AppScope
import tachiyomi.core.http.Http
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.library.service.LibraryCovers
import javax.inject.Inject

val CoilLoader = AppScope.getInstance<CoilLoaderFactory>().create()

class CoilLoaderFactory @Inject constructor(
  private val context: Application,
  private val http: Http,
  private val getLocalCatalog: GetLocalCatalog,
  private val libraryCovers: LibraryCovers
) {

  fun create(): ImageLoader {
    val coilCache = CoilUtils.createDefaultCache(context)

    val libraryFetcher = LibraryMangaFetcher(http.defaultClient, libraryCovers,
      getLocalCatalog, coilCache)

    return ImageLoader.Builder(context)
      .componentRegistry {
        add(libraryFetcher)
        add(CatalogRemoteMapper())
        add(CatalogInstalledFetcher(context))
      }
      .okHttpClient(http.defaultClient.newBuilder().cache(coilCache).build())
      .build()
  }

}
