/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

@Composable
fun rememberMangaCover(manga: Manga): MangaCover {
  return remember(manga.id) {
    MangaCover.from(manga)
  }
}

@Composable
fun rememberMangaCover(manga: LibraryManga): MangaCover {
  return remember(manga.id) {
    MangaCover.from(manga)
  }
}
