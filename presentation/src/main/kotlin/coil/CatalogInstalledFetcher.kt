/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import android.app.Application
import coil.bitmappool.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.Size
import tachiyomi.domain.catalog.model.CatalogInstalled

class CatalogInstalledFetcher(context: Application) : Fetcher<CatalogInstalled> {

  private val pkgManager = context.packageManager

  override suspend fun fetch(
    pool: BitmapPool,
    data: CatalogInstalled,
    size: Size,
    options: Options
  ): FetchResult {
    val icon = pkgManager.getApplicationIcon(data.pkgName)
    return DrawableResult(icon, false, DataSource.DISK)
  }

  override fun key(data: CatalogInstalled): String? {
    return data.pkgName
  }

}
