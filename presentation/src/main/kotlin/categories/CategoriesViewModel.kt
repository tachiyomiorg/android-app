/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tachiyomi.domain.library.interactor.CreateCategoryWithName
import tachiyomi.domain.library.interactor.DeleteCategories
import tachiyomi.domain.library.interactor.GetCategories
import tachiyomi.domain.library.interactor.RenameCategory
import tachiyomi.domain.library.interactor.ReorderCategory
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
  private val getCategories: GetCategories,
  private val createCategoryWithName: CreateCategoryWithName,
  private val renameCategory: RenameCategory,
  private val reorderCategory: ReorderCategory,
  private val deleteCategory: DeleteCategories
) : BaseViewModel() {

  var categories by mutableStateOf(emptyList<Category>())
    private set
  var dialog by mutableStateOf<Dialog?>(null)
    private set

  init {
    getCategories.subscribe()
      .onEach { categories = it.filterNot(Category::isSystemCategory) }
      .launchIn(scope)
  }

  fun createCategory(name: String) {
    scope.launch {
      createCategoryWithName.await(name)
    }
  }

  fun renameCategory(category: Category, newName: String) {
    scope.launch {
      renameCategory.await(category, newName)
    }
  }

  fun deleteCategory(category: Category) {
    scope.launch {
      deleteCategory.await(category.id)
    }
  }

  fun moveUp(category: Category) {
    scope.launch {
      reorderCategory.await(category, category.order - 1)
    }
  }

  fun moveDown(category: Category) {
    scope.launch {
      reorderCategory.await(category, category.order + 1)
    }
  }

  fun showCreateDialog() {
    dialog = Dialog.Create
  }

  fun showRenameDialog(category: Category) {
    dialog = Dialog.Rename(category)
  }

  fun showDeleteDialog(category: Category) {
    dialog = Dialog.Delete(category)
  }

  fun dismissDialog() {
    dialog = null
  }

  sealed class Dialog {
    object Create : Dialog()
    data class Rename(val category: Category) : Dialog()
    data class Delete(val category: Category) : Dialog()
  }

}
