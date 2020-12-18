/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.AmbientTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.util.Typefaces

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryMangaCompactGrid(
  library: List<LibraryManga>,
  onClickManga: (LibraryManga) -> Unit = {}
) {
  LazyVerticalGrid(
    cells = GridCells.Adaptive(160.dp),
    modifier = Modifier.fillMaxSize().padding(4.dp)
  ) {
    items(library) { manga ->
      LibraryMangaCompactGridItem(
        manga = manga,
        unread = null, // TODO
        downloaded = null, // TODO
        onClick = { onClickManga(manga) }
      )
    }
  }
}

@Composable
private fun LibraryMangaCompactGridItem(
  manga: LibraryManga,
  unread: Int?,
  downloaded: Int?,
  onClick: () -> Unit = {}
) {
  val cover = remember { MangaCover.from(manga) }
  val fontStyle = AmbientTextStyle.current.merge(
    TextStyle(letterSpacing = 0.sp, fontFamily = Typefaces.ptSansFont, fontSize = 14.sp)
  )

  Box(modifier = Modifier.padding(4.dp)
    .fillMaxWidth()
    .aspectRatio(3f / 4f)
    .clip(MaterialTheme.shapes.medium)
    .clickable(onClick = onClick)
  ) {
    CoilImage(model = cover)
    Box(modifier = Modifier.fillMaxSize().then(shadowGradient))
    Text(
      text = manga.title,
      color = Color.White,
      style = fontStyle,
      maxLines = 2,
      modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
    )
    LibraryMangaBadges(
      unread = unread,
      downloaded = downloaded,
      modifier = Modifier.padding(4.dp)
    )
  }
}

private val shadowGradient = Modifier.drawWithCache {
  val gradient = Brush.linearGradient(
    0.75f to Color.Transparent,
    1.0f to Color(0xAA000000),
    start = Offset(0f, 0f),
    end = Offset(0f, size.height)
  )
  onDrawBehind {
    drawRect(gradient)
  }
}
