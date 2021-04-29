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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
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
  var selectedManga by mutableStateOf(emptySet<Long>())
    private set
  var showUpdatingCategory by mutableStateOf(false)
    private set
  var sheetVisible by mutableStateOf(false)

  val filters by libraryPreferences.filters().asState()
  val sorting by libraryPreferences.sorting().asState()
  val displayMode by libraryPreferences.displayMode().asState()
  val showCategoryTabs by libraryPreferences.showCategoryTabs().asState()
  val showCountInCategory by libraryPreferences.showCountInCategory().asState()

  val selectedCategory get() = categories.getOrNull(selectedCategoryIndex)

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
    }.collectAsState(emptyList())
  }

//  private fun getSideEffects(): List<SideEffect<LibraryState, Action>> {
//    val sideEffects = mutableListOf<SideEffect<LibraryState, Action>>()
//
//    sideEffects += FlowSwitchSideEffect("Subscribe user categories") f@{ _, action ->
//      if (action !is Action.Init) return@f null
//
//      suspend {
//        getUserCategories.subscribe(true).flatMapConcat { categories ->
//          val lastCategoryId = lastUsedCategoryPreference.get()
//          val category = categories.find { it.id == lastCategoryId } ?: categories.firstOrNull()
//
//          flowOf(Action.CategoriesUpdate(categories), Action.SetSelectedCategory(category))
//        }
//      }
//    }
//
//    var subscribedCategory: Category? = null
//    var subscribedSorting: LibrarySorting? = null
//    sideEffects += FlowSwitchSideEffect("Subscribe selected category") f@{ stateFn, action ->
//      if (action !is Action.SetSelectedCategory && action !is Action.SetSorting) return@f null
//      val state = stateFn()
//      val category = state.selectedCategory
//      val sorting = state.sorting
//      if (subscribedCategory == category && subscribedSorting == sorting) return@f null
//      subscribedCategory = category
//      subscribedSorting = sorting
//
//      suspend {
//        if (category == null) {
//          flowOf(Action.LibraryUpdate(emptyList()))
//        } else {
//          lastUsedCategoryPreference.set(category.id)
//          //val sorting = if (category.useOwnFilters) category.sort else lastSortPreference.get()
//          val filters = state.filters
//
//          getLibraryCategory.subscribe(category.id, sorting).map { Action.LibraryUpdate(it) }
//        }
//      }
//    }
//
//    sideEffects += FlowSwitchSideEffect("Update selected category") f@{ stateFn, action ->
//      if (action !is Action.UpdateCategory) return@f null
//      val categoryId = stateFn().selectedCategory?.id ?: return@f null
//
//      suspend {
//        GlobalScope.launch {
//          updateLibraryCategory.enqueue(categoryId)
//          //.awaitWork()
//        }
//
//        flow {
//          emit(Action.ShowUpdatingCategory(true))
//          delay(1000)
//          emit(Action.ShowUpdatingCategory(false))
//        }
//      }
//    }
//
//    sideEffects += EmptySideEffect("Update filters and sorting") f@{ stateFn, action ->
//      when (action) {
//        is Action.SetFilters -> suspend {
//          stateFn().selectedCategory?.let { setCategoryFilters.await(action.filters) }
//        }
//        is Action.SetSorting -> suspend {
//          stateFn().selectedCategory?.let { setCategorySorting.await(action.sort) }
//        }
//        else -> null
//      }
//    }
//
//    sideEffects += EmptySideEffect("Toggle quick categories") f@{ stateFn, action ->
//      if (action !is Action.ToggleQuickCategories) return@f null
//      suspend { quickCategoriesPreference.set(stateFn().showQuickCategories) }
//    }
//
//    return sideEffects
//  }
//
//  fun setSelectedCategory(position: Int) {
//    val category = state.value.categories.getOrNull(position) ?: return
//    setSelectedCategory(category)
//  }
//
//  fun setSelectedCategory(category: Category) {
//    store.dispatch(Action.SetSelectedCategory(category))
//  }
//
//  fun updateSelectedCategory() {
//    store.dispatch(Action.UpdateCategory)
//  }
//
//  fun setSelectedSort(sort: LibrarySort) {
//    val sorting = state.value.sorting
//    if (sort == sorting.type) return
//
//    store.dispatch(Action.SetSorting(LibrarySorting(sort, sorting.isAscending)))
//  }
//
//  fun toggleMangaSelection(manga: LibraryManga) {
//    store.dispatch(Action.ToggleSelection(manga))
//  }
//
//  fun unselectMangas() {
//    store.dispatch(Action.UnselectMangas)
//  }
//
//  fun showSheet() {
//    store.dispatch(Action.SetSheetVisibility(true))
//  }
//
//  fun hideSheet() {
//    store.dispatch(Action.SetSheetVisibility(false))
//  }
//
//  fun setCategoriesForMangas(categoryIds: Collection<Long>, mangaIds: Collection<Long>) {
//    scope.launch {
//      val result = setCategoriesForMangas.await(categoryIds, mangaIds)
//      if (result is SetCategoriesForMangas.Result.Success) {
//        unselectMangas()
//      }
//    }
//  }
//
//  fun toggleQuickCategories() {
//    store.dispatch(Action.ToggleQuickCategories)
//  }

}
