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
import tachiyomi.domain.library.model.LibraryManga
import java.io.InputStream

internal class LibraryMangaModelLoader(
  private val coverLoader: ModelLoader<MangaCover, InputStream>
) : ModelLoader<LibraryManga, InputStream> {

  override fun buildLoadData(
    manga: LibraryManga,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<InputStream>? {
    val mangaCover = MangaCover.from(manga)
    return coverLoader.buildLoadData(mangaCover, width, height, options)
  }

  override fun handles(model: LibraryManga): Boolean {
    return true
  }

  class Factory : ModelLoaderFactory<LibraryManga, InputStream> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<LibraryManga, InputStream> {
      val coverLoader = factory.build(MangaCover::class.java, InputStream::class.java)
      return LibraryMangaModelLoader(coverLoader)
    }

    override fun teardown() {

    }

  }

}
