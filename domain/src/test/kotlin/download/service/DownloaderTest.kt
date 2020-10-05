/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestContext
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import tachiyomi.core.http.GET
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.download.model.QueuedDownload
import tachiyomi.domain.download.service.Downloader.Result.Success
import tachiyomi.source.HttpSource
import tachiyomi.source.model.ImageBase64
import tachiyomi.source.model.ImageUrl
import tachiyomi.source.model.PageUrl
import tachiyomi.source.model.Text
import java.io.File
import java.nio.file.Files

class DownloaderTest : FunSpec({

  val client = OkHttpClient.Builder()
    .cache(Cache(getTestCacheDir(), Long.MAX_VALUE))
    .build()

  val directoryProvider = mockk<DownloadDirectoryProvider>()
  val getLocalCatalog = mockk<GetLocalCatalog>()
  val source = mockk<HttpSource>()
  val catalog = mockk<CatalogLocal>()

  val imageBase64 = ImageBase64(getBase64Image())

  val downloader = object : Downloader(directoryProvider, getLocalCatalog) {
    override val retryDelay = 0L
  }

  val awaitGivenDownload: suspend TestContext.(QueuedDownload) -> Downloader.Result = {
    val channel = Channel<QueuedDownload>(Channel.UNLIMITED)
    val result = Channel<Downloader.Result>()
    launch { downloader.worker(this, channel, result) }
    channel.send(it)
    channel.close()
    result.receive()
  }

  val awaitDownload: suspend TestContext.() -> Downloader.Result = {
    awaitGivenDownload(QueuedDownload(1, 1))
  }

  afterTest { clearAllMocks() }

  beforeTest {
    every { directoryProvider.getTempChapterDir(any()) } returns tempdir("chapter")
    every { getLocalCatalog.get(any()) } returns catalog
    every { catalog.source } returns source
    every { source.client } returns client
  }

  context("page list") {
    fun everyPageList() = coEvery { source.getPageList(any()) }
    val pageList = listOf(Text("a"))
    val error = Exception("Source threw exception on getPageList")

    test("completes") {
      everyPageList() returns pageList
      val result = awaitDownload()
      result.success shouldBe true
      coVerify(exactly = 1) { source.getPageList(any()) }
    }
    test("fails when empty") {
      everyPageList() returns listOf()
      val result = awaitDownload()
      result.success shouldBe false
      coVerify(exactly = 1) { source.getPageList(any()) }
    }
    test("is retried") {
      everyPageList() throws error andThen pageList
      val result = awaitDownload()
      result.success shouldBe true
      coVerify(exactly = 2) { source.getPageList(any()) }
    }
    test("fails after many attempts") {
      everyPageList() throws error andThenThrows error andThen pageList
      val result = awaitDownload()
      result.success shouldBe false
      coVerify(exactly = 2) { source.getPageList(any()) }
    }
    test("uses saved value on resumed download") {
      val download = QueuedDownload(1, 1)
      download.pages = pageList
      val result = awaitGivenDownload(download)
      result.success shouldBe true
      coVerify(exactly = 0) { source.getPageList(any()) }
    }
  }

  context("incomplete page") {
    beforeTest {
      coEvery { source.getPageList(any()) } returns listOf(PageUrl("a"))
    }
    fun everyPage() = coEvery { source.getPage(any()) }
    val text = Text("a")
    val error = Exception("Source threw exception on getPageSource")

    test("is completed") {
      everyPage() returns text
      val result = awaitDownload()
      result.success shouldBe true
      coVerify(exactly = 1) { source.getPage(any()) }
    }
    test("is retried") {
      everyPage() throws error andThen text
      val result = awaitDownload()
      result.success shouldBe true
      coVerify(exactly = 2) { source.getPage(any()) }
    }
    test("fails after many attempts") {
      everyPage() throws error andThenThrows error andThen text
      val result = awaitDownload()
      result.success shouldBe false
      coVerify(exactly = 2) { source.getPage(any()) }
    }
    test("is updated to complete page") {
      everyPage() throws error andThen text
      val result = awaitDownload()
      result.download.pages!!.first().shouldBeInstanceOf<Text>()
    }
    test("resumes to next page on failure") {
      coEvery { source.getPageList(any()) } returns listOf(PageUrl("a"), PageUrl("b"))
      coEvery { source.getPage(PageUrl("a")) } throws error
      coEvery { source.getPage(PageUrl("b")) } returns text
      val result = awaitDownload()
      result.success shouldBe false
      coVerify(exactly = 1) { source.getPage(PageUrl("b")) }
    }
  }

  context("complete page") {
    val imageUrl = ImageUrl("https://i.picsum.photos/id/11/300/400.jpg")
    val imageUrlRequest = GET(imageUrl.url, cache = CacheControl.FORCE_CACHE)
    beforeTest {
      coEvery { source.getImageRequest(any()) } returns imageUrlRequest
    }

    test("ImageUrl is saved to disk") {
      coEvery { source.getPageList(any()) } returns listOf(imageUrl)
      val result = awaitDownload()
      result as Success
      result.tmpDir.listFiles()!!.first().extension shouldBe "jpg"
    }
    test("ImageUrl is retried") {
      coEvery { source.getImageRequest(any()) } throws Exception() andThen imageUrlRequest
      coEvery { source.getPageList(any()) } returns listOf(imageUrl)
      val result = awaitDownload()
      result as Success
    }
    test("ImageBase64 is saved to disk") {
      coEvery { source.getPageList(any()) } returns listOf(imageBase64)
      val result = awaitDownload()
      result as Success
      result.tmpDir.listFiles()!!.first().extension shouldBe "jpg"
    }
    test("Text is saved to disk") {
      coEvery { source.getPageList(any()) } returns listOf(Text("a"))
      val result = awaitDownload()
      result as Success
      result.tmpDir.listFiles()!!.first().extension shouldBe "txt"
    }
  }

})

private fun getBase64Image(): String {
  return File(ClassLoader.getSystemClassLoader().getResource("base64image.txt")!!.file).readText()
}

private fun getTestCacheDir(): File {
  return File(ClassLoader.getSystemClassLoader().getResource("downloader-net-cache")!!.file)
}

fun TestConfiguration.tempdir(prefix: String? = null): File {
  val file = Files.createTempDirectory(prefix).toFile()
  afterSpec {
    file.deleteRecursively()
  }
  return file
}
