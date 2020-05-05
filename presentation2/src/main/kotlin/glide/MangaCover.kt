/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

/**
 * Class used to load manga covers with Glide.
 *
 * Note: DO NOT implement equals on this class or turn it into a data class. Glide internally relies
 * on equals implementations to check whether an image is already loaded, but then we can't check
 * if/when our cover files have changed.
 */
internal class MangaCover(
  val id: Long,
  val sourceId: Long,
  val cover: String,
  val favorite: Boolean
) {

  companion object {
    fun from(manga: Manga): MangaCover {
      return MangaCover(manga.id, manga.sourceId, manga.cover, manga.favorite)
    }

    fun from(manga: LibraryManga): MangaCover {
      return MangaCover(manga.id, manga.sourceId, manga.cover, true)
    }
  }

}

