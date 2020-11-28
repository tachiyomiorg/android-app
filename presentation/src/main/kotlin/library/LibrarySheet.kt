/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.ToggleableState
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.unit.dp
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.core.components.Pager
import tachiyomi.ui.core.components.PagerState
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class LibrarySheetViewModel @Inject constructor(
  private val libraryPreferences: LibraryPreferences
) : BaseViewModel() {
  var selectedPage by mutableStateOf(0)

  var filters by libraryPreferences.filters(includeAll = true).asState()
    private set

  var sorting by libraryPreferences.sorting().asState()
    private set

  var showAllCategory by libraryPreferences.showAllCategory().asState()
    private set

  fun toggleFilter(type: LibraryFilter.Type) {
    val newFilters = filters
      .map { filterState ->
        if (type == filterState.type) {
          LibraryFilter(type, when (filterState.value) {
            Included -> Excluded
            Excluded -> Missing
            Missing -> Included
          })
        } else {
          filterState
        }
      }

    filters = newFilters
  }

  fun toggleSort(type: LibrarySort.Type) {
    val currentSort = sorting
    sorting = if (type == currentSort.type) {
      currentSort.copy(isAscending = !currentSort.isAscending)
    } else {
      currentSort.copy(type = type)
    }
  }

  fun toggleShowAllCategory() {
    showAllCategory = !showAllCategory
  }
}

@Composable
fun LibrarySheet() {
  val vm = viewModel<LibrarySheetViewModel>()

  val clock = AnimationClockAmbient.current
  val selectedPage = vm.selectedPage
  val state = remember(selectedPage) {
    PagerState(clock, selectedPage, 0, 2)
  }
  onCommit(state.currentPage) {
    if (selectedPage != state.currentPage) {
      vm.selectedPage = state.currentPage
    }
  }

  TabRow(
    modifier = Modifier.height(48.dp),
    selectedTabIndex = selectedPage,
    backgroundColor = CustomColors.current.bars,
    contentColor = CustomColors.current.onBars
  ) {
    listOf("Filter", "Sort", "Display").forEachIndexed { i, title ->
      Tab(selected = selectedPage == i, onClick = { vm.selectedPage = i }) {
        Text(title)
      }
    }
  }
  Pager(state = state) {
    ScrollableColumn(modifier = Modifier.fillMaxSize()) {
      when (page) {
        0 -> FiltersPage(filters = vm.filters, onClick = { vm.toggleFilter(it) })
        1 -> SortPage(sorting = vm.sorting, onClick = { vm.toggleSort(it) })
        2 -> DisplayPage(
          showAllCategory = vm.showAllCategory,
          onShowAllCategoryClick = { vm.toggleShowAllCategory() }
        )
      }
    }
  }
}

@Composable
private fun FiltersPage(
  filters: List<LibraryFilter>,
  onClick: (LibraryFilter.Type) -> Unit
) {
  filters.forEach { (filter, state) ->
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

@Composable
private fun SortPage(
  sorting: LibrarySort,
  onClick: (LibrarySort.Type) -> Unit
) {
  LibrarySort.types.forEach { type ->
    ClickableRow(onClick = { onClick(type) }) {
      val iconModifier = Modifier.width(56.dp)
      if (sorting.type == type) {
        val icon = if (sorting.isAscending) {
          Icons.Default.KeyboardArrowUp
        } else {
          Icons.Default.KeyboardArrowDown
        }
        Icon(icon, iconModifier, MaterialTheme.colors.primary)
      } else {
        Spacer(iconModifier)
      }
      Text(type.name)
    }
  }
}

@Composable
private fun DisplayPage(
  showAllCategory: Boolean,
  onShowAllCategoryClick: () -> Unit
) {
  ClickableRow(onClick = { onShowAllCategoryClick() }) {
    Checkbox(
      modifier = Modifier.padding(horizontal = 16.dp),
      checked = showAllCategory,
      onCheckedChange = { onShowAllCategoryClick() }
    )
    Text("Show all category")
  }
}

@Composable
private fun ClickableRow(onClick: () -> Unit, content: @Composable () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = { onClick() }),
    verticalAlignment = Alignment.CenterVertically,
    children = { content() }
  )
}

private fun LibraryFilter.Value.asToggleableState(): ToggleableState {
  return when (this) {
    Included -> ToggleableState.On
    Excluded -> ToggleableState.Indeterminate
    Missing -> ToggleableState.Off
  }
}
