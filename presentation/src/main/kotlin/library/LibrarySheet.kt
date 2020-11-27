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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.ToggleableState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tachiyomi.domain.library.model.LibraryFilter
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

  private val filtersPreference = libraryPreferences.filters()

  // TODO support exclusion
  var filters by mutableStateOf(emptyList<Pair<LibraryFilter, ToggleableState>>())
    private set

  init {
    filtersPreference.stateIn(scope)
      .onEach { enabledFilters ->
        filters = LibraryFilter.values.map { it to ToggleableState(enabledFilters.contains(it)) }
      }
      .launchIn(scope)
  }

  fun toggle(filter: LibraryFilter) {
    val currFilters = filtersPreference.get()
    val newFilters = if (filter in currFilters) {
      currFilters - filter
    } else {
      currFilters + filter
    }
    filtersPreference.set(newFilters)
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
    Column(modifier = Modifier.fillMaxSize()) {
      when (page) {
        0 -> FiltersPage(filters = vm.filters, onClick = { vm.toggle(it) })
        1 -> SortPage()
        2 -> DisplayPage()
      }
    }
  }
}

@Composable
private fun FiltersPage(
  filters: List<Pair<LibraryFilter, ToggleableState>>,
  onClick: (LibraryFilter) -> Unit
) {
  filters.forEach { (filter, state) ->
    Row(
      modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = { onClick(filter) }),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TriStateCheckbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = state,
        onClick = { onClick(filter) }
      )
      Text(filter.name)
    }
  }
}

@Composable
private fun SortPage() {
  Text("Sort")
}

@Composable
private fun DisplayPage() {
  Text("Display")
}
