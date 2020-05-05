/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

internal class MangaCoverModelLoader(
  private val delegate: MangaCoverModelLoaderDelegate
) : ModelLoader<MangaCover, InputStream> {

  override fun buildLoadData(
    manga: MangaCover,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<InputStream>? {
    return delegate.build(manga)
  }

  override fun handles(model: MangaCover): Boolean {
    return true
  }

  class Factory(
    private val delegate: MangaCoverModelLoaderDelegate
  ) : ModelLoaderFactory<MangaCover, InputStream> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<MangaCover, InputStream> {
      return MangaCoverModelLoader(delegate)
    }

    override fun teardown() {

    }

  }

}
