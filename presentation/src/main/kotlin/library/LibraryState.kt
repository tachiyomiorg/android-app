/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import tachiyomi.domain.library.model.CategoryWithCount

class LibraryState(
  sheetPage: Int = 0,
  searchMode: Boolean = false,
  searchQuery: String = "",
) {

  var sheetPage by mutableStateOf(sheetPage)
  var searchMode by mutableStateOf(searchMode)
  var searchQuery by mutableStateOf(searchQuery)
  var categories by mutableStateOf(emptyList<CategoryWithCount>(), referentialEqualityPolicy())
  var selectedCategoryIndex by mutableStateOf(0)
  var selectedManga = mutableStateListOf<Long>()

  companion object {
    val Saver: Saver<LibraryState, Any> = listSaver(
      save = {
        listOf(
          it.sheetPage,
          it.searchMode,
          it.searchQuery
        )
      },
      restore = {
        @Suppress("CAST_NEVER_SUCCEEDS")
        LibraryState(
          it[0] as Int,
          it[1] as Boolean,
          it[2] as String
        )
      }
    )
  }
}
