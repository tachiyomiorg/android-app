/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibraryUpdaterEvent
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource.Diff
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource.Result as SyncResult

class LibraryUpdaterTaskTest : FunSpec({

  lateinit var syncChapters: SyncChaptersFromSource
  lateinit var events: MutableSharedFlow<LibraryUpdaterEvent>
  lateinit var job: Job
  lateinit var updater: LibraryUpdaterTask

  val testManga = buildList<LibraryManga> {
    for (i in 1..7L) {
      for (j in 1..4L) {
        add(mockk {
          every { id } returns (i - 1) * 10 + j
          every { sourceId } returns i
        })
      }
    }
  }

  beforeTest {
    syncChapters = mockk {
      coEvery { await(any()) } returns SyncResult.Success(Diff())
    }
    val inputs = Channel<List<LibraryManga>>()
    job = SupervisorJob()
    val scope = CoroutineScope(job)
    events = MutableSharedFlow(replay = Int.MAX_VALUE) // Store every event sent
    updater = LibraryUpdaterTask(syncChapters, scope, inputs, events) {
      scope.cancel()
    }
  }

  test("updates sequentially when using the same source") {
    val sameSourceManga = testManga.groupBy { it.sourceId }.values.first()
    updater.send(sameSourceManga)
    job.join()
    events.replayCache
      .filterIsInstance<LibraryUpdaterEvent.Progress>()
      .forEach { it.updating shouldHaveSize 1 }

    events.replayCache.shouldExist { it is LibraryUpdaterEvent.Completed }
  }

  test("updates concurrently when using different sources") {
    val diffSourceManga = testManga.groupBy { it.sourceId }.values.take(2).flatten()
    updater.send(diffSourceManga)
    job.join()
    val firstEvent = events.replayCache
      .filterIsInstance<LibraryUpdaterEvent.Progress>()
      .first()

    firstEvent.updating shouldHaveSize 2
    events.replayCache.shouldExist { it is LibraryUpdaterEvent.Completed }
  }

  test("updates at most from 5 sources at the same time") {
    testManga.map { it.sourceId }.toSet().size shouldBeGreaterThan 5
    updater.send(testManga)
    job.join()
    val firstEvent = events.replayCache
      .filterIsInstance<LibraryUpdaterEvent.Progress>()
      .first()

    firstEvent.updating shouldHaveSize 5
    events.replayCache.shouldExist { it is LibraryUpdaterEvent.Completed }
  }

  test("cancels tasks while updating") {
    val sameSourceManga = testManga.groupBy { it.sourceId }.values.first()
    val cancelAfterManga = sameSourceManga[2]
    coEvery { syncChapters.await(match { it.id == cancelAfterManga.id }) } coAnswers {
      delay(3000)
      SyncResult.Success(Diff())
    }

    updater.send(sameSourceManga)
    delay(100)
    updater.cancel()
    job.join()

    events.replayCache.filterIsInstance<LibraryUpdaterEvent.Completed>().shouldBeEmpty()
    events.replayCache.filterIsInstance<LibraryUpdaterEvent.Progress>() shouldHaveSize 3
  }

})
