/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.library.service.LibraryUpdateScheduler
import tachiyomi.domain.library.service.LibraryUpdater
import javax.inject.Inject

class SetCategoryUpdateInterval @Inject internal constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryScheduler: LibraryUpdateScheduler
) {

  suspend fun await(categoryId: Long, intervalInHours: Int): Result {
    val update = CategoryUpdate(
      id = categoryId,
      updateInterval = intervalInHours
    )
    return try {
      categoryRepository.updatePartial(update)

      if (intervalInHours > 0) {
        libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters, intervalInHours)
      } else {
        libraryScheduler.unschedule(categoryId, LibraryUpdater.Target.Chapters)
      }
      Result.Success
    } catch (e: Exception) {
      Result.InternalError(e)
    }
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
