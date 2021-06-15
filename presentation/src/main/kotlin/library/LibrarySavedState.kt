/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class LibrarySavedState(
  sheetPage: Int = 0,
  searchMode: Boolean = false,
  searchQuery: String = "",
) {

  var sheetPage by mutableStateOf(sheetPage)
  var searchMode by mutableStateOf(searchMode)
  var searchQuery by mutableStateOf(searchQuery)

  companion object {
    val Saver: Saver<LibrarySavedState, *> = listSaver(
      save = {
        listOf(
          it.sheetPage,
          it.searchMode,
          it.searchQuery
        )
      },
      restore = {
        @Suppress("CAST_NEVER_SUCCEEDS")
        LibrarySavedState(
          it[0] as Int,
          it[1] as Boolean,
          it[2] as String
        )
      }
    )
  }
}

@Composable
fun rememberLibrarySavedState(): LibrarySavedState {
  return rememberSaveable(saver = LibrarySavedState.Saver) {
    LibrarySavedState()
  }
}
