/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.DisplayMode
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.ui.core.components.ChoiceChip
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.viewModel
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibrarySheet(
  initialPage: Int,
  onPageChanged: (Int) -> Unit
) {
  val vm = viewModel<LibrarySheetViewModel> {
    LibrarySheetViewModel.Props(initialPage = initialPage)
  }
  val scope = rememberCoroutineScope()
  val selectedPage = vm.selectedPage
  val pagerState = rememberPagerState(3, selectedPage, initialOffscreenLimit = 3)
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect {
      vm.selectedPage = it
      onPageChanged(it)
    }
  }

  TabRow(
    modifier = Modifier.requiredHeight(48.dp),
    selectedTabIndex = selectedPage,
    backgroundColor = CustomColors.current.bars,
    contentColor = CustomColors.current.onBars,
    indicator = { TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, it)) }
  ) {
    listOf("Filter", "Sort", "Display").forEachIndexed { i, title ->
      Tab(
        selected = selectedPage == i,
        onClick = { scope.launch { pagerState.animateScrollToPage(i) } }
      ) {
        Text(title)
      }
    }
  }
  HorizontalPager(
    state = pagerState,
    verticalAlignment = Alignment.Top,
  ) { page ->
    LazyColumn {
      when (page) {
        0 -> FiltersPage(filters = vm.filters, onClick = { vm.toggleFilter(it) })
        1 -> SortPage(sorting = vm.sorting, onClick = { vm.toggleSort(it) })
        2 -> DisplayPage(
          displayMode = vm.displayMode,
          downloadBadges = vm.downloadBadges,
          unreadBadges = vm.unreadBadges,
          categoryTabs = vm.showCategoryTabs,
          allCategory = vm.showAllCategory,
          countInCategory = vm.showCountInCategory,
          onDisplayModeClick = { vm.changeDisplayMode(it) },
          onDownloadBadgesClick = { vm.toggleDownloadBadges() },
          onUnreadBadgesClick = { vm.toggleUnreadBadges() },
          onCategoryTabsClick = { vm.toggleShowCategoryTabs() },
          onAllCategoryClick = { vm.toggleShowAllCategory() },
          onCountInCategoryClick = { vm.toggleShowCountInCategory() }
        )
      }
    }
  }
}

private fun LazyListScope.FiltersPage(
  filters: List<LibraryFilter>,
  onClick: (LibraryFilter.Type) -> Unit
) {
  items(filters) { (filter, state) ->
    ClickableRow(onClick = { onClick(filter) }) {
      TriStateCheckbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = state.asToggleableState(),
        onClick = { onClick(filter) }
      )
      Text(filter.name)
    }
  }
}

private fun LazyListScope.SortPage(
  sorting: LibrarySort,
  onClick: (LibrarySort.Type) -> Unit
) {
  items(LibrarySort.types) { type ->
    ClickableRow(onClick = { onClick(type) }) {
      val iconModifier = Modifier.requiredWidth(56.dp)
      if (sorting.type == type) {
        val icon = if (sorting.isAscending) {
          Icons.Default.KeyboardArrowUp
        } else {
          Icons.Default.KeyboardArrowDown
        }
        Icon(icon, null, iconModifier, MaterialTheme.colors.primary)
      } else {
        Spacer(iconModifier)
      }
      Text(type.name)
    }
  }
}

private fun LazyListScope.DisplayPage(
  displayMode: DisplayMode,
  downloadBadges: Boolean,
  unreadBadges: Boolean,
  categoryTabs: Boolean,
  allCategory: Boolean,
  countInCategory: Boolean,
  onDisplayModeClick: (DisplayMode) -> Unit,
  onDownloadBadgesClick: () -> Unit,
  onUnreadBadgesClick: () -> Unit,
  onCategoryTabsClick: () -> Unit,
  onAllCategoryClick: () -> Unit,
  onCountInCategoryClick: () -> Unit
) {
  item {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
        text = "Display mode".toUpperCase(Locale.ROOT),
        modifier = Modifier.padding(bottom = 12.dp),
        style = MaterialTheme.typography.subtitle2,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
      )
      FlowRow(mainAxisSpacing = 4.dp) {
        DisplayMode.values.forEach {
          ChoiceChip(
            isSelected = it == displayMode,
            onClick = { onDisplayModeClick(it) },
            content = { Text(it.name) }
          )
        }
      }
    }
  }
  item {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
        text = "Badges".toUpperCase(Locale.ROOT),
        modifier = Modifier.padding(bottom = 12.dp),
        style = MaterialTheme.typography.subtitle2,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
      )
      FlowRow(mainAxisSpacing = 4.dp) {
        ChoiceChip(
          isSelected = unreadBadges,
          onClick = { onUnreadBadgesClick() },
          content = { Text("Unread") }
        )
        ChoiceChip(
          isSelected = downloadBadges,
          onClick = { onDownloadBadgesClick() },
          content = { Text("Downloaded") }
        )
      }
    }
  }
  item {
    Text(
      text = "Tabs".toUpperCase(Locale.ROOT),
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      style = MaterialTheme.typography.subtitle2,
      color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
    )
  }
  item {
    ClickableRow(onClick = { onCategoryTabsClick() }) {
      Checkbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        checked = categoryTabs,
        onCheckedChange = null
      )
      Text("Show category tabs")
    }
  }
  item {
    ClickableRow(onClick = { onAllCategoryClick() }) {
      Checkbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        checked = allCategory,
        onCheckedChange = null
      )
      Text("Show all category")
    }
  }
  item {
    ClickableRow(onClick = { onCountInCategoryClick() }) {
      Checkbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        checked = countInCategory,
        onCheckedChange = null
      )
      Text("Show number of items")
    }
  }
}

@Composable
private fun ClickableRow(onClick: () -> Unit, content: @Composable () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .requiredHeight(48.dp)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
    content = { content() }
  )
}

private fun LibraryFilter.Value.asToggleableState(): ToggleableState {
  return when (this) {
    Included -> ToggleableState.On
    Excluded -> ToggleableState.Indeterminate
    Missing -> ToggleableState.Off
  }
}
