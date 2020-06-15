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
import java.io.File

internal open class DownloadManagerActor(
  private val messages: Channel<Message>,
  private val preferences: DownloadPreferences,
  private val downloader: Downloader,
  private val compressor: DownloadCompressor,
  private val repository: DownloadRepository
) {

  private val compressPreference = preferences.compress()

  protected var workerJob: Job? = null
  private var workerScope: CoroutineScope? = null
  private var workers = mutableListOf<Worker>()
  protected val downloadResults = Channel<Downloader.Result>()
  protected val compressionResults = Channel<DownloadCompressor.Result>()

  protected val downloads = mutableListOf<QueuedDownload>()
  protected val states = mutableMapOf<Long, State>()
  private val lockedDownloads = mutableSetOf<Long>()

  suspend fun receiveAll() {
    val savedDownloads = repository.findAll()
    if (savedDownloads.isNotEmpty()) {
      restore(savedDownloads)
    }

    while (true) {
      select<Unit> {
        messages.onReceive { msg ->
          when (msg) {
            Message.Start -> start()
            Message.Stop -> stop()
            is Message.Add -> add(msg.downloads)
            is Message.Remove -> remove(msg.downloads)
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
      workerJob = null
      workerScope = null

      // Only keep states of completing downloads
      val iterator = states.iterator()
      while (iterator.hasNext()) {
        val (_, state) = iterator.next()
        if (state !is State.Completing) {
          iterator.remove()
        }
      }
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
      val downloading = states.values.asSequence()
        .filterIsInstance<State.Downloading>()

      // Check if this worker is already downloading
      if (downloading.any { it.workerId == worker.id }) continue

      val activeSources = downloading.map { it.download.sourceId }.toSet()

      val nextDownload = downloads.find {
        it.chapterId !in states &&
          it.chapterId !in lockedDownloads &&
          it.sourceId !in activeSources
      } ?: continue

      val state = State.Downloading(worker.id, nextDownload)
      states[nextDownload.chapterId] = state
      worker.channel.offer(nextDownload)
    }

    if (states.none { it.value.inProgress }) {
      stop()
    }
  }

  private suspend fun onChapterDownloadResult(result: Downloader.Result) {
    val download = result.download
    states.remove(download.chapterId)

    when (result) {
      is Downloader.Result.Success -> {
        val compressionEnabled = compressPreference.get()
        if (!compressionEnabled) {
          states[download.chapterId] = State.Completing(download, result.tmpDir)
          checkDownloadCompletion(download.chapterId)
        } else {
          workerScope?.let {
            states[download.chapterId] = State.Compressing(download)
            compressor.worker(it, result.download, result.tmpDir, compressionResults)
          }
        }
      }
      is Downloader.Result.Failure -> {
        states[download.chapterId] = State.Failed(result.error)
      }
    }

    scheduleDownloads()
  }

  private suspend fun onChapterCompressionResult(result: DownloadCompressor.Result) {
    val download = result.download
    states.remove(download.chapterId)

    when (result) {
      is DownloadCompressor.Result.Success -> {
        states[download.chapterId] = State.Completing(download, result.tmpZip)
        checkDownloadCompletion(download.chapterId)
      }
      is DownloadCompressor.Result.Failure -> {
        states[download.chapterId] = State.Failed(result.error)
      }
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
    val (download, tmpFile) = states[chapterId] as? State.Completing ?: return

    repository.delete(chapterId)
    states.remove(chapterId)
    downloads.remove(download)

    // Both compressed files and directories end with "[._]tmp" so we just drop the last 4 chars
    val finalFile = File(tmpFile.absolutePath.dropLast(4))
    tmpFile.renameTo(finalFile)
  }

  sealed class State {
    data class Downloading(val workerId: Int, val download: QueuedDownload) : State()
    data class Compressing(val download: QueuedDownload) : State()
    data class Completing(val download: QueuedDownload, val tmpFile: File) : State()
    data class Failed(val error: Throwable) : State()

    val inProgress get() = this is Downloading || this is Compressing
  }

  sealed class Message {
    object Start : Message()
    object Stop : Message()

    // TODO maybe provide other objects
    data class Add(val downloads: Map<Manga, List<Chapter>>) : Message()
    data class Remove(val downloads: List<Chapter>) : Message()
    object Clear : Message()
    //data class Reorder : Message() // TODO to implement later

    data class LockSourceFiles(val chapterId: Long) : Message()
    data class UnlockSourceFiles(val chapterId: Long) : Message()
  }

  private data class Worker(val id: Int, val channel: SendChannel<QueuedDownload>)

}
