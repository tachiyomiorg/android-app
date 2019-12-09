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
import tachiyomi.domain.library.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryWithName @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  suspend fun await(name: String): Result = withContext(NonCancellable) f@{
    if (name.isBlank()) {
      return@f Result.EmptyCategoryNameError
    }

    val categories = categoryRepository.findAll()
    if (categories.any { name.equals(it.name, ignoreCase = true) }) {
      return@f Result.CategoryAlreadyExistsError(name)
    }

    val nextOrder = categories.maxBy { it.order }?.order?.plus(1) ?: 0
    val newCategory = Category(
      id = 0,
      name = name,
      order = nextOrder
    )

    try {
      categoryRepository.insert(newCategory)
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }

    Result.Success
  }

  sealed class Result {
    object Success : Result()
    object EmptyCategoryNameError : Result()
    data class CategoryAlreadyExistsError(val name: String) : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
