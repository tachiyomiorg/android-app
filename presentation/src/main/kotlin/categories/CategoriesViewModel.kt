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
import tachiyomi.domain.library.interactor.GetCategories
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
  private val getCategories: GetCategories,
  private val createCategoryWithName: CreateCategoryWithName
) : BaseViewModel() {

  var categories by mutableStateOf(emptyList<Category>())
    private set
  var showCreateDialog by mutableStateOf(false)

  init {
    getCategories.subscribe()
      .onEach { categories = it.filter { !it.isSystemCategory } }
      .launchIn(scope)
  }

  fun createCategory(name: String) {
    scope.launch {
      createCategoryWithName.await(name)
    }
  }

}
