package tachiyomi.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class HistoryViewModel @Inject constructor() : BaseViewModel() {

  var searchMode by mutableStateOf(false)
    private set
  var searchQuery by mutableStateOf("")
    private set

  fun openSearch() {
    searchMode = true
    searchQuery = ""
  }

  fun closeSearch() {
    searchMode = false
    searchQuery = ""
  }

  fun updateQuery(query: String) {
    searchQuery = query
  }

  fun deleteAll() {
    // TODO
  }
}