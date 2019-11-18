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
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdateScheduler
import javax.inject.Inject

class DeleteCategories @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val libraryScheduler: LibraryUpdateScheduler) {

  suspend fun await(categoryIds: Collection<Long>) = withContext(NonCancellable) f@{
    val safeCategoryIds = categoryIds.filter { it > 0 }
    if (safeCategoryIds.isEmpty()) {
      return@f Result.NothingToDelete
    }

    try {
      withContext(Dispatchers.IO) { categoryRepository.delete(safeCategoryIds) }
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }

    libraryPreferences.defaultCategory().run {
      if (get() in safeCategoryIds) {
        delete()
      }
    }
    for (id in safeCategoryIds) {
      libraryScheduler.unscheduleAll(id)
    }

    Result.Success
  }

  sealed class Result {
    object Success : Result()
    object NothingToDelete : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
