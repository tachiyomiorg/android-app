/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import tachiyomi.domain.library.service.LibraryUpdater
import javax.inject.Inject

class UpdateLibraryCategory @Inject internal constructor(
  private val libraryUpdater: LibraryUpdater,
) {

  fun enqueue(categoryId: Long) {
    libraryUpdater.enqueue(listOf(categoryId))
  }

//  private fun rescheduleCategory(result: LibraryUpdater.QueueResult, categoryId: Long) {
//    if (result == LibraryUpdater.QueueResult.AlreadyEnqueued) {
//      // Nothing to do. The other running operation will reschedule
//      return
//    }
//
//    GlobalScope.launch(Dispatchers.Default) {
//      result.awaitWork()
//
//      val category = withContext(Dispatchers.IO) {
//        categoryRepository.find(categoryId)
//      }
//      if (category != null && category.updateInterval > 0) {
//        Log.debug("Rescheduling category $categoryId")
//        libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters,
//          category.updateInterval)
//      }
//    }
//  }

}
