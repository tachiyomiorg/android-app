/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.updates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.R
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.MangaListItem
import tachiyomi.ui.core.components.MangaListItemColumn
import tachiyomi.ui.core.components.MangaListItemImage
import tachiyomi.ui.core.components.MangaListItemSubtitle
import tachiyomi.ui.core.components.MangaListItemTitle
import tachiyomi.ui.core.components.Toolbar

@Composable
fun UpdatesScreen(navController: NavController) {
  Scaffold(
    topBar = {
      UpdatesToolbar(
        selectedManga = emptyList(),
        selectionMode = false,
        onClickCancelSelection = {},
        onClickSelectAll = {},
        onClickInvertSelection = {},
        onClickRefresh = {}
      )
    }
  ) {
  }
}

@Composable
fun UpdatesToolbar(
  selectedManga: List<Long>,
  selectionMode: Boolean,
  onClickCancelSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickInvertSelection: () -> Unit,
  onClickRefresh: () -> Unit
) {
  when {
    selectionMode -> {
      UpdatesSelectionToolbar(
        selectedManga = selectedManga,
        onClickCancelSelection = onClickCancelSelection,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickInvertSelection
      )
    }
    else -> {
      UpdatesRegularToolbar(
        onClickRefresh = onClickRefresh
      )
    }
  }
}

@Composable
private fun UpdatesSelectionToolbar(
  selectedManga: List<Long>,
  onClickCancelSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickInvertSelection: () -> Unit
) {
  Toolbar(
    title = { Text("${selectedManga.size}") },
    navigationIcon = {
      IconButton(onClick = onClickCancelSelection) {
        Icon(Icons.Default.Close, contentDescription = null)
      }
    },
    actions = {
      IconButton(onClick = onClickSelectAll) {
        Icon(Icons.Default.SelectAll, contentDescription = null)
      }
      IconButton(onClick = onClickInvertSelection) {
        Icon(Icons.Default.FlipToBack, contentDescription = null)
      }
    }
  )
}

@Composable
fun UpdatesRegularToolbar(onClickRefresh: () -> Unit) {
  Toolbar(
    title = { Text(stringResource(R.string.updates_label)) },
    actions = {
      IconButton(onClick = onClickRefresh) {
        Icon(Icons.Default.Refresh, contentDescription = null)
      }
    }
  )
}

@Composable
fun UpdatesItem(
  manga: Manga,
  onClickItem: (Manga) -> Unit = { /* TODO Open chapter in reader */ },
  onClickCover: (Manga) -> Unit = { /* TODO Open manga details */ },
  onClickDownload: (Manga) -> Unit = { /* TODO */}
) {
  MangaListItem(
    modifier = Modifier
      .clickable { onClickItem(manga) }
      .height(56.dp)
      .fillMaxWidth()
      .padding(end = 4.dp),
  ) {
    MangaListItemImage(
      modifier = Modifier
        .clickable { onClickCover(manga) }
        .fillMaxHeight()
        .aspectRatio(1f)
        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium),
      mangaCover = rememberMangaCover(manga)
    )
    MangaListItemColumn(
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp)
    ) {
      MangaListItemTitle(
        text = manga.title,
        fontWeight = FontWeight.SemiBold
      )
      MangaListItemSubtitle(
        text = "Chapter 69" // TODO
      )
    }
    // TODO Replace with Download Composable when that is implemented
    IconButton(onClick = { onClickDownload(manga) }) {
      Icon(imageVector = Icons.Outlined.Download, contentDescription = "")
    }
  }
}
