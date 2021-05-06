/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import tachiyomi.domain.library.interactor.GetLibraryCategory
import tachiyomi.domain.library.interactor.GetUserCategories
import tachiyomi.domain.library.interactor.SetCategoriesForMangas
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import tachiyomi.domain.library.model.CategoryWithCount
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
  private val getUserCategories: GetUserCategories,
  private val getLibraryCategory: GetLibraryCategory,
  private val setCategoriesForMangas: SetCategoriesForMangas,
  private val libraryPreferences: LibraryPreferences,
  private val updateLibraryCategory: UpdateLibraryCategory
) : BaseViewModel() {

  private val lastUsedCategoryPreference = libraryPreferences.lastUsedCategory()

  var categories by mutableStateOf(emptyList<CategoryWithCount>())
    private set
  var selectedCategoryIndex by mutableStateOf(0)
    private set
  var library by mutableStateOf(emptyList<LibraryManga>())
    private set
  val selectedManga = mutableStateListOf<Long>()
  var showUpdatingCategory by mutableStateOf(false)
    private set
  var sheetVisible by mutableStateOf(false)

  val filters by libraryPreferences.filters().asState()
  val sorting by libraryPreferences.sorting().asState()
  val displayMode by libraryPreferences.displayMode().asState()
  val showCategoryTabs by libraryPreferences.showCategoryTabs().asState()
  val showCountInCategory by libraryPreferences.showCountInCategory().asState()

  val selectedCategory get() = categories.getOrNull(selectedCategoryIndex)

  private val loadedManga = mutableMapOf<Long, List<LibraryManga>>()

  init {
    libraryPreferences.showAllCategory().stateIn(scope)
      .flatMapLatest { showAll ->
        getUserCategories.subscribe(showAll)
          .onEach { categories ->
            val lastCategoryId = lastUsedCategoryPreference.get()
            val index = categories.indexOfFirst { it.id == lastCategoryId }.takeIf { it != -1 } ?: 0

            this.categories = categories
            this.selectedCategoryIndex = index
          }
      }
      .launchIn(scope)
  }

  fun setSelectedPage(index: Int) {
    if (index == selectedCategoryIndex) return
    val categories = categories
    val category = categories.getOrNull(index) ?: return
    selectedCategoryIndex = index
    lastUsedCategoryPreference.set(category.id)
  }

  @Composable
  fun getLibraryForCategoryIndex(categoryIndex: Int): State<List<LibraryManga>> {
    val categoryId = categories[categoryIndex].id
    return remember(categoryId, sorting, filters) {
      getLibraryCategory.subscribe(categoryId, sorting, filters)
        .onEach { loadedManga[categoryId] = it }
        .onCompletion { loadedManga.remove(categoryId) }
    }.collectAsState(emptyList())
  }

  fun toggleManga(manga: LibraryManga) {
    if (manga.id in selectedManga) {
      selectedManga.remove(manga.id)
    } else {
      selectedManga.add(manga.id)
    }
  }

  fun unselectAll() {
    selectedManga.clear()
  }

  fun selectAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedManga.toList()
    val mangaIds = mangaInCurrentCategory.map { it.id }.filter { it !in currentSelected }
    selectedManga.addAll(mangaIds)
  }

  fun flipAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedManga.toList()
    val (toRemove, toAdd) = mangaInCurrentCategory.map { it.id }.partition { it in currentSelected }
    selectedManga.removeAll(toRemove)
    selectedManga.addAll(toAdd)
  }

}
