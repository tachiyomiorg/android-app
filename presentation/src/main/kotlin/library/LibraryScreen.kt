/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.SelectAll
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.CategoryWithCount
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
    snapshotFlow { pagerState.currentPage }.collect {
      vm.setSelectedPage(it)
    }
  }

  ModalBottomSheetLayout(
    sheetState = sheetState,
    sheetContent = { LibrarySheet() }
  ) {
    Column {
      LibraryToolbar(
        selectedCategory = vm.selectedCategory,
        selectedManga = vm.selectedManga,
        showCategoryTabs = vm.showCategoryTabs,
        showCountInCategory = vm.showCountInCategory,
        onClickFilter = { scope.launch { sheetState.show() } },
        onClickCloseSelection = { vm.unselectAll() },
        onClickSelectAll = { vm.selectAllInCurrentCategory() },
        onClickUnselectAll = { vm.flipAllInCurrentCategory() }
      )
      LibraryTabs(
        state = pagerState,
        visible = vm.showCategoryTabs,
        categories = vm.categories,
        showCount = vm.showCountInCategory,
        onClickTab = { scope.launch { pagerState.animateScrollToPage(it) } }
      )
      LibraryPager(
        state = pagerState,
        categories = vm.categories,
        displayMode = vm.displayMode,
        selectedManga = vm.selectedManga,
        getLibraryForPage = { vm.getLibraryForCategoryIndex(it) },
        onClickManga = { manga ->
          if (vm.selectedManga.isEmpty()) {
            navController.navigate("${Route.LibraryManga.id}/${manga.id}")
          } else {
            vm.toggleManga(manga)
          }
        },
        onLongClickManga = { vm.toggleManga(it) }
      )
    }
  }
}

@Composable
private fun LibraryToolbar(
  selectedCategory: CategoryWithCount?,
  selectedManga: List<Long>,
  showCategoryTabs: Boolean,
  showCountInCategory: Boolean,
  onClickFilter: () -> Unit,
  onClickCloseSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickUnselectAll: () -> Unit
) {
  if (selectedManga.isEmpty()) {
    // Regular toolbar
    Toolbar(
      title = {
        val text = when {
          showCategoryTabs -> stringResource(R.string.library_label)
          selectedCategory != null -> selectedCategory.visibleName + if (!showCountInCategory) {
            ""
          } else {
            " (${selectedCategory.mangaCount})"
          }
          else -> ""
        }
        Text(text)
      },
      actions = {
        IconButton(onClick = onClickFilter) {
          Icon(Icons.Default.FilterList, contentDescription = null)
        }
      }
    )
  } else {
    // Selection toolbar
    Toolbar(
      title = { Text("${selectedManga.size}") },
      navigationIcon = {
        IconButton(onClick = onClickCloseSelection) {
          Icon(Icons.Default.Close, contentDescription = null)
        }
      },
      actions = {
        IconButton(onClick = onClickSelectAll) {
          Icon(Icons.Default.SelectAll, contentDescription = null)
        }
        IconButton(onClick = onClickUnselectAll) {
          Icon(Icons.Default.FlipToBack, contentDescription = null)
        }
      }
    )
    BackHandler(onBack = onClickCloseSelection)
  }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
private fun LibraryTabs(
  state: PagerState,
  visible: Boolean,
  categories: List<CategoryWithCount>,
  showCount: Boolean,
  onClickTab: (Int) -> Unit
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
          onClick = { onClickTab(i) },
          text = {
            Text(
              category.visibleName + if (!showCount) {
                ""
              } else {
                " (${category.mangaCount})"
              }
            )
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun LibraryPager(
  state: PagerState,
  categories: List<CategoryWithCount>,
  displayMode: DisplayMode,
  selectedManga: List<Long>,
  getLibraryForPage: @Composable (Int) -> State<List<LibraryManga>>,
  onClickManga: (LibraryManga) -> Unit,
  onLongClickManga: (LibraryManga) -> Unit
) {
  if (categories.isEmpty()) return

  HorizontalPager(state = state, offscreenLimit = 1) { page ->
    val library by getLibraryForPage(page)
    when (displayMode) {
      DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
        library = library,
        selectedManga = selectedManga,
        onClickManga = onClickManga,
        onLongClickManga = onLongClickManga
      )
      DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
        library = library,
        selectedManga = selectedManga,
        onClickManga = onClickManga,
        onLongClickManga = onLongClickManga
      )
      DisplayMode.List -> LibraryMangaList(
        library = library,
        selectedManga = selectedManga,
        onClickManga = onClickManga,
        onLongClickManga = onLongClickManga
      )
    }
  }
}
