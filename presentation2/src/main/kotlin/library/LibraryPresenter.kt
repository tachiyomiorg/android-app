/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.Composable
import androidx.compose.State
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tachiyomi.domain.library.interactor.GetLibraryCategory
import tachiyomi.domain.library.interactor.GetUserCategories
import tachiyomi.domain.library.interactor.SetCategoriesForMangas
import tachiyomi.domain.library.interactor.SetCategoryFilters
import tachiyomi.domain.library.interactor.SetCategorySorting
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.LibrarySorting
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.collectAsState
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.EmptySideEffect
import tachiyomi.ui.presenter.FlowSwitchSideEffect
import timber.log.Timber
import timber.log.warn
import javax.inject.Inject

class LibraryPresenter @Inject constructor(
  private val getUserCategories: GetUserCategories,
  private val getLibraryCategory: GetLibraryCategory,
  private val setCategoriesForMangas: SetCategoriesForMangas,
  private val libraryPreferences: LibraryPreferences,
  private val setCategoryFilters: SetCategoryFilters,
  private val setCategorySorting: SetCategorySorting,
  private val updateLibraryCategory: UpdateLibraryCategory
) : BasePresenter() {

  private val lastSortPreference = libraryPreferences.lastSorting()

  private val filtersPreference = libraryPreferences.filters()

  private val lastUsedCategoryPreference = libraryPreferences.lastUsedCategory()

  private val quickCategoriesPreference = libraryPreferences.quickCategories()

  private val initialViewState = getInitialViewState()

  val state = ConflatedBroadcastChannel(initialViewState)

  private val store = scope.createStore(
    name = "Library presenter",
    initialState = initialViewState,
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    Timber.warn { "Hi!" }
    store.dispatch(Action.Init)
  }

  override fun destroy() {
    super.destroy()
    Timber.warn { "Bye!" }
  }

  @Composable
  fun state(): State<LibraryState> {
    return store.asFlow().collectAsState(initialViewState)
  }

  private fun getInitialViewState(): LibraryState {
    return LibraryState(
      categories = emptyList(),
      library = emptyList(),
      filters = emptyList(),
      sorting = lastSortPreference.get(),
      showQuickCategories = quickCategoriesPreference.get()
    )
  }

  private fun getSideEffects(): List<SideEffect<LibraryState, Action>> {
    val sideEffects = mutableListOf<SideEffect<LibraryState, Action>>()

    sideEffects += FlowSwitchSideEffect("Subscribe user categories") f@{ _, action ->
      if (action !is Action.Init) return@f null

      suspend {
        getUserCategories.subscribe(true).flatMapConcat { categories ->
          val lastCategoryId = lastUsedCategoryPreference.get()
          val category = categories.find { it.id == lastCategoryId } ?: categories.firstOrNull()

          flowOf(Action.CategoriesUpdate(categories), Action.SetSelectedCategory(category))
        }
      }
    }

    var subscribedCategory: Category? = null
    var subscribedSorting: LibrarySorting? = null
    sideEffects += FlowSwitchSideEffect("Subscribe selected category") f@{ stateFn, action ->
      if (action !is Action.SetSelectedCategory && action !is Action.SetSorting) return@f null
      val state = stateFn()
      val category = state.selectedCategory
      val sorting = state.sorting
      if (subscribedCategory == category && subscribedSorting == sorting) return@f null
      subscribedCategory = category
      subscribedSorting = sorting

      suspend {
        if (category == null) {
          flowOf(Action.LibraryUpdate(emptyList()))
        } else {
          lastUsedCategoryPreference.set(category.id)
          //val sorting = if (category.useOwnFilters) category.sort else lastSortPreference.get()
          val filters = state.filters

          getLibraryCategory.subscribe(category.id, sorting).map { Action.LibraryUpdate(it) }
        }
      }
    }

    sideEffects += FlowSwitchSideEffect("Update selected category") f@{ stateFn, action ->
      if (action !is Action.UpdateCategory) return@f null
      val categoryId = stateFn().selectedCategory?.id ?: return@f null

      suspend {
        GlobalScope.launch {
          updateLibraryCategory.enqueue(categoryId).awaitWork()
        }

        flow {
          emit(Action.ShowUpdatingCategory(true))
          delay(1000)
          emit(Action.ShowUpdatingCategory(false))
        }
      }
    }

    sideEffects += EmptySideEffect("Update filters and sorting") f@{ stateFn, action ->
      when (action) {
        is Action.SetFilters -> suspend {
          stateFn().selectedCategory?.let { setCategoryFilters.await(action.filters) }
        }
        is Action.SetSorting -> suspend {
          stateFn().selectedCategory?.let { setCategorySorting.await(action.sort) }
        }
        else -> null
      }
    }

    sideEffects += EmptySideEffect("Toggle quick categories") f@{ stateFn, action ->
      if (action !is Action.ToggleQuickCategories) return@f null
      suspend { quickCategoriesPreference.set(stateFn().showQuickCategories) }
    }

    return sideEffects
  }

  fun setSelectedCategory(position: Int) {
    val category = state.value.categories.getOrNull(position) ?: return
    setSelectedCategory(category)
  }

  fun setSelectedCategory(category: Category) {
    store.dispatch(Action.SetSelectedCategory(category))
  }

  fun updateSelectedCategory() {
    store.dispatch(Action.UpdateCategory)
  }

  fun setSelectedSort(sort: LibrarySort) {
    val sorting = state.value.sorting
    if (sort == sorting.type) return

    store.dispatch(Action.SetSorting(LibrarySorting(sort, sorting.isAscending)))
  }

  fun toggleMangaSelection(manga: LibraryManga) {
    store.dispatch(Action.ToggleSelection(manga))
  }

  fun unselectMangas() {
    store.dispatch(Action.UnselectMangas)
  }

  fun showSheet() {
    store.dispatch(Action.SetSheetVisibility(true))
  }

  fun hideSheet() {
    store.dispatch(Action.SetSheetVisibility(false))
  }

  fun setCategoriesForMangas(categoryIds: Collection<Long>, mangaIds: Collection<Long>) {
    scope.launch {
      val result = setCategoriesForMangas.await(categoryIds, mangaIds)
      if (result is SetCategoriesForMangas.Result.Success) {
        unselectMangas()
      }
    }
  }

  fun toggleQuickCategories() {
    store.dispatch(Action.ToggleQuickCategories)
  }

  fun getCategories(): List<Category> {
    return state.value.categories
  }

}
