/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.selects.whileSelect
import tachiyomi.core.util.removeFirst
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibraryUpdaterEvent
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import javax.inject.Inject

internal class LibraryUpdaterTask @Inject constructor(
  private val syncChaptersFromSource: SyncChaptersFromSource,
  private val scope: CoroutineScope,
  private val inputs: Channel<List<LibraryManga>>,
  private val events: MutableSharedFlow<LibraryUpdaterEvent>,
  private val onComplete: () -> Any
) {

  private val pendingManga = mutableListOf<LibraryManga>()
  private val workers = mutableListOf<Worker>()
  private val results = Channel<Result>(Channel.UNLIMITED)
  private var updated = 0
  private var total = 0

  init {
    scope.launch {
      repeat(5) { id -> launchWorker(id) }

      whileSelect {
        inputs.onReceive { mangas ->
          pendingManga += mangas
          total += mangas.size
          reschedule()
        }
        results.onReceive { result ->
          workers.first { it.current == result.manga }.current = null
          updated += 1
          reschedule()
        }
      }

      // Clean up when completed
      closeChannels()
    }
  }

  suspend fun send(mangas: List<LibraryManga>) {
    if (mangas.isEmpty()) return
    inputs.send(mangas)
  }

  fun cancel() {
    scope.cancel()
    closeChannels()
  }

  private fun closeChannels() {
    inputs.close()
    workers.forEach { it.channel.close() }
  }

  private suspend fun reschedule(): Boolean {
    if (pendingManga.isEmpty() && workers.all { it.current == null }) {
      events.emit(LibraryUpdaterEvent.Completed(total))
      onComplete()
      return false
    }

    for (worker in workers) {
      if (worker.current != null || pendingManga.isEmpty()) continue
      val busySources = workers.mapNotNull { it.current?.sourceId }
      val nextManga = pendingManga.removeFirst { it.sourceId !in busySources } ?: continue

      worker.current = nextManga
      worker.channel.send(nextManga)
    }

    val updatingManga = workers.mapNotNull { it.current }
    events.emit(LibraryUpdaterEvent.Progress(updated, total, updatingManga))
    return true
  }

  private fun CoroutineScope.launchWorker(id: Int) {
    val channel = Channel<LibraryManga>()
    val worker = Worker(id, channel)
    workers.add(worker)

    channel.consumeAsFlow()
      .onEach { manga ->
        val diff = syncChaptersFromSource.await(manga)
        results.send(Result(manga, diff))
      }
      .launchIn(this + CoroutineName("libupdater #$id"))
  }

  private data class Worker(val id: Int, val channel: SendChannel<LibraryManga>) {
    var current: LibraryManga? = null
  }

  private data class Result(val manga: LibraryManga, val result: SyncChaptersFromSource.Result)
}
