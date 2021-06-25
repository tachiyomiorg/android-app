/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.coil.rememberCoilPainter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.util.Typefaces

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryMangaComfortableGrid(
  library: List<LibraryManga>,
  selectedManga: List<Long>,
  columns: Int,
  onClickManga: (LibraryManga) -> Unit = {},
  onLongClickManga: (LibraryManga) -> Unit = {}
) {
  val cells = if (columns > 1) {
    GridCells.Fixed(columns)
  } else {
    GridCells.Adaptive(160.dp)
  }
  LazyVerticalGrid(
    cells = cells,
    modifier = Modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    items(library) { manga ->
      LibraryMangaComfortableGridItem(
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
private fun LibraryMangaComfortableGridItem(
  manga: LibraryManga,
  isSelected: Boolean,
  unread: Int?,
  downloaded: Int?,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  val fontStyle = LocalTextStyle.current.merge(
    TextStyle(letterSpacing = 0.sp, fontFamily = Typefaces.ptSansFont, fontSize = 14.sp)
  )

  Box(
    modifier = Modifier
      .selectedBackground(isSelected)
      .combinedClickable(onClick = onClick, onLongClick = onLongClick)
      .padding(4.dp)
      .fillMaxWidth()
  ) {
    Column {
      Image(
        painter = rememberCoilPainter(rememberMangaCover(manga)),
        contentDescription = null,
        modifier = Modifier
          .aspectRatio(3f / 4f)
          .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop
      )
      Text(
        text = manga.title,
        style = fontStyle,
        maxLines = 3,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
      )
    }
    LibraryMangaBadges(
      unread = unread,
      downloaded = downloaded,
      modifier = Modifier.padding(4.dp)
    )
  }
}

private fun Modifier.selectedBackground(isSelected: Boolean) = composed {
  if (isSelected) {
    background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
  } else {
    this
  }
}

