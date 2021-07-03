/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.domain.library.interactor.GetLibraryCategory
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibraryUpdaterEvent
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryUpdater @Inject constructor(
  private val getLibraryCategory: GetLibraryCategory,
  private val syncChaptersFromSource: SyncChaptersFromSource
) {

  private val _running = MutableStateFlow(false)
  val running: StateFlow<Boolean> get() = _running

  private val _events = MutableSharedFlow<LibraryUpdaterEvent>(
    extraBufferCapacity = 3,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: Flow<LibraryUpdaterEvent> get() = _events

  private var task: LibraryUpdaterTask? = null
    set(value) {
      if (field !== value) {
        field = value
        _running.value = value != null
        if (value == null) {
          enqueuedCategories.clear()
          enqueuedManga.clear()
        }
      }
    }

  private var enqueuedCategories = mutableSetOf<Long>()
  private var enqueuedManga = mutableSetOf<Long>()

  @Suppress("EXPERIMENTAL_API_USAGE")
  private val actor = GlobalScope.actor<Message>(
    context = Dispatchers.Default,
    capacity = Channel.UNLIMITED
  ) {
    for (msg in channel) {
      when (msg) {
        is Message.Enqueue -> processEnqueue(msg.categoryIds)
        Message.Complete -> processComplete()
        Message.Cancel -> processCancel()
      }
    }
  }

  fun enqueue(categoryIds: List<Long>) {
    actor.trySend(Message.Enqueue(categoryIds))
  }

  fun cancel() {
    actor.trySend(Message.Cancel)
  }

  private suspend fun processEnqueue(categoryIds: List<Long>) {
    val categoriesToUpdate = categoryIds.distinct()
      .filter { categoryId ->
        if (categoryId in enqueuedCategories) {
          false
        } else {
          enqueuedCategories.add(categoryId)
          true
        }
      }

    val mangasToUpdate = categoriesToUpdate.map { getLibraryCategory.await(it) }
      .flatten()
      .filter { manga ->
        if (manga.id in enqueuedManga) {
          false
        } else {
          enqueuedManga.add(manga.id)
          true
        }
      }

    if (mangasToUpdate.isEmpty()) {
      return
    }

    if (task == null) {
      val scope = CoroutineScope(SupervisorJob())
      val onComplete = { actor.trySend(Message.Complete).isSuccess }
      val channel = Channel<List<LibraryManga>>()
      task = LibraryUpdaterTask(syncChaptersFromSource, scope, channel, _events, onComplete)
    }

    task?.send(mangasToUpdate)
  }

  private fun processComplete() {
    task?.cancel()
    task = null
  }

  private fun processCancel() {
    task?.cancel()
    task = null
  }

  private sealed class Message {
    data class Enqueue(val categoryIds: List<Long>) : Message()
    object Complete : Message()
    object Cancel : Message()
  }

}

