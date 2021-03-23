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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.SwipeToRefreshLayout
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

  // TODO
  val isRefreshing = false
  val onRefresh = {}
  val onTracking = {}
  val onWebView = {}

  SwipeToRefreshLayout(
    refreshingState = isRefreshing,
    onRefresh = onRefresh,
    refreshIndicator = {
      Surface(elevation = 10.dp, shape = CircleShape) {
        CircularProgressIndicator(
          modifier = Modifier
            .size(36.dp)
            .padding(8.dp),
          strokeWidth = 3.dp
        )
      }
    }
  ) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      item {
        MangaInfoHeader(navController, manga, onFavorite, onTracking, onWebView)
      }

      items(chapters) { chapter ->
        ChapterItem(chapter)
      }
    }
  }
}

@Composable
private fun MangaInfoHeader(
  navController: NavHostController,
  manga: Manga,
  onFavorite: () -> Unit,
  onTracking: () -> Unit,
  onWebView: () -> Unit
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

          Column(
            modifier = Modifier
              .padding(start = 16.dp, bottom = 16.dp)
              .align(Alignment.Bottom)
          ) {
            Text(manga.title, style = MaterialTheme.typography.h6, maxLines = 3)

            ProvideTextStyle(
              MaterialTheme.typography.body2.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
              )
            ) {
              Text(manga.author, modifier = Modifier.padding(top = 4.dp))
              if (manga.artist.isNotBlank() && manga.artist != manga.author) {
                Text(manga.artist)
              }
              Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(manga.status.toString())
                Text("•", modifier = Modifier.padding(horizontal = 4.dp))
                Text(manga.sourceId.toString())
              }
            }
          }
        }
      }
    }

    // Action bar
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
      TextButton(onClick = onFavorite, modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = if (manga.favorite) {
              Icons.Default.Favorite
            } else {
              Icons.Default.FavoriteBorder
            }, contentDescription = null
          )
          Text(if (manga.favorite) "In library" else "Add to library")
        }
      }
      TextButton(onClick = onTracking, modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(imageVector = Icons.Default.Sync, contentDescription = null)
          Text("Tracking")
        }
      }
      TextButton(onClick = onWebView, modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(imageVector = Icons.Default.Public, contentDescription = null)
          Text("WebView")
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
private fun ChapterItem(chapter: Chapter) {
  Text("${chapter.number}: ${chapter.name}")
}
