/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import kotlinx.serialization.parse
import tachiyomi.core.http.Http
import tachiyomi.core.http.awaitBody
import tachiyomi.core.http.get
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogRemoteApi
import javax.inject.Inject

internal class CatalogGithubApi @Inject constructor(private val http: Http) : CatalogRemoteApi {

  // TODO create a new branch for 1.x extensions
//  private val repoUrl = "https://raw.githubusercontent.com/inorichi/tachiyomi-extensions/repo"
  private val repoUrl = "https://tachiyomi.kanade.eu/repo"

  override suspend fun findCatalogs(): List<CatalogRemote> {
    val body = http.defaultClient.get("$repoUrl/index.min.json").awaitBody()
    val json = Json(JsonConfiguration.Stable).parse<JsonArray>(body)
    return json.map { element ->
      element as JsonObject
      val name = element["name"]!!.content
      val pkgName = element["pkg"]!!.content
      val versionName = element["version"]!!.content
      val versionCode = element["code"]!!.int
      val lang = element["lang"]!!.content
      val apkName = element["apk"]!!.content
      val sourceId = element["id"]!!.long
      val description = element["description"]!!.content
      val nsfw = element["nsfw"]!!.booleanOrNull ?: false

      val apkUrl = "$repoUrl/apk/$apkName"
      val iconUrl = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"

      CatalogRemote(name, description, sourceId, pkgName, versionName, versionCode, lang, apkUrl,
        iconUrl, nsfw)
    }
  }

}
