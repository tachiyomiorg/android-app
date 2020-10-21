/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.service

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import tachiyomi.domain.library.service.LibraryUpdateScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LibraryUpdateSchedulerImpl @Inject constructor() : LibraryUpdateScheduler {

  private val workManager = WorkManager.getInstance()

  override fun schedule(categoryId: Long, timeInHours: Int) {
    val work = OneTimeWorkRequest.Builder(LibraryUpdaterWorker::class.java)
      .setInitialDelay(timeInHours.toLong(), TimeUnit.HOURS)
      .addTag(LIBRARY_UPDATER_TAG)
      .addTag(getCategoryTag(categoryId))
      .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
      .setInputData(Data.Builder()
        .putLong(LibraryUpdaterWorker.CATEGORY_KEY, categoryId)
        .build()
      )
      .build()

    val workName = getUniqueWorkName(categoryId)
    workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, work)
  }

  override fun unschedule(categoryId: Long) {
    workManager.cancelUniqueWork(getUniqueWorkName(categoryId))
  }

  override fun unscheduleAll(categoryId: Long) {
    workManager.cancelAllWorkByTag(getCategoryTag(categoryId))
  }

  private fun getUniqueWorkName(categoryId: Long): String {
    return "library_chapters_$categoryId"
  }

  private fun getCategoryTag(categoryId: Long): String {
    return "library_category_$categoryId"
  }

  private companion object {
    const val LIBRARY_UPDATER_TAG = "library_updater"
  }

}
