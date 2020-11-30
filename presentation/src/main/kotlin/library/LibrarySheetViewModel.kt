/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tachiyomi.domain.library.model.DisplayMode
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class LibrarySheetViewModel @Inject constructor(
  libraryPreferences: LibraryPreferences
) : BaseViewModel() {
  var selectedPage by mutableStateOf(0)

  var filters by libraryPreferences.filters(includeAll = true).asState()
    private set

  var sorting by libraryPreferences.sorting().asState()
    private set

  var displayMode by libraryPreferences.displayMode().asState()
    private set

  var downloadBadges by libraryPreferences.downloadBadges().asState()
    private set

  var unreadBadges by libraryPreferences.unreadBadges().asState()
    private set

  var showCategoryTabs by libraryPreferences.showCategoryTabs().asState()
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

  fun changeDisplayMode(mode: DisplayMode) {
    displayMode = mode
  }

  fun toggleDownloadBadges() {
    downloadBadges = !downloadBadges
  }

  fun toggleUnreadBadges() {
    unreadBadges = !unreadBadges
  }

  fun toggleShowCategoryTabs() {
    showCategoryTabs = !showCategoryTabs
  }

  fun toggleShowAllCategory() {
    showAllCategory = !showAllCategory
  }
}
