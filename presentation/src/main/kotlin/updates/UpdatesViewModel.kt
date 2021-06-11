package tachiyomi.ui.updates

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class UpdatesViewModel @Inject constructor() : BaseViewModel() {

  var selectedManga = mutableStateListOf<Long>()
    private set
  val selectionMode by derivedStateOf{ selectedManga.isNotEmpty() }

  fun unselectAll() {
    selectedManga.clear()
  }

  fun selectAll() {
    // TODO
  }

  fun flipSelection() {
    // TODO
  }

  fun updateLibrary() {
    // TODO
  }
}