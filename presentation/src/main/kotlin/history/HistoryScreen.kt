/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.history

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.MangaListItem
import tachiyomi.ui.core.components.MangaListItemColumn
import tachiyomi.ui.core.components.MangaListItemImage
import tachiyomi.ui.core.components.MangaListItemSubtitle
import tachiyomi.ui.core.components.MangaListItemTitle
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun HistoryScreen(navController: NavController) {
  val vm = viewModel<HistoryViewModel>()

  Scaffold(
    topBar = {
      HistoryToolbar(
        searchMode = vm.searchMode,
        searchQuery = vm.searchQuery,
        onChangeSearchQuery = { vm.updateQuery(it) },
        onClickCloseSearch = { vm.closeSearch() },
        onClickSearch = { vm.openSearch() },
        onClickDeleteAll = { vm.deleteAll() }
      )
    }
  ) {
  }
}

@Composable
fun HistoryItem(
  manga: Manga,
  onClickItem: (Manga) -> Unit = { /* TODO Open manga details */ },
  onClickDelete: (Manga) -> Unit = { /* TODO */ },
  onClickPlay: (Manga) -> Unit = { /* TODO */ }
) {
  MangaListItem(
    modifier = Modifier
      .clickable { onClickItem(manga) }
      .height(80.dp)
      .fillMaxWidth()
      .padding(end = 4.dp),
  ) {
    MangaListItemImage(
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(3f / 4f)
        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium),
      mangaCover = rememberMangaCover(manga)
    )
    MangaListItemColumn(
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp, end = 8.dp)
    ) {
      MangaListItemTitle(
        text = manga.title,
        maxLines = 2,
        fontWeight = FontWeight.SemiBold
      )
      MangaListItemSubtitle(
        text = "Ch. 69 - 10:59" // TODO
      )
    }
    IconButton(onClick = { onClickDelete(manga) }) {
      Icon(imageVector = Icons.Outlined.Delete, contentDescription = "")
    }
    IconButton(onClick = { onClickPlay(manga) }) {
      Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "")
    }
  }
}
