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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.State
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.Route
import tachiyomi.ui.categories.visibleName
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.AutofitGrid
import tachiyomi.ui.core.components.Pager
import tachiyomi.ui.core.components.PagerState
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.components.manga.MangaGridItem
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.viewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen(navController: NavController) {
  val vm = viewModel<LibraryViewModel>()
  val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = { LibrarySheet() }
  ) {
    Column {
      Toolbar(
        title = { Text(stringResource(R.string.library_label)) },
        actions = {
          IconButton(onClick = { sheetState.show() }) {
            Icon(Icons.Default.FilterList)
          }
        }
      )
      LibraryTabs(
        categories = vm.categories,
        selectedPage = vm.selectedCategoryIndex,
        onPageChanged = { vm.setSelectedPage(it) }
      )
      LibraryPager(
        categories = vm.categories,
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
  categories: List<Category>,
  selectedPage: Int,
  onPageChanged: (Int) -> Unit
) {
  AnimatedVisibility(
    visible = categories.isNotEmpty(),
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
  selectedPage: Int,
  getLibraryForPage: @Composable (Int) -> State<List<LibraryManga>>,
  onPageChanged: (Int) -> Unit,
  onClickManga: (LibraryManga) -> Unit
) {
  if (categories.isEmpty()) return

  val clock = AnimationClockAmbient.current
  val state = remember(categories.size, selectedPage) {
    PagerState(
      clock = clock,
      currentPage = selectedPage,
      minPage = 0,
      maxPage = categories.lastIndex
    )
  }
  onCommit(state.currentPage) {
    if (state.currentPage != selectedPage) {
      onPageChanged(state.currentPage)
    }
  }
  Pager(state = state, offscreenLimit = 1) {
    val library = getLibraryForPage(page)
    LibraryGrid(
      library = library.value,
      onClickManga = onClickManga
    )
  }
}

@Composable
private fun LibraryGrid(
  library: List<LibraryManga>,
  onClickManga: (LibraryManga) -> Unit = {}
) {
  AutofitGrid(
    data = library,
    defaultColumnWidth = 160.dp,
    modifier = Modifier.fillMaxSize()
  ) { manga ->
    MangaGridItem(
      title = manga.title,
      cover = MangaCover.from(manga),
      onClick = { onClickManga(manga) }
    )
  }
}
