/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.LibrarySorting

data class LibraryState(
  val categories: List<Category> = emptyList(),
  val selectedCategory: Category? = null,
  val library: List<LibraryManga> = emptyList(),
  val filters: List<LibraryFilter> = emptyList(),
  val sorting: LibrarySorting = LibrarySorting(LibrarySort.Title, true),
  val selectedManga: Set<Long> = emptySet(),
  val showUpdatingCategory: Boolean = false,
  val showQuickCategories: Boolean = false,
  val sheetVisible: Boolean = false
)

sealed class Action {

  object Init : Action()

  data class SetFilters(val filters: List<LibraryFilter>) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(filters = filters)
  }

  data class SetSorting(val sort: LibrarySorting) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(sorting = sort)
  }

  data class LibraryUpdate(val library: List<LibraryManga>) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(library = library)
  }

  data class CategoriesUpdate(val categories: List<Category>) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(categories = categories)
  }

  data class SetSelectedCategory(val category: Category?) : Action() {
    override fun reduce(state: LibraryState): LibraryState {
      val selectedCategory = state.categories.find { it.id == category?.id }
      return state.copy(selectedCategory = selectedCategory)
    }
  }

  data class ToggleSelection(val manga: LibraryManga) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(selectedManga = if (manga.id in state.selectedManga) {
        state.selectedManga - manga.id
      } else {
        state.selectedManga + manga.id
      })
  }

  object ToggleQuickCategories : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(showQuickCategories = !state.showQuickCategories)
  }

  data class SetSheetVisibility(val visible: Boolean) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(sheetVisible = visible)
  }

  object UnselectMangas : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(selectedManga = emptySet())
  }

  object UpdateCategory : Action()

  data class ShowUpdatingCategory(val loading: Boolean) : Action() {
    override fun reduce(state: LibraryState) =
      state.copy(showUpdatingCategory = loading)
  }

  open fun reduce(state: LibraryState) = state

}

