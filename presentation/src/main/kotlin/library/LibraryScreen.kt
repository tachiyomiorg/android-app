/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.AmbientAnimationClock
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.DisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.Route
import tachiyomi.ui.categories.visibleName
import tachiyomi.ui.core.components.Pager
import tachiyomi.ui.core.components.PagerState
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.viewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen(navController: NavController) {
  val vm = viewModel<LibraryViewModel>()
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = { LibrarySheet() }
  ) {
    Column {
      Toolbar(
        title = {
          val text = if (vm.showCategoryTabs) {
            stringResource(R.string.library_label)
          } else {
            vm.selectedCategory?.visibleName.orEmpty()
          }
          Text(text)
        },
        actions = {
          IconButton(onClick = { scope.launch { sheetState.show() }}) {
            Icon(Icons.Default.FilterList, contentDescription = null)
          }
        }
      )
      LibraryTabs(
        visible = vm.showCategoryTabs,
        categories = vm.categories,
        selectedPage = vm.selectedCategoryIndex,
        onPageChanged = { vm.setSelectedPage(it) }
      )
      LibraryPager(
        categories = vm.categories,
        displayMode = vm.displayMode,
        selectedPage = vm.selectedCategoryIndex,
        getLibraryForPage = { vm.getLibraryForCategoryIndex(it) },
        onPageChanged = { vm.setSelectedPage(it) },
        onClickManga = { navController.navigate("${Route.LibraryManga.id}/${it.id}") }
      )
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LibraryTabs(
  visible: Boolean,
  categories: List<Category>,
  selectedPage: Int,
  onPageChanged: (Int) -> Unit
) {
  if (categories.isEmpty()) return

  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(),
    exit = shrinkVertically()
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedPage,
      backgroundColor = CustomColors.current.bars,
      contentColor = CustomColors.current.onBars,
      edgePadding = 0.dp
    ) {
      categories.forEachIndexed { i, category ->
        Tab(
          selected = selectedPage == i,
          onClick = { onPageChanged(i) },
          text = { Text(category.visibleName) }
        )
      }
    }
  }
}

@Composable
private fun LibraryPager(
  categories: List<Category>,
  displayMode: DisplayMode,
  selectedPage: Int,
  getLibraryForPage: @Composable (Int) -> State<List<LibraryManga>>,
  onPageChanged: (Int) -> Unit,
  onClickManga: (LibraryManga) -> Unit
) {
  if (categories.isEmpty()) return

  val clock = AmbientAnimationClock.current
  val state = remember(categories.size, selectedPage) {
    PagerState(
      clock = clock,
      currentPage = selectedPage,
      minPage = 0,
      maxPage = categories.lastIndex
    )
  }
  DisposableEffect(state.currentPage) {
    if (state.currentPage != selectedPage) {
      onPageChanged(state.currentPage)
    }
    onDispose { }
  }
  Pager(state = state, offscreenLimit = 1) {
    val library by getLibraryForPage(page)
    when (displayMode) {
      DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
        library = library,
        onClickManga = onClickManga
      )
      DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
        library = library,
        onClickManga = onClickManga
      )
      DisplayMode.List -> LibraryMangaList(
        library = library,
        onClickManga = onClickManga
      )
    }
  }
}
