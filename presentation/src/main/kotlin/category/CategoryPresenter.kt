/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.category

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.library.interactor.CreateCategoryWithName
import tachiyomi.domain.library.interactor.DeleteCategories
import tachiyomi.domain.library.interactor.GetCategories
import tachiyomi.domain.library.interactor.RenameCategory
import tachiyomi.domain.library.interactor.ReorderCategory
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.EmptySideEffect
import tachiyomi.ui.presenter.SingleSideEffect
import javax.inject.Inject

class CategoryPresenter @Inject constructor(
  private val getCategories: GetCategories,
  private val createCategoryWithName: CreateCategoryWithName,
  private val deleteCategories: DeleteCategories,
  private val renameCategory: RenameCategory,
  private val reorderCategory: ReorderCategory
) : BasePresenter() {

  private val initialState = getInitialViewState()

  /**
   * Behavior subject containing the last emitted view state.
   */
  val state = ConflatedBroadcastChannel(initialState)

  private val store = scope.createStore(
    name = "Category presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.offer(it) }
    loadCategories()
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    // Category creation side effect
    sideEffects += EmptySideEffect("Create a category") f@{ _, action ->
      if (action !is Action.CreateCategory) return@f null
      suspend { createCategoryWithName.await(action.name) }
    }

    // Category deletion side effect
    sideEffects += SingleSideEffect("Delete categories") f@{ _, action ->
      if (action !is Action.DeleteCategories) return@f null
      suspend {
        val result = deleteCategories.await(action.categoryIds)
        when (result) {
          // Unselect categories when the operation succeeds
          DeleteCategories.Result.Success -> Action.UnselectCategories
          else -> null
        }
      }
    }

    // Rename categories side effect
    sideEffects += SingleSideEffect("Rename a category") f@{ _, action ->
      if (action !is Action.RenameCategory) return@f null
      suspend {
        val result = renameCategory.await(action.categoryId, action.newName)
        when (result) {
          // Unselect categories when the operation succeeds
          RenameCategory.Result.Success -> Action.UnselectCategories
          else -> null
        }
      }
    }

    // Reorder categories side effect
    sideEffects += EmptySideEffect("Reorder a category") f@{ _, action ->
      if (action !is Action.ReorderCategory) return@f null
      suspend { reorderCategory.await(action.category, action.newPosition) }
    }

    return sideEffects
  }

  private fun loadCategories() {
    scope.launch(Dispatchers.Default) {
      getCategories.subscribe().collect {
        store.dispatch(Action.CategoriesUpdate(it))
      }
    }
  }

  fun createCategory(name: String) {
    store.dispatch(Action.CreateCategory(name))
  }

  fun deleteCategories(categories: Set<Long>) {
    store.dispatch(Action.DeleteCategories(categories))
  }

  fun renameCategory(categoryId: Long, newName: String) {
    store.dispatch(Action.RenameCategory(categoryId, newName))
  }

  fun reorderCategory(category: Category, newPosition: Int) {
    store.dispatch(Action.ReorderCategory(category, newPosition))
  }

  fun toggleCategorySelection(category: Category) {
    store.dispatch(Action.ToggleCategorySelection(category))
  }

  fun unselectCategories() {
    store.dispatch(Action.UnselectCategories)
  }

  fun getCategory(id: Long): Category? {
    return state.value?.categories?.find { it.id == id }
  }

}
