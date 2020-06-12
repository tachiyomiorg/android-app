/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import tachiyomi.core.util.removeFirst
import tachiyomi.domain.download.model.QueuedDownload
import tachiyomi.domain.download.model.SavedDownload
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga

internal class DownloadManagerActor(
  private val messages: Channel<Message>,
  private val preferences: DownloadPreferences,
  private val downloader: Downloader,
  private val compressor: DownloadCompressor,
  private val repository: DownloadRepository
) {

  private val compressPreference = preferences.compress()

  private var workerJob: Job? = null
  private var workerScope: CoroutineScope? = null
  private var workers = mutableListOf<Worker>()
  private val downloadResults = Channel<Downloader.Result>()
  private val compressionResults = Channel<DownloadCompressor.Result>()

  private val downloads = mutableListOf<QueuedDownload>()
  private val activeDownloads = mutableMapOf<Long, Int>()
  private val failedDownloads = mutableSetOf<Long>()
  private val lockedDownloads = mutableSetOf<Long>()
  private val compressingDownloads = mutableSetOf<Long>()
  private val awaitingCompletionDownloads = mutableListOf<QueuedDownload>()

  suspend fun receiveAll() {
    val savedDownloads = repository.findAll()
    if (savedDownloads.isNotEmpty()) {
      messages.send(Message.Restore(savedDownloads))
    }

    while (true) {
      select<Unit> {
        messages.onReceive { msg ->
          when (msg) {
            Message.Start -> start()
            Message.Stop -> stop()
            is Message.Add -> add(msg.downloads)
            is Message.Remove -> remove(msg.downloads)
            is Message.Restore -> restore(msg.downloads)
            Message.Clear -> clear()
            is Message.LockSourceFiles -> lockSourceFiles(msg.chapterId)
            is Message.UnlockSourceFiles -> unlockSourceFiles(msg.chapterId)
          }
        }
        downloadResults.onReceive { result ->
          onChapterDownloadResult(result)
        }
        compressionResults.onReceive { result ->
          onChapterCompressionResult(result)
        }
      }
    }
  }

  private suspend fun start() {
    if (workerJob != null || downloads.isEmpty()) return

    val job = SupervisorJob()
    val scope = CoroutineScope(job)
    workerJob = job
    workerScope = scope

    repeat(3) { id ->
      val channel = Channel<QueuedDownload>()
      val worker = Worker(id, channel)
      workers.add(worker)
      downloader.worker(scope, channel, downloadResults)
    }
    scheduleDownloads()
  }

  private suspend fun stop() {
    if (workerJob != null) {
      workerJob!!.cancelAndJoin()
      workers.clear()
      failedDownloads.clear()
      activeDownloads.clear()
      workerJob = null
      workerScope = null
    }
  }

  private suspend fun add(mangaWithChapters: Map<Manga, List<Chapter>>) {
    val downloadsIds = downloads.asSequence().map { it.chapterId }.toSet()
    for ((manga, chapters) in mangaWithChapters) {
      for (chapter in chapters) {
        val inQueue = chapter.id in downloadsIds
        val isDownloaded = false // TODO
        if (!inQueue && !isDownloaded) {
          val download = QueuedDownload(chapter.id, manga.sourceId)
          downloads.add(download)
        }
      }
    }
    scheduleDownloads()
  }

  private suspend fun remove(chapters: List<Chapter>) {
    // TODO we might need to stop the downloader if the active download is being removed
    for (chapter in chapters) {
      downloads.removeFirst { it.chapterId == chapter.id }
    }
    if (downloads.isEmpty()) {
      stop()
    }
  }

  private suspend fun restore(savedDownloads: List<SavedDownload>) {
    for (download in savedDownloads) {
      val queued = QueuedDownload(download.chapterId, download.sourceId)
      downloads.add(queued)
    }
  }

  private suspend fun clear() {
    stop()
    downloads.clear()
  }

  private suspend fun scheduleDownloads() {
    for (worker in workers) {
      if (worker.isDownloading) continue

      val activeSources = activeDownloads.asSequence()
        .mapNotNull { (chapterId, _) ->
          downloads.find { it.chapterId == chapterId }?.sourceId
        }
        .toSet()

      val nextDownload = downloads.find {
        it.chapterId !in failedDownloads &&
          it.chapterId !in activeDownloads &&
          it.chapterId !in lockedDownloads &&
          it.chapterId !in compressingDownloads &&
          it.sourceId !in activeSources
      } ?: continue

      activeDownloads[nextDownload.chapterId] = worker.id
      worker.channel.send(nextDownload)
    }

    if (activeDownloads.isEmpty()) {
      stop()
    }
  }

  private suspend fun onChapterDownloadResult(result: Downloader.Result) {
    val download = result.download
    activeDownloads.remove(download.chapterId)

    when (result) {
      is Downloader.Result.Success -> {
        if (!compressPreference.get()) {
          awaitingCompletionDownloads.add(download)
          checkDownloadCompletion(download.chapterId)
        } else {
          workerScope?.let {
            compressingDownloads.add(download.chapterId)
            compressor.worker(it, result.download, result.tmpDir, compressionResults)
          }
        }
      }
      is Downloader.Result.Failure -> {
        failedDownloads.add(download.chapterId)
      }
    }

    scheduleDownloads()
  }

  private suspend fun onChapterCompressionResult(result: DownloadCompressor.Result) {
    val download = result.download
    compressingDownloads.remove(download.chapterId)

    if (!result.success) {
      failedDownloads.add(download.chapterId)
    } else {
      awaitingCompletionDownloads.add(download)
      checkDownloadCompletion(download.chapterId)
    }
  }

  private suspend fun lockSourceFiles(chapterId: Long) {
    lockedDownloads.add(chapterId)
  }

  private suspend fun unlockSourceFiles(chapterId: Long) {
    lockedDownloads.remove(chapterId)
    checkDownloadCompletion(chapterId)
  }

  private suspend fun checkDownloadCompletion(chapterId: Long) {
    if (chapterId in lockedDownloads) return
    val download = awaitingCompletionDownloads.removeFirst { it.chapterId == chapterId } ?: return

    // TODO remove from downloads and from repository, rename to final chapter
    downloads.removeFirst { it.chapterId == chapterId }
  }

  sealed class Message {
    object Start : Message()
    object Stop : Message()

    // TODO maybe provide other objects
    data class Add(val downloads: Map<Manga, List<Chapter>>) : Message()
    data class Remove(val downloads: List<Chapter>) : Message()
    data class Restore(val downloads: List<SavedDownload>) : Message()
    object Clear : Message()
    //data class Reorder : Message() // TODO to implement later

    data class LockSourceFiles(val chapterId: Long) : Message()
    data class UnlockSourceFiles(val chapterId: Long) : Message()
  }

  private data class Worker(val id: Int, val channel: SendChannel<QueuedDownload>)

  private val Worker.isDownloading get() = activeDownloads.containsValue(id)

}
