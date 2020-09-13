/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.sites.myanimelist

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.json
import kotlinx.serialization.json.put
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import tachiyomi.core.http.Http
import tachiyomi.core.http.awaitBody
import tachiyomi.core.http.awaitSuccess
import tachiyomi.core.http.get
import tachiyomi.core.http.post
import tachiyomi.domain.track.model.TrackSearchResult
import tachiyomi.domain.track.model.TrackState
import tachiyomi.domain.track.model.TrackStateUpdate
import tachiyomi.domain.track.model.TrackStatus
import tachiyomi.domain.track.service.TrackPreferences
import tachiyomi.domain.track.sites.TrackServices
import tachiyomi.domain.track.sites.TrackSite
import javax.inject.Inject

class MyAnimeList @Inject constructor(
  private val http: Http,
  private val preferences: TrackPreferences
) : TrackSite() {

  override val id = TrackServices.MYANIMELIST

  override val name: String
    get() = "MyAnimeList"

  private var csrf = ""

  private val baseUrl = "https://myanimelist.net"

  private val client get() = http.defaultClient

  override suspend fun add(mediaId: Long): Long {
    val url = "$baseUrl/ownlist/manga/add.json"
    val payload = buildJsonObject {
      put("manga_id", mediaId)
      put("status", TrackStatus.Reading.toSiteState)
      put("csrf_token", csrf)
    }
    val form = payload.toString().toRequestBody(jsonType)

    val response = client.post(url, form).awaitSuccess()
    response.close()
    return mediaId
  }

  override suspend fun update(entryId: Long, track: TrackStateUpdate) {
    val url = "$baseUrl/ownlist/manga/edit.json"
    val payload = buildJsonObject {
      put("manga_id", entryId)
      if (track.status != null) put("status", track.status.toSiteState)
      if (track.score != null) put("score", Math.round(track.score))
      if (track.lastChapterRead != null) put("num_read_chapters", Math.round(track.lastChapterRead))
      put("csrf_token", csrf)
    }
    val form = payload.toString().toRequestBody(jsonType)

    val response = client.post(url, form).awaitSuccess()
    response.close()
  }

  internal suspend fun delete(entryId: Long) {
    val url = "$baseUrl/ownlist/manga/$entryId/delete"
    val payload = buildJsonObject {
      put("csrf_token", csrf)
    }
    val form = payload.toString().toRequestBody(jsonType)

    val response = client.post(url, form).awaitSuccess()
    response.close()
  }

  override suspend fun search(query: String): List<TrackSearchResult> {
    val col = "c[]"
    val url = "$baseUrl/manga.php".toHttpUrl().newBuilder()
      .addQueryParameter("q", query)
      .addQueryParameter(col, "a")
      .addQueryParameter(col, "b")
      .addQueryParameter(col, "c")
      .addQueryParameter(col, "d")
      .addQueryParameter(col, "e")
      .addQueryParameter(col, "g")
      .build()
      .toString()

    val body = client.get(url).awaitBody()
    val parsedBody = Jsoup.parse(body, url)

    return parsedBody.select("div.js-categories-seasonal.js-block-list.list table tbody tr")
      .drop(1)
      .filter { it.select("td")[2].text() != "Novel" }
      .map { row ->
        val picElement = row.select("div.picSurround a").first()
        val totalChapters = if (row.select("td")[4].text() == "-") {
          0
        } else {
          row.select("td")[4].text().toInt()
        }
        val publishingStatus = if (row.select("td").last().text() == "-") {
          "Publishing"
        } else {
          "Finished"
        }
        TrackSearchResult(
          mediaId = picElement.attr("id").replace("sarea", "").toLong(),
          mediaUrl = picElement.absUrl("href"),
          title = row.select("strong").text(),
          totalChapters = totalChapters,
          coverUrl = picElement.select("img").attr("data-src"),
          summary = row.select("div.pt4").first().ownText(),
          publishingStatus = publishingStatus,
          publishingType = row.select("td")[2].text(),
          startDate = row.select("td")[6].text()
        )
      }
  }

  override suspend fun getState(entryId: Long): TrackState? {
    val url = "$baseUrl/ownlist/manga/$entryId/edit"
    val response = client.get(url).awaitSuccess()
    val body = Jsoup.parse(response.awaitBody(), url)

    val lastChapterRead = body.getElementById("add_manga_num_read_chapters").`val`()
    val totalChapters = body.getElementById("totalChap").text()
    val score = body.getElementById("add_manga_score").select("option[selected]").`val`()
    val status = body.getElementById("add_manga_status").select("option[selected]").`val`()

    return TrackState(
      lastChapterRead = lastChapterRead.toFloat(),
      totalChapters = totalChapters.toInt(),
      score = score.toFloatOrNull() ?: 0f,
      status = status.toInt().toStatus
    )
  }

  override suspend fun getEntryId(mediaId: Long): Long? {
    val url = "$baseUrl/ownlist/manga/$mediaId/edit"
    val response = client.get(url).awaitSuccess()
    response.close()

    // If the prior request is a redirect, the manga isn't in the list
    val priorResponse = response.priorResponse
    return if (priorResponse == null || !priorResponse.isRedirect) {
      mediaId
    } else {
      null
    }
  }

  override suspend fun login(username: String, password: String): Boolean {
    val url = "$baseUrl/login.php"
    val csrfBody = client.get(url).awaitBody()

    val csrf = Jsoup.parse(csrfBody, url)
      .select("meta[name=csrf_token]")
      .attr("content")

    this.csrf = csrf

    val loginForm = FormBody.Builder()
      .add("user_name", username)
      .add("password", password)
      .add("cookie", "1")
      .add("sublogin", "Login")
      .add("submit", "1")
      .add("csrf_token", csrf)
      .build()

    val loginResponse = client.post(url, loginForm).awaitSuccess()

    return loginResponse.use {
      it.priorResponse?.code == 302
    }
  }

  override suspend fun logout() {
    TODO("not implemented")
  }

  override fun getSupportedStatusList(): List<TrackStatus> {
    return listOf(
      TrackStatus.Reading,
      TrackStatus.Completed,
      TrackStatus.OnHold,
      TrackStatus.Dropped,
      TrackStatus.Planned
    )
  }

  private val TrackStatus.toSiteState
    get() = when (this) {
      TrackStatus.Reading -> 1
      TrackStatus.Completed -> 2
      TrackStatus.OnHold -> 3
      TrackStatus.Dropped -> 4
      TrackStatus.Planned -> 6
      TrackStatus.Repeating -> 1
    }

  private val Int.toStatus
    get() = when (this) {
      1 -> TrackStatus.Reading
      2 -> TrackStatus.Completed
      3 -> TrackStatus.OnHold
      4 -> TrackStatus.Dropped
      6 -> TrackStatus.Planned
      else -> TrackStatus.Reading
    }

  private companion object {
    val jsonType = "application/json; charset=utf-8".toMediaType()
  }

}
