/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tachiyomi.core.di.AppScope
import tachiyomi.core.log.Logger
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import toothpick.ktp.delegate.inject

class LibraryUpdaterWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  private val updater by inject<UpdateLibraryCategory>()

  init {
    AppScope.inject(this)
  }

  private val categoryId = params.inputData.getLong(CATEGORY_KEY, -1)

  override suspend fun doWork(): Result {
    Logger.debug("Starting scheduled update for category $categoryId")
    if (categoryId == -1L) {
      return Result.failure()
    }

    withContext(Dispatchers.IO) {
      updater.enqueue(categoryId).awaitWork()
    }

    return Result.success()
  }

  companion object {
    const val CATEGORY_KEY = "category_id"
  }

}
