/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import tachiyomi.core.util.Optional
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.repository.CategoryRepository
import javax.inject.Inject

class RenameCategory @Inject constructor(
  private val categoryRepository: CategoryRepository) {

  suspend fun await(categoryId: Long, newName: String) = withContext(NonCancellable) f@{
    if (newName.isBlank()) {
      return@f Result.EmptyNameError
    }

    val categories = withContext(Dispatchers.IO) { categoryRepository.findAll() }

    val category = categories.find { it.id == categoryId }
      ?: return@f Result.NotFoundError

    if (category.isSystemCategory) {
      return@f Result.CantBeRenamedError
    }

    // Allow to rename if it doesn't exist or it's the same category
    val categoryWithSameName = categories.find { it.name == newName }
    if (categoryWithSameName != null && categoryWithSameName.id != categoryId) {
      return@f Result.NameAlreadyExistsError
    }

    val update = CategoryUpdate(
      id = categoryId,
      name = Optional.of(newName)
    )

    try {
      withContext(Dispatchers.IO) { categoryRepository.savePartial(update) }
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }

    Result.Success
  }

  suspend fun await(category: Category, newName: String): Result {
    return await(category.id, newName)
  }

  sealed class Result {
    object Success : Result()
    object EmptyNameError : Result()
    object NameAlreadyExistsError : Result()
    object CantBeRenamedError : Result()
    object NotFoundError : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
