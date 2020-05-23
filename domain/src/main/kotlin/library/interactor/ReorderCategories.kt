/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.service.CategoryRepository
import javax.inject.Inject

class ReorderCategory @Inject internal constructor(
  private val categoryRepository: CategoryRepository
) {

  suspend fun await(categoryId: Long, newPosition: Int) = withContext(NonCancellable) f@{
    val categories = categoryRepository.findAll()

    // If nothing changed, return
    val currPosition = categories.indexOfFirst { it.id == categoryId }
    if (currPosition == newPosition || currPosition == -1) {
      return@f Result.Unchanged
    }

    val reorderedCategories = categories.toMutableList()
    val movedCategory = reorderedCategories.removeAt(currPosition)
    reorderedCategories.add(newPosition, movedCategory)

    val updates = reorderedCategories.mapIndexed { index, category ->
      CategoryUpdate(
        id = category.id,
        order = index
      )
    }

    try {
      categoryRepository.updatePartial(updates)
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }
    Result.Success
  }

  suspend fun await(category: Category, newPosition: Int): Result {
    return await(category.id, newPosition)
  }

  sealed class Result {
    object Success : Result()
    object Unchanged : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
