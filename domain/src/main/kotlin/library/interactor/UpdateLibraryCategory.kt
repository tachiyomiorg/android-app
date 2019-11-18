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
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdateScheduler
import tachiyomi.domain.library.updater.LibraryUpdater
import tachiyomi.domain.library.updater.LibraryUpdaterNotification
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject

class UpdateLibraryCategory @Inject constructor(
  private val getLibraryCategory: GetLibraryCategory,
  private val syncChaptersFromSource: SyncChaptersFromSource,
  private val notifier: LibraryUpdaterNotification,
  private val libraryUpdater: LibraryUpdater,
  private val categoryRepository: CategoryRepository,
  private val libraryScheduler: LibraryUpdateScheduler
) {

  suspend fun execute(categoryId: Long): LibraryUpdater.QueueResult {
    val operation: suspend (Job) -> Any = { job ->
      Timber.debug { "Updating category $categoryId ${Thread.currentThread()}" }
      notifier.start()

      job.invokeOnCompletion {
        Timber.debug { "Finished updating category $categoryId ${Thread.currentThread()}" }
        notifier.end()
      }

      val mangas = getLibraryCategory.execute(categoryId)
      val total = mangas.size

      for ((progress, manga) in mangas.withIndex()) {
        if (!job.isActive) break

        notifier.showProgress(manga, progress, total)
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
        Timber.debug { "Rescheduling category $categoryId" }
        libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters,
          category.updateInterval)
      }
    }
  }

}
