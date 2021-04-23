/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.flowlayout.FlowRow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MangaInfoHeader(
  navController: NavHostController,
  manga: Manga,
  source: Source?,
  expandedSummary: Boolean,
  onFavorite: () -> Unit,
  onTracking: () -> Unit,
  onWebView: () -> Unit,
  onToggle: () -> Unit
) {
  Box(modifier = Modifier.height(IntrinsicSize.Min)) {
    val cover = rememberMangaCover(manga)

    var imageLoaded by remember { mutableStateOf(false) }
    val fadeInImage by animateFloatAsState(
      if (imageLoaded) 0.2f else 0f, tween(easing = LinearOutSlowInEasing)
    )

    Image(
      painter = rememberCoilPainter(
        request = cover,
        requestBuilder = {
          listener(onSuccess = { _, _ ->
            imageLoaded = true
          })
        }
      ),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .alpha(fadeInImage),
      contentScale = ContentScale.Crop,
    )

    Column {
      Toolbar(
        title = {},
        navigationIcon = { BackIconButton(navController) },
        contentColor = MaterialTheme.colors.onBackground,
        backgroundColor = Color.Transparent,
        elevation = 0.dp
      )

      // Cover + main info
      Row(modifier = Modifier.padding(top = 16.dp)) {
        Image(
          painter = rememberCoilPainter(cover),
          contentDescription = null,
          modifier = Modifier
            .padding(16.dp)
            .weight(0.33f)
            .aspectRatio(3f / 4f)
            .clip(MaterialTheme.shapes.medium)
        )

        Column(
          modifier = Modifier
            .padding(bottom = 16.dp)
            .weight(0.67f)
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
            Row(
              modifier = Modifier.padding(top = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(manga.status.toString())
              Text("•")
              Text(source?.name.orEmpty())
            }
          }
        }
      }
    }
  }

  // Action bar
  Row(modifier = Modifier.padding(horizontal = 16.dp)) {
    val activeButtonColors = ButtonDefaults.textButtonColors()
    val inactiveButtonColors = ButtonDefaults.textButtonColors(
      contentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
    )

    TextButton(
      onClick = onFavorite,
      modifier = Modifier.weight(1f),
      colors = if (manga.favorite) activeButtonColors else inactiveButtonColors
    ) {
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
    TextButton(
      onClick = onTracking,
      modifier = Modifier.weight(1f),
      colors = inactiveButtonColors
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
        Text("Tracking")
      }
    }
    TextButton(
      onClick = onWebView,
      modifier = Modifier.weight(1f),
      colors = inactiveButtonColors
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Default.Public, contentDescription = null)
        Text("WebView")
      }
    }
  }

  // Description
  Box(modifier = Modifier.animateContentSize()) {
    if (expandedSummary) {
      ExpandedSummary(manga.description, manga.genres, onToggle)
    } else {
      CollapsedSummary(manga.description, manga.genres, onToggle)
    }
  }
}

@Composable
private fun CollapsedSummary(
  description: String,
  genres: List<String>,
  onToggleClick: () -> Unit
) {
  Column {
    Row {
      Text(
        description,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .weight(1f),
        style = MaterialTheme.typography.body2,
        color = LocalContentColor.current.copy(ContentAlpha.medium),
        maxLines = 2
      )
      TextButton(onClick = onToggleClick) {
        Text("More")
      }
    }
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      items(genres) { genre ->
        GenreChip(genre)
      }
    }
  }
}

@Composable
private fun ExpandedSummary(
  description: String,
  genres: List<String>,
  onToggleClick: () -> Unit
) {
  Column {
    Text(
      description,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
      style = MaterialTheme.typography.body2,
      color = LocalContentColor.current.copy(ContentAlpha.medium)
    )
    TextButton(
      onClick = onToggleClick,
      modifier = Modifier.align(Alignment.End)
    ) {
      Text("Less")
    }
    FlowRow(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
      mainAxisSpacing = 4.dp,
      crossAxisSpacing = 6.dp
    ) {
      genres.forEach { genre ->
        GenreChip(genre)
      }
    }
  }
}

@Composable
private fun GenreChip(genre: String) {
  Surface(
    shape = CircleShape,
    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
  ) {
    Text(
      genre,
      color = MaterialTheme.colors.primary,
      style = MaterialTheme.typography.caption,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
  }
}
