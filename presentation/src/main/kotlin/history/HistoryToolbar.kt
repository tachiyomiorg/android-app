package tachiyomi.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import tachiyomi.ui.R
import tachiyomi.ui.core.components.SearchField
import tachiyomi.ui.core.components.Toolbar

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
      SearchField(
        modifier = Modifier.focusRequester(focusRequester),
        query = searchQuery,
        onChangeQuery = onChangeSearchQuery,
        onDone = { focusManager.clearFocus() }
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
          Text(text = stringResource(id = R.string.clear_history))
        }
      }
    }
  )
  LaunchedEffect(focusRequester) {
    focusRequester.requestFocus()
  }
  BackHandler(onBack = onClickCloseSearch)
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
          Text(text = stringResource(id = R.string.clear_history))
        }
      }
    }
  )
}