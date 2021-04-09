/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.SwipeToRefreshLayout
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.viewmodel.viewModel

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
        Toolbar(
          title = { Text(manga.title) },
          navigationIcon = { BackIconButton(navController) }
        )
      }
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
