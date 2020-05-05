/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import tachiyomi.domain.catalog.model.CatalogInstalled

internal class CatalogInstalledModelLoader(
  context: Context
) : ModelLoader<CatalogInstalled, Drawable> {

  private val packageManager = context.packageManager

  override fun buildLoadData(
    model: CatalogInstalled,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<Drawable> {
    val key = getCatalogKey(model)
    val fetcher = Fetcher(packageManager, model)
    return ModelLoader.LoadData(key, fetcher)
  }

  override fun handles(model: CatalogInstalled): Boolean {
    return true
  }

  private class Fetcher(
    private val packageManager: PackageManager,
    private val model: CatalogInstalled
  ) : DataFetcher<Drawable> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
      try {
        val icon = packageManager.getApplicationIcon(model.pkgName)
        callback.onDataReady(icon)
      } catch (e: PackageManager.NameNotFoundException) {
        callback.onLoadFailed(e)
      }
    }

    override fun getDataClass(): Class<Drawable> {
      return Drawable::class.java
    }

    override fun getDataSource(): DataSource {
      return DataSource.DATA_DISK_CACHE
    }

    override fun cleanup() {
    }

    override fun cancel() {
    }

  }

  class Factory(private val context: Context) : ModelLoaderFactory<CatalogInstalled, Drawable> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<CatalogInstalled, Drawable> {
      return CatalogInstalledModelLoader(context)
    }

    override fun teardown() {

    }

  }
}
