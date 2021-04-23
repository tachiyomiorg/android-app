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
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.DisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.categories.visibleName
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.viewModel
import tachiyomi.ui.main.Route

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun LibraryScreen(navController: NavController) {
  val vm = viewModel<LibraryViewModel>()
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
  val pagerState = rememberPagerState(vm.categories.size, vm.selectedCategoryIndex)
  LaunchedEffect(pagerState) {
    snapshotFlow {
      vm.setSelectedPage(pagerState.currentPage)
    }
  }

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
        state = pagerState,
        visible = vm.showCategoryTabs,
        categories = vm.categories,
        onTabClicked = { scope.launch { pagerState.animateScrollToPage(it) } }
      )
      LibraryPager(
        state = pagerState,
        categories = vm.categories,
        displayMode = vm.displayMode,
        getLibraryForPage = { vm.getLibraryForCategoryIndex(it) },
        onMangaClicked = { navController.navigate("${Route.LibraryManga.id}/${it.id}") }
      )
    }
  }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
private fun LibraryTabs(
  state: PagerState,
  visible: Boolean,
  categories: List<Category>,
  onTabClicked: (Int) -> Unit
) {
  if (categories.isEmpty()) return

  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(),
    exit = shrinkVertically()
  ) {
    ScrollableTabRow(
      selectedTabIndex = state.currentPage,
      backgroundColor = CustomColors.current.bars,
      contentColor = CustomColors.current.onBars,
      edgePadding = 0.dp,
      indicator = { TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(state, it)) }
    ) {
      categories.forEachIndexed { i, category ->
        Tab(
          selected = state.currentPage == i,
          onClick = { onTabClicked(i) },
          text = { Text(category.visibleName) }
        )
      }
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun LibraryPager(
  state: PagerState,
  categories: List<Category>,
  displayMode: DisplayMode,
  getLibraryForPage: @Composable (Int) -> State<List<LibraryManga>>,
  onMangaClicked: (LibraryManga) -> Unit
) {
  if (categories.isEmpty()) return

  HorizontalPager(state = state, offscreenLimit = 1) { page ->
    val library by getLibraryForPage(page)
    when (displayMode) {
      DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
        library = library,
        onClickManga = onMangaClicked
      )
      DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
        library = library,
        onClickManga = onMangaClicked
      )
      DisplayMode.List -> LibraryMangaList(
        library = library,
        onClickManga = onMangaClicked
      )
    }
  }
}
