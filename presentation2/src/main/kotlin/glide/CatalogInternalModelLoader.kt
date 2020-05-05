/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.ui.util.dpToPx
import tachiyomi.ui.widget.TextOvalDrawable

internal class CatalogInternalModelLoader : ModelLoader<CatalogInternal, Drawable> {

  override fun buildLoadData(
    model: CatalogInternal,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<Drawable>? {
    val key = getCatalogKey(model)
    val fetcher = Fetcher(model)
    return ModelLoader.LoadData(key, fetcher)
  }

  override fun handles(model: CatalogInternal): Boolean {
    return true
  }

  private class Fetcher(
    private val model: CatalogInternal
  ) : DataFetcher<Drawable> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
      val sourceName = model.source.name
      val text = if (sourceName.isNotEmpty()) {
        sourceName.take(1)
      } else {
        ""
      }

      val drawable = TextOvalDrawable(
        text = text,
        backgroundColor = TextOvalDrawable.Colors.getColor(sourceName),
        textColor = Color.WHITE
      )
      val padding = 2.dpToPx
      drawable.setPadding(padding, padding, padding, padding)

      callback.onDataReady(drawable)
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

  class Factory : ModelLoaderFactory<CatalogInternal, Drawable> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<CatalogInternal, Drawable> {
      return CatalogInternalModelLoader()
    }

    override fun teardown() {

    }

  }

}
