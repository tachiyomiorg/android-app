/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.library.service.LibraryUpdateScheduler
import tachiyomi.domain.library.service.LibraryUpdater
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import javax.inject.Inject

class UpdateLibraryCategory @Inject constructor(
  private val getLibraryCategory: GetLibraryCategory,
  private val syncChaptersFromSource: SyncChaptersFromSource,
  //private val notifier: LibraryUpdaterNotification,
  private val libraryUpdater: LibraryUpdater,
  private val categoryRepository: CategoryRepository,
  private val libraryScheduler: LibraryUpdateScheduler
) {

  suspend fun enqueue(categoryId: Long): LibraryUpdater.QueueResult {
    val operation: suspend (Job) -> Any = { job ->
      Logger.debug { "Updating category $categoryId ${Thread.currentThread()}" }
      //notifier.start()

      job.invokeOnCompletion {
        Logger.debug { "Finished updating category $categoryId ${Thread.currentThread()}" }
        //notifier.end()
      }

      val mangas = getLibraryCategory.await(categoryId)
      val total = mangas.size

      for ((progress, manga) in mangas.withIndex()) {
        if (!job.isActive) break

        //notifier.showProgress(manga, progress, total)
        syncChaptersFromSource.await(manga)
      }
    }

    val result = libraryUpdater.enqueue(categoryId, LibraryUpdater.Target.Chapters, operation)
    rescheduleCategory(result, categoryId)
    return result
  }

  private fun rescheduleCategory(result: LibraryUpdater.QueueResult, categoryId: Long) {
    if (result == LibraryUpdater.QueueResult.AlreadyEnqueued) {
      // Nothing to do. The other running operation will reschedule
      return
    }

    GlobalScope.launch(Dispatchers.Default) {
      result.awaitWork()

      val category = withContext(Dispatchers.IO) {
        categoryRepository.find(categoryId)
      }
      if (category != null && category.updateInterval > 0) {
        Logger.debug("Rescheduling category $categoryId")
        libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters,
          category.updateInterval)
      }
    }
  }

}
