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
import org.tinylog.kotlin.Logger
import tachiyomi.domain.library.model.LibrarySorting
import tachiyomi.domain.library.service.LibraryPreferences
import javax.inject.Inject

class SetCategorySorting @Inject constructor(
  private val libraryPreferences: LibraryPreferences
) {

  suspend fun await(sorting: LibrarySorting) = withContext(NonCancellable) {
    try {
      libraryPreferences.lastSorting().set(sorting)
      Result.Success
    } catch (e: Exception) {
      Logger.warn(e)
      Result.InternalError(e)
    }
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Exception) : Result()
  }

}
