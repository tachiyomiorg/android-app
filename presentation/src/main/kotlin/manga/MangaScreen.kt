/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.SwipeToRefreshLayout
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.viewmodel.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MangaScreen(
  navController: NavHostController,
  mangaId: Long
) {
  val vm = viewModel<MangaViewModel> {
    MangaViewModel.Params(mangaId)
  }

  val manga = vm.manga
  if (manga == null) {
    // TODO: loading UX
    Column {
      Toolbar(
        title = {},
        navigationIcon = { BackIconButton(navController) }
      )
      LoadingScreen()
    }
    return
  }

  // TODO
  val isRefreshing = false
  val onRefresh = {}
  val onTracking = {}
  val onWebView = {}
  val onFavorite = { vm.favorite() }
  val onToggle = { vm.toggleExpandedSummary() }

  SwipeToRefreshLayout(
    refreshingState = isRefreshing,
    onRefresh = onRefresh,
    refreshIndicator = {
      Surface(elevation = 10.dp, shape = CircleShape) {
        CircularProgressIndicator(
          modifier = Modifier.size(36.dp).padding(8.dp),
          strokeWidth = 3.dp
        )
      }
    }
  ) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      item {
        MangaInfoHeader(
          navController,
          manga,
          vm.source,
          vm.expandedSummary,
          onFavorite,
          onTracking,
          onWebView,
          onToggle
        )
      }

      item {
        ChapterHeader(vm.chapters, {})
      }

      items(vm.chapters) { chapter ->
        ChapterRow(chapter, false, {})
      }
    }
  }
}

@Composable
private fun MangaInfoHeader(
  navController: NavHostController,
  manga: Manga,
  source: Source?,
  expandedSummary: Boolean,
  onFavorite: () -> Unit,
  onTracking: () -> Unit,
  onWebView: () -> Unit,
  onToggle: () -> Unit
) {
  Column {
    Box {
      // TODO: Backdrop
      // CoilImage(model = cover)

      Column {
        Toolbar(
          title = { Text(manga.title) },
          navigationIcon = { BackIconButton(navController) }
        )

        // Cover + main info
        Row(modifier = Modifier.padding(16.dp)) {
          Surface(
            modifier = Modifier.fillMaxWidth(0.3f).aspectRatio(3f / 4f),
            shape = RoundedCornerShape(4.dp)
          ) {
            CoilImage(model = rememberMangaCover(manga))
          }

          Column(
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp).align(Alignment.Bottom)
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).weight(1f),
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

private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)

@Composable
private fun ChapterHeader(
  chapters: List<Chapter>,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier.clickable(onClick = onClick).padding(start = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text("${chapters.size} chapters", modifier = Modifier.weight(1f))
    IconButton(onClick = { /*TODO*/ }) {
      Icon(Icons.Default.FilterList, null)
    }
  }
}

@Composable
private fun ChapterRow(
  chapter: Chapter,
  isDownloaded: Boolean,
  onClick: () -> Unit,
  onDownloadClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier.height(64.dp).clickable(onClick = onClick).padding(start = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        chapter.name,
        style = MaterialTheme.typography.body2,
        color = LocalContentColor.current.copy(
          alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
        )
      )
      val subtitleStr = buildAnnotatedString {
        if (chapter.dateUpload > 0) {
          append(dateFormat.format(Date(chapter.dateUpload)))
        }
        if (!chapter.read && chapter.progress > 0) {
          if (length > 0) append(" • ")
          append(
            AnnotatedString(
              "Page " + (chapter.progress + 1).toString(),
              SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled))
            )
          )
        }
        if (chapter.scanlator.isNotBlank()) {
          if (length > 0) append(" • ")
          append(chapter.scanlator)
        }
      }
      Text(
        subtitleStr,
        style = MaterialTheme.typography.caption,
        color = LocalContentColor.current.copy(
          alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
        )
      )
    }
    if (isDownloaded) {
      DownloadedIconButton(onDeleteClick)
    } else {
      DownloadIconButton(onDownloadClick)
    }
  }
}

@Composable
private fun DownloadIconButton(onClick: () -> Unit) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.fillMaxHeight()
  ) {
    Surface(
      shape = CircleShape,
      border = BorderStroke(2.dp, LocalContentColor.current.copy(alpha = ContentAlpha.disabled)),
    ) {
      Icon(
        Icons.Default.ArrowDownward,
        null,
        Modifier.size(22.dp).padding(2.dp),
        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
      )
    }
  }
}

@Composable
private fun DownloadedIconButton(onClick: () -> Unit) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.fillMaxHeight()
  ) {
    Surface(shape = CircleShape, color = LocalContentColor.current) {
      Icon(
        Icons.Default.Check,
        null,
        Modifier.size(22.dp).padding(2.dp),
        MaterialTheme.colors.surface
      )
    }
  }
}
