/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover

@Composable
fun LibraryMangaList(
  library: List<LibraryManga>,
  onClickManga: (LibraryManga) -> Unit = {}
) {
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(library) { manga ->
      LibraryMangaListItem(
        manga = manga,
        unread = null, // TODO
        downloaded = null, // TODO
        onClick = { onClickManga(manga) }
      )
    }
  }
}

@Composable
private fun LibraryMangaListItem(
  manga: LibraryManga,
  unread: Int?,
  downloaded: Int?,
  onClick: () -> Unit = {}
) {
  val cover = remember(manga.id) { MangaCover.from(manga) }
  Row(
    modifier = Modifier.clickable(onClick = onClick)
      .requiredHeight(56.dp)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    CoilImage(
      model = cover,
      modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.medium)
    )
    Text(
      text = manga.title,
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
      style = MaterialTheme.typography.body2
    )
    LibraryMangaBadges(unread, downloaded)
  }
}
