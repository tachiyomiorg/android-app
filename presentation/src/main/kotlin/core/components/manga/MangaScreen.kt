/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components.manga

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.ScrollableColumn
import tachiyomi.ui.core.components.Toolbar

@Composable
fun MangaScreen(
  navController: NavHostController,
  manga: Manga?,
  chapters: List<Chapter> = emptyList(),
  onFavorite: () -> Unit = {},
) {
  if (manga == null) {
    // TODO: loading UX
    LoadingScreen()
    return
  }

  ScrollableColumn {
    MangaInfoHeader(navController, manga, onFavorite)
    MangaChapters(navController, chapters)
  }
}

@Composable
private fun MangaInfoHeader(
  navController: NavHostController,
  manga: Manga,
  onFavorite: () -> Unit
) {
  val cover = MangaCover.from(manga)
  Column {
    Box {
      // TODO: Backdrop
      // CoilImage(model = cover)

      Column {
        Toolbar(
          title = { Text(manga.title) },
          navigationIcon = { BackIconButton(navController) },
          backgroundColor = Color.Transparent,
          elevation = 0.dp
        )

        // Cover + main info
        Row(modifier = Modifier.padding(16.dp)) {
          Surface(
            modifier = Modifier
              .fillMaxWidth(0.3f)
              .aspectRatio(3f / 4f),
            shape = RoundedCornerShape(4.dp)
          ) {
            CoilImage(model = cover)
          }

          Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(manga.title, fontWeight = FontWeight.Bold)
            if (manga.author.isNotBlank()) {
              Text(manga.author)
            }
            if (manga.artist.isNotBlank()) {
              Text(manga.artist)
            }
            Text(manga.status.toString())
            Text(manga.sourceId.toString())
          }
        }

        // Action bar
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
          Button(onClick = onFavorite) {
            Icon(imageVector = if (manga.favorite) {
              Icons.Default.Favorite
            } else {
              Icons.Default.FavoriteBorder
            }, contentDescription = null)
            Text(if (manga.favorite) "In library" else "Add to library")
          }
        }
      }
    }

    // Description
    Row(modifier = Modifier.padding(16.dp)) {
      Text(manga.description)
    }
  }
}

@Composable
private fun MangaChapters(navController: NavHostController, chapters: List<Chapter>) {
  chapters.forEach {
    Text("${it.number}: ${it.name}")
  }
}
