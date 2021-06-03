/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import tachiyomi.domain.library.model.CategoryWithCount
import tachiyomi.ui.R.string
import tachiyomi.ui.categories.visibleName
import tachiyomi.ui.core.components.Toolbar

@Composable
fun LibraryToolbar(
  selectedCategory: CategoryWithCount?,
  selectedManga: List<Long>,
  showCategoryTabs: Boolean,
  showCountInCategory: Boolean,
  selectionMode: Boolean,
  searchMode: Boolean,
  searchQuery: String,
  onClickSearch: () -> Unit,
  onClickFilter: () -> Unit,
  onClickRefresh: () -> Unit,
  onClickCloseSelection: () -> Unit,
  onClickCloseSearch: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickUnselectAll: () -> Unit,
  onChangeSearchQuery: (String) -> Unit
) = when {
  searchMode -> LibrarySearchToolbar(
    searchQuery = searchQuery,
    onChangeSearchQuery = onChangeSearchQuery,
    onClickCloseSearch = onClickCloseSearch,
    onClickFilter = onClickFilter
  )
  selectionMode -> LibrarySelectionToolbar(
    selectedManga = selectedManga,
    onClickCloseSelection = onClickCloseSelection,
    onClickSelectAll = onClickSelectAll,
    onClickUnselectAll = onClickUnselectAll
  )
  else -> LibraryRegularToolbar(
    selectedCategory = selectedCategory,
    showCategoryTabs = showCategoryTabs,
    showCountInCategory = showCountInCategory,
    onClickSearch = onClickSearch,
    onClickFilter = onClickFilter,
    onClickRefresh = onClickRefresh
  )
}

@Composable
private fun LibraryRegularToolbar(
  selectedCategory: CategoryWithCount?,
  showCategoryTabs: Boolean,
  showCountInCategory: Boolean,
  onClickSearch: () -> Unit,
  onClickFilter: () -> Unit,
  onClickRefresh: () -> Unit
) {
  Toolbar(
    title = {
      val text = when {
        showCategoryTabs -> stringResource(string.library_label)
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
      IconButton(onClick = onClickSearch) {
        Icon(Icons.Default.Search, contentDescription = null)
      }
      IconButton(onClick = onClickFilter) {
        Icon(Icons.Default.FilterList, contentDescription = null)
      }
      IconButton(onClick = onClickRefresh) {
        Icon(Icons.Default.Refresh, contentDescription = null)
      }
    }
  )
}

@Composable
private fun LibrarySelectionToolbar(
  selectedManga: List<Long>,
  onClickCloseSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickUnselectAll: () -> Unit
) {
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

@Composable
private fun LibrarySearchToolbar(
  searchQuery: String,
  onChangeSearchQuery: (String) -> Unit,
  onClickCloseSearch: () -> Unit,
  onClickFilter: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  Toolbar(
    title = {
      BasicTextField(
        searchQuery,
        onChangeSearchQuery,
        modifier = Modifier.focusRequester(focusRequester),
        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
        cursorBrush = SolidColor(LocalContentColor.current),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
      )
    },
    navigationIcon = {
      IconButton(onClick = onClickCloseSearch) {
        Icon(Icons.Default.ArrowBack, contentDescription = null)
      }
    },
    actions = {
      IconButton(onClick = { onChangeSearchQuery("") }) {
        Icon(Icons.Default.Close, contentDescription = null)
      }
      IconButton(onClick = {
        onClickFilter()
        focusManager.clearFocus()
      }) {
        Icon(Icons.Default.FilterList, contentDescription = null)
      }
    }
  )
  LaunchedEffect(focusRequester) {
    focusRequester.requestFocus()
  }
  BackHandler(onBack = onClickCloseSearch)
}
