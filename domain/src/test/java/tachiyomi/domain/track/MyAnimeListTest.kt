/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track

import io.kotlintest.TestCaseConfig
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import tachiyomi.core.http.CookieManager
import tachiyomi.core.http.CookieStore
import tachiyomi.core.http.Http
import tachiyomi.domain.track.model.TrackStateUpdate
import tachiyomi.domain.track.model.TrackStatus
import tachiyomi.domain.track.service.TrackPreferences
import tachiyomi.domain.track.sites.myanimelist.MyAnimeList

class MyAnimeListTest : StringSpec() {

  private val malUser get() = System.getenv("MAL_USER")
  private val malPass get() = System.getenv("MAL_PASS")

  private val hasLogin = !malUser.isNullOrEmpty() && !malPass.isNullOrEmpty()

  override val defaultTestCaseConfig = TestCaseConfig(enabled = hasLogin)

  private val cookieStore = mockk<CookieStore>(relaxUnitFun = true) {
    every { load() } returns emptyMap()
  }

  private val client: OkHttpClient = OkHttpClient.Builder()
    .cookieJar(CookieManager(cookieStore))
    .build()

  private val http = mockk<Http> {
    every { defaultClient } returns client
  }

  private val prefs = mockk<TrackPreferences>()

  private val mal = MyAnimeList(http, prefs)

  init {
    val testMediaId = 2L

    "Search should work" {
      val results = mal.search("berserk")
      val berserk = results.first()
      berserk.mediaId shouldBe 2
      berserk.mediaUrl shouldBe "https://myanimelist.net/manga/2/Berserk"
      berserk.title shouldBe "Berserk"
      berserk.totalChapters shouldBe 0
      berserk.publishingStatus shouldBe "Publishing"
      berserk.startDate shouldBe "08-25-89"
    }
    "Login should work" {
      mal.login(malUser, malPass) shouldBe true
    }
    "Manga list should not have test manga" {
      mal.getEntryId(testMediaId) shouldBe null
    }
    "Test manga should be added to the list" {
      mal.add(testMediaId)
    }
    "Manga list should have the test manga" {
      mal.getEntryId(testMediaId) shouldNotBe null
    }
    "Test manga should be updated from the list" {
      val track = TrackStateUpdate(
        status = TrackStatus.OnHold,
        score = 9f,
        lastChapterRead = 6f
      )
      mal.update(testMediaId, track)
    }
    "Test manga should receive updated state" {
      val state = mal.getState(testMediaId)!!
      state.status shouldBe TrackStatus.OnHold
      state.score shouldBe 9f
      state.lastChapterRead shouldBe 6f
    }
    "Test manga should be deleted from the list" {
      mal.delete(testMediaId)
    }
    "Manga list should have deleted the test manga" {
      mal.getEntryId(testMediaId) shouldBe null
    }
  }

}
