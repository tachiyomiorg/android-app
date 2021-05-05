/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.core.coil.rememberMangaCover

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
  Row(
    modifier = Modifier
      .combinedClickable(onClick = onClick, onLongClick = onLongClick)
      .selectionBackground(isSelected)
      .requiredHeight(56.dp)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      painter = rememberCoilPainter(rememberMangaCover(manga)),
      contentDescription = null,
      modifier = Modifier
        .size(40.dp)
        .clip(MaterialTheme.shapes.medium),
      contentScale = ContentScale.Crop
    )
    Text(
      text = manga.title,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      style = MaterialTheme.typography.body2
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
