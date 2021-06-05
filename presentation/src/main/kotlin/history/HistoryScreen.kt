/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.R
import tachiyomi.ui.core.coil.rememberMangaCover
import tachiyomi.ui.core.components.MangaListItem
import tachiyomi.ui.core.components.MangaListItemColumn
import tachiyomi.ui.core.components.MangaListItemImage
import tachiyomi.ui.core.components.MangaListItemSubtitle
import tachiyomi.ui.core.components.MangaListItemTitle
import tachiyomi.ui.core.components.Toolbar

@Composable
fun HistoryScreen(navController: NavController) {
  Scaffold(
    topBar = {
      HistoryToolbar(
        searchMode = false,
        searchQuery = "",
        onChangeSearchQuery = {},
        onClickCloseSearch = {},
        onClickSearch = {},
        onClickDeleteAll = {}
      )
    }
  ) {
  }
}

@Composable
fun HistoryToolbar(
  searchMode: Boolean,
  searchQuery: String,
  onChangeSearchQuery: (String) -> Unit,
  onClickCloseSearch: () -> Unit,
  onClickSearch: () -> Unit,
  onClickDeleteAll: () -> Unit
) {
  when {
    searchMode -> {
      HistorySearchToolbar(
        searchQuery = searchQuery,
        onChangeSearchQuery = onChangeSearchQuery,
        onClickCloseSearch = onClickCloseSearch,
        onClickDeleteAll = onClickDeleteAll
      )
    }
    else -> {
      HistoryRegularToolbar(
        onClickSearch = onClickSearch,
        onClickDeleteAll = onClickDeleteAll
      )
    }
  }
}

@Composable
fun HistorySearchToolbar(
  searchQuery: String,
  onChangeSearchQuery: (String) -> Unit,
  onClickCloseSearch: () -> Unit,
  onClickDeleteAll: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  var expanded by remember { mutableStateOf(false) }

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
        expanded = !expanded
        focusManager.clearFocus()
      }) {
        Icon(Icons.Default.MoreVert, contentDescription = null)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(onClick = {
          onClickDeleteAll()
          expanded = false
        }) {
          Text(text = "Delete All")
        }
      }
    }
  )
}

@Composable
fun HistoryRegularToolbar(
  onClickSearch: () -> Unit,
  onClickDeleteAll: () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Toolbar(
    title = { Text(stringResource(R.string.history_label)) },
    actions = {
      IconButton(onClick = onClickSearch) {
        Icon(Icons.Default.Search, contentDescription = null)
      }
      IconButton(onClick = {
        expanded = !expanded
      }) {
        Icon(Icons.Default.MoreVert, contentDescription = null)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(onClick = {
          onClickDeleteAll()
          expanded = false
        }) {
          Text(text = "Delete All")
        }
      }
    }
  )
}

@Composable
fun HistoryItem(
  manga: Manga,
  onClickItem: (Manga) -> Unit = { /* TODO Open manga details */ },
  onClickDelete: (Manga) -> Unit = { /* TODO */ },
  onClickPlay: (Manga) -> Unit = { /* TODO */ }
) {
  MangaListItem(
    modifier = Modifier
      .clickable { onClickItem(manga) }
      .height(80.dp)
      .fillMaxWidth()
      .padding(end = 4.dp),
  ) {
    MangaListItemImage(
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(3f / 4f)
        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium),
      mangaCover = rememberMangaCover(manga)
    )
    MangaListItemColumn(
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp, end = 8.dp)
    ) {
      MangaListItemTitle(
        text = manga.title,
        maxLines = 2,
        fontWeight = FontWeight.SemiBold
      )
      MangaListItemSubtitle(
        text = "Ch. 69 - 10:59" // TODO
      )
    }
    IconButton(onClick = { onClickDelete(manga) }) {
      Icon(imageVector = Icons.Outlined.Delete, contentDescription = "")
    }
    IconButton(onClick = { onClickPlay(manga) }) {
      Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "")
    }
  }
}
