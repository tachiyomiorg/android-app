/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.MangaListItem
import tachiyomi.ui.core.components.MangaListItemImage
import tachiyomi.ui.core.components.MangaListItemTitle

@Composable
fun LibraryMangaList(
  library: List<LibraryManga>,
  selectedManga: List<Long>,
  onClickManga: (LibraryManga) -> Unit = {},
  onLongClickManga: (LibraryManga) -> Unit = {}
) {
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(library) { manga ->
      LibraryMangaListItem(
        manga = manga,
        isSelected = manga.id in selectedManga,
        unread = null, // TODO
        downloaded = null, // TODO
        onClick = { onClickManga(manga) },
        onLongClick = { onLongClickManga(manga) }
      )
    }
  }
}

@Composable
private fun LibraryMangaListItem(
  manga: LibraryManga,
  isSelected: Boolean,
  unread: Int?,
  downloaded: Int?,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  MangaListItem(
    modifier = Modifier
      .combinedClickable(onClick = onClick, onLongClick = onLongClick)
      .selectionBackground(isSelected)
      .requiredHeight(56.dp)
      .padding(horizontal = 16.dp),
  ) {
    MangaListItemImage(
      modifier = Modifier
        .size(40.dp)
        .clip(MaterialTheme.shapes.medium),
      mangaCover = rememberMangaCover(manga)
    )
    MangaListItemTitle(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      text = manga.title,
    )
    LibraryMangaBadges(unread, downloaded)
  }
}

private fun Modifier.selectionBackground(isSelected: Boolean): Modifier = composed {
  if (isSelected) {
    background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
  } else {
    this
  }
}
