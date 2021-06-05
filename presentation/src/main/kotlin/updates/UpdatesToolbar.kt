package tachiyomi.ui.updates

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tachiyomi.ui.R
import tachiyomi.ui.core.components.Toolbar

@Composable
fun UpdatesToolbar(
  selectedManga: List<Long>,
  selectionMode: Boolean,
  onClickCancelSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickFlipSelection: () -> Unit,
  onClickRefresh: () -> Unit
) {
  when {
    selectionMode -> {
      UpdatesSelectionToolbar(
        selectedManga = selectedManga,
        onClickCancelSelection = onClickCancelSelection,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickFlipSelection
      )
    }
    else -> {
      UpdatesRegularToolbar(
        onClickRefresh = onClickRefresh
      )
    }
  }
}

@Composable
private fun UpdatesSelectionToolbar(
  selectedManga: List<Long>,
  onClickCancelSelection: () -> Unit,
  onClickSelectAll: () -> Unit,
  onClickInvertSelection: () -> Unit
) {
  Toolbar(
    title = { Text("${selectedManga.size}") },
    navigationIcon = {
      IconButton(onClick = onClickCancelSelection) {
        Icon(Icons.Default.Close, contentDescription = null)
      }
    },
    actions = {
      IconButton(onClick = onClickSelectAll) {
        Icon(Icons.Default.SelectAll, contentDescription = null)
      }
      IconButton(onClick = onClickInvertSelection) {
        Icon(Icons.Default.FlipToBack, contentDescription = null)
      }
    }
  )
}

@Composable
fun UpdatesRegularToolbar(onClickRefresh: () -> Unit) {
  Toolbar(
    title = { Text(stringResource(R.string.updates_label)) },
    actions = {
      IconButton(onClick = onClickRefresh) {
        Icon(Icons.Default.Refresh, contentDescription = null)
      }
    }
  )
}