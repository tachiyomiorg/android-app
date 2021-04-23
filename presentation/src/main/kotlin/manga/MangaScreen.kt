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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.theme.TransparentStatusBar
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
    TransparentStatusBar {
      Column {
        Toolbar(
          title = {},
          navigationIcon = { BackIconButton(navController) },
          contentColor = MaterialTheme.colors.onBackground,
          backgroundColor = Color.Transparent,
          elevation = 0.dp
        )
        LoadingScreen()
      }
    }
    return
  }

  // TODO
  val onRefresh = {}
  val onTracking = {}
  val onWebView = {}
  val onFavorite = { vm.toggleFavorite() }
  val onToggle = { vm.toggleExpandedSummary() }

  val swipeState = rememberSwipeRefreshState(vm.isRefreshing)

  TransparentStatusBar {
    SwipeRefresh(
      state = swipeState,
      onRefresh = onRefresh,
      swipeEnabled = false // enabling it crashes the app at the moment
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
}
