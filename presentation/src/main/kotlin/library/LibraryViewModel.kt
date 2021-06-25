/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import tachiyomi.domain.library.interactor.GetLibraryCategory
import tachiyomi.domain.library.interactor.GetUserCategories
import tachiyomi.domain.library.interactor.SetCategoriesForMangas
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
class LibraryViewModel @Inject constructor(
  private val state: LibraryState,
  private val getUserCategories: GetUserCategories,
  private val getLibraryCategory: GetLibraryCategory,
  private val setCategoriesForMangas: SetCategoriesForMangas,
  private val libraryPreferences: LibraryPreferences,
  private val updateLibraryCategory: UpdateLibraryCategory
) : BaseViewModel() {

  val categories get() = state.categories
  val selectedCategoryIndex get() = state.selectedCategoryIndex
  val selectedManga: List<Long> get() = state.selectedManga
  val sheetPage get() = state.sheetPage
  val searchMode get() = state.searchMode
  val searchQuery get() = state.searchQuery
  val sheetState get() = state.sheetState
  val pagerState get() = state.pagerState

  var lastUsedCategory by libraryPreferences.lastUsedCategory().asState()
  val filters by libraryPreferences.filters().asState()
  val sorting by libraryPreferences.sorting().asState()
  val displayMode by libraryPreferences.displayMode().asState()
  val showCategoryTabs by libraryPreferences.showCategoryTabs().asState()
  val showCountInCategory by libraryPreferences.showCountInCategory().asState()

  val selectionMode by derivedStateOf { selectedManga.isNotEmpty() }
  val selectedCategory by derivedStateOf { categories.getOrNull(selectedCategoryIndex) }

  private val loadedManga = mutableMapOf<Long, List<LibraryManga>>()

  init {
    var restoreLastUsedCategory = true
    libraryPreferences.showAllCategory().stateIn(scope)
      .flatMapLatest { showAll ->
        getUserCategories.subscribe(showAll)
          .onEach { categories ->
            val lastCategoryId = lastUsedCategory
            val index = categories.indexOfFirst { it.id == lastCategoryId }.takeIf { it != -1 } ?: 0

            state.categories = categories
            state.selectedCategoryIndex = index
            pagerState.pageCount = categories.size
            if (restoreLastUsedCategory) {
              pagerState.scrollToPage(selectedCategoryIndex)
              restoreLastUsedCategory = false
            }
          }
      }
      .launchIn(scope)
  }

  fun setSelectedPage(index: Int) {
    if (index == selectedCategoryIndex) return
    val categories = categories
    val category = categories.getOrNull(index) ?: return
    state.selectedCategoryIndex = index
    lastUsedCategory = category.id
  }

  @Composable
  fun getLibraryForCategoryIndex(categoryIndex: Int): State<List<LibraryManga>> {
    val scope = rememberCoroutineScope()
    val categoryId = categories[categoryIndex].id

    // TODO(inorichi): this approach with a shared flow doesn't look too bad but maybe there's a
    //  better way todo this in a compose world
    val unfiltered = remember(sorting, filters) {
      getLibraryCategory.subscribe(categoryId, sorting, filters)
        .shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
    }

    return remember(sorting, filters, searchQuery) {
      val query = searchQuery
      if (query.isBlank()) {
        unfiltered
      } else {
        unfiltered.map { mangas ->
          mangas.filter { it.title.contains(query, true) }
        }
      }
        .onEach { loadedManga[categoryId] = it }
        .onCompletion { loadedManga.remove(categoryId) }
    }.collectAsState(emptyList())
  }

  @Composable
  fun getLibraryColumns(): State<Int> {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val (preference, columns) = remember(isLandscape) {
      if (isLandscape) {
        libraryPreferences.columnsInLandscape()
      } else {
        libraryPreferences.columnsInPortrait()
      }.let {
        it to mutableStateOf(it.get())
      }
    }
    LaunchedEffect(isLandscape) {
      preference.changes()
        .onEach { columns.value = it }
        .launchIn(this)
    }
    return columns
  }

  fun toggleManga(manga: LibraryManga) {
    if (manga.id in selectedManga) {
      state.selectedManga.remove(manga.id)
    } else {
      state.selectedManga.add(manga.id)
    }
  }

  fun unselectAll() {
    state.selectedManga.clear()
  }

  fun selectAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedManga.toList()
    val mangaIds = mangaInCurrentCategory.map { it.id }.filter { it !in currentSelected }
    state.selectedManga.addAll(mangaIds)
  }

  fun flipAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedManga.toList()
    val (toRemove, toAdd) = mangaInCurrentCategory.map { it.id }.partition { it in currentSelected }
    state.selectedManga.removeAll(toRemove)
    state.selectedManga.addAll(toAdd)
  }

  fun openSearch() {
    state.searchMode = true
    state.searchQuery = ""
  }

  fun closeSearch() {
    state.searchMode = false
    state.searchQuery = ""
  }

  fun updateQuery(query: String) {
    state.searchQuery = query
  }

  fun updateLibrary() {
    // TODO(inorichi): For now it only updates the selected category, not the ones selected for
    //  global updates
    val categoryId = selectedCategory?.id ?: return
    updateLibraryCategory.enqueue(categoryId)
  }

  fun changeCategoriesForSelectedManga() {
    // TODO
  }

  fun toggleReadSelectedManga(read: Boolean) {
    // TODO
  }

  fun downloadSelectedManga() {
    // TODO
  }

  fun deleteDownloadsSelectedManga() {
    // TODO
  }

  fun setSheetPage(page: Int) {
    state.sheetPage = page
  }

  fun showSheet(scope: CoroutineScope) {
    scope.launch { state.sheetState.show() }
  }

  fun animatePagerScrollToPage(page: Int, scope: CoroutineScope) {
    scope.launch { pagerState.animateScrollToPage(page) }
  }

}
