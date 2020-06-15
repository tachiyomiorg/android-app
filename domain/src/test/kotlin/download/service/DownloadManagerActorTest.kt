/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestContext
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import tachiyomi.core.prefs.Preference
import tachiyomi.domain.download.model.SavedDownload
import tachiyomi.domain.download.service.DownloadManagerActor.Message
import tachiyomi.domain.download.service.DownloadManagerActor.State
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class DownloadManagerActorTest : FunSpec({

  lateinit var messages: Channel<Message>
  val compressPreference = mockk<Preference<Boolean>>()
  val preferences = mockk<DownloadPreferences>()
  val downloader = mockk<Downloader>()
  val compressor = mockk<DownloadCompressor>()
  val repository = mockk<DownloadRepository>()
  val tmpFile = mockk<File>()

  lateinit var actor: TestDownloadManagerActor

  val withActor: suspend TestContext.(suspend () -> Unit) -> Unit = { fn ->
    launch(Dispatchers.Unconfined) {
      try {
        actor.receiveAll()
      } catch (e: ClosedReceiveChannelException) {
        // Ignored
      }
    }
    fn()
    messages.close()
  }

  afterTest { clearAllMocks() }

  beforeTest {
    every { preferences.compress() } returns compressPreference
    messages = Channel()
    actor = TestDownloadManagerActor(messages, preferences, downloader, compressor, repository)
    every { compressPreference.get() } returns false
    coEvery { repository.findAll() } returns emptyList()
    coEvery { downloader.worker(any(), any(), any()) } returns Job()
    coEvery { compressor.worker(any(), any(), any(), any()) } returns Job()
    coEvery { repository.delete(any<Long>()) } just Runs
    every { tmpFile.absolutePath } returns "/tmp/nonexistentfile_tmp"
    every { tmpFile.renameTo(any()) } returns true
  }

  context("restore") {
    test("saved downloads") {
      val savedDownloads = listOf(SavedDownload(
        chapterId = 1, mangaId = 1, priority = 0, sourceId = 1, mangaName = "", chapterKey = "",
        chapterName = "", scanlator = ""
      ))
      coEvery { repository.findAll() } returns savedDownloads

      withActor {
        actor._downloads shouldHaveSize 1
      }
    }
  }

  context("add") {
    test("chapters to queue") {
      withActor {
        messages.send(Message.Add(getTestChaptersToDownload(2)))

        actor._downloads shouldHaveSize 2
      }
    }

    test("chapters to queue without duplicates") {
      withActor {
        messages.send(Message.Add(getTestChaptersToDownload(1)))
        messages.send(Message.Add(getTestChaptersToDownload(3)))

        actor._downloads shouldHaveSize 3
      }
    }
  }

  context("remove") {
    test("non existing chapter from queue") {
      val chapterToRemove = Chapter(100, 1, "ch1", "Chapter 100")
      withActor {
        messages.send(Message.Add(getTestChaptersToDownload(1)))
        messages.send(Message.Remove(listOf(chapterToRemove)))

        actor._downloads shouldHaveSize 1
      }
    }
    test("existing chapter from queue") {
      val chaptersToAdd = getTestChaptersToDownload(1)
      withActor {
        messages.send(Message.Add(chaptersToAdd))
        messages.send(Message.Remove(chaptersToAdd.values.first()))

        actor._downloads.shouldBeEmpty()
      }
    }

    test("clear all chapters") {
      val chaptersToAdd = getTestChaptersToDownload(5)
      withActor {
        messages.send(Message.Add(chaptersToAdd))
        messages.send(Message.Clear)

        actor._downloads.shouldBeEmpty()
      }
    }
  }

  context("download") {
    val testDownloadsMultiManga = (1..5L).associate { mangaId ->
      Manga(mangaId, mangaId, "manga $mangaId", "Manga $mangaId") to (1..3L).map {
        val chapterId = (mangaId - 1) * 100 + it
        Chapter(chapterId, mangaId, "chapter $chapterId", "Chapter $chapterId")
      }
    }
    val testDownloadsSingleManga = testDownloadsMultiManga.filterKeys { it.id == 1L }

    test("does nothing when there are no downloads") {
      withActor {
        messages.send(Message.Start)

        actor._workerJob.shouldBeNull()
        actor._states.shouldBeEmpty()
      }
    }

    test("begins downloading chapters") {
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        actor._workerJob.shouldNotBeNull()
        actor._states shouldHaveSize 1
        actor._states.values.first().shouldBeInstanceOf<State.Downloading>()
      }
    }

    test("begins downloading chapters simultaneously from multiple sources") {
      withActor {
        messages.send(Message.Add(testDownloadsMultiManga))
        messages.send(Message.Start)

        // Up to 3 workers spawn, so we can have 3 simultaneous downloads max
        actor._states shouldHaveSize 3
        actor._states.values.forEach { it.shouldBeInstanceOf<State.Downloading>() }
      }
    }

    test("downloads a chapter") {
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        actor._downloads shouldNotContain download
        actor._states[download.chapterId].shouldBeNull()
        verify { tmpFile.renameTo(any()) }
      }
    }

    test("fails to download a chapter") {
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Failure(download, Exception("Download error"))
        actor._downloadResults.send(downloadResult)

        actor._downloads shouldContain download
        actor._states[download.chapterId].shouldBeInstanceOf<State.Failed>()
        verify(exactly = 0) { tmpFile.renameTo(any()) }
      }
    }

    test("downloads next chapter on completion") {
      withActor {
        messages.send(Message.Add(testDownloadsMultiManga))
        messages.send(Message.Start)

        val prevDownloads = actor._states.values.filterIsInstance<State.Downloading>()
          .map { it.download.chapterId }
        val download = (actor._states.values.first() as State.Downloading).download

        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        val newDownloads = actor._states.values.filterIsInstance<State.Downloading>()
          .map { it.download.chapterId }

        prevDownloads shouldBeSameSizeAs newDownloads
        prevDownloads shouldContain download.chapterId
        newDownloads shouldNotContain download.chapterId
      }
    }

    test("downloads next chapter on failure") {
      withActor {
        messages.send(Message.Add(testDownloadsMultiManga))
        messages.send(Message.Start)

        val prevDownloads = actor._states.values.filterIsInstance<State.Downloading>()
          .map { it.download.chapterId }
        val download = (actor._states.values.first() as State.Downloading).download

        val downloadResult = Downloader.Result.Failure(download, Exception("Download error"))
        actor._downloadResults.send(downloadResult)

        val newDownloads = actor._states.values.filterIsInstance<State.Downloading>()
          .map { it.download.chapterId }

        prevDownloads shouldBeSameSizeAs newDownloads
        prevDownloads shouldContain download.chapterId
        newDownloads shouldNotContain download.chapterId
      }
    }

    test("downloads chapters until none is left").config(
      timeout = 1.toDuration(DurationUnit.SECONDS)
    ) {
      withActor {
        messages.send(Message.Add(testDownloadsMultiManga))
        messages.send(Message.Start)

        while (true) {
          val currentlyDownloading = actor._states.values.filterIsInstance<State.Downloading>()
          if (currentlyDownloading.isEmpty()) break

          for (downloading in currentlyDownloading) {
            val downloadResult = Downloader.Result.Success(downloading.download, tmpFile)
            actor._downloadResults.send(downloadResult)
          }
        }

        actor._downloads.shouldBeEmpty()
        actor._workerJob.shouldBeNull()
      }
    }

    test("downloads chapters until stopped") {
      withActor {
        messages.send(Message.Add(testDownloadsMultiManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        messages.send(Message.Stop)

        actor._states.shouldBeEmpty()
        actor._workerJob.shouldBeNull()
      }
    }

    test("schedules chapter compression") {
      every { compressPreference.get() } returns true
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        actor._downloads shouldContain download
        actor._states[download.chapterId].shouldBeInstanceOf<State.Compressing>()
      }
    }

    test("compresses a chapter") {
      every { compressPreference.get() } returns true
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        val compressionResult = DownloadCompressor.Result.Success(download, tmpFile)
        actor._compressionResults.send(compressionResult)

        actor._downloads shouldNotContain download
        actor._states[download.chapterId].shouldBeNull()
        verify { tmpFile.renameTo(any()) }
      }
    }

    test("fails to compress a chapter") {
      every { compressPreference.get() } returns true
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        val compressionResult = DownloadCompressor.Result.Failure(download, Exception("Error"))
        actor._compressionResults.send(compressionResult)

        actor._downloads shouldContain download
        actor._states[download.chapterId].shouldBeInstanceOf<State.Failed>()
        verify(exactly = 0) { tmpFile.renameTo(any()) }
      }
    }

    test("keeps a locked chapter") {
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        messages.send(Message.LockSourceFiles(download.chapterId))

        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        actor._downloads shouldContain download
        actor._states[download.chapterId].shouldBeInstanceOf<State.Completing>()
        verify(exactly = 0) { tmpFile.renameTo(any()) }
      }
    }

    test("keeps a locked chapter until unlocked") {
      withActor {
        messages.send(Message.Add(testDownloadsSingleManga))
        messages.send(Message.Start)

        val download = (actor._states.values.first() as State.Downloading).download
        messages.send(Message.LockSourceFiles(download.chapterId))

        val downloadResult = Downloader.Result.Success(download, tmpFile)
        actor._downloadResults.send(downloadResult)

        actor._states[download.chapterId].shouldBeInstanceOf<State.Completing>()

        messages.send(Message.UnlockSourceFiles(download.chapterId))

        actor._downloads shouldNotContain download
        actor._states[download.chapterId].shouldBeNull()
        verify { tmpFile.renameTo(any()) }
      }
    }
  }

})

@Suppress("PropertyName")
private class TestDownloadManagerActor(
  messages: Channel<Message>,
  preferences: DownloadPreferences,
  downloader: Downloader,
  compressor: DownloadCompressor,
  repository: DownloadRepository
) : DownloadManagerActor(messages, preferences, downloader, compressor, repository) {

  val _downloads get() = downloads
  val _workerJob get() = workerJob
  val _downloadResults get() = downloadResults
  val _compressionResults get() = compressionResults
  val _states get() = states
}

private fun getTestChaptersToDownload(numChapters: Int): Map<Manga, List<Chapter>> {
  return mapOf(
    Manga(1, 1, "manga1", "Manga 1") to (1..numChapters).map {
      Chapter(it.toLong(), 1, "chapter$it", "Chapter $it")
    }
  )
}
