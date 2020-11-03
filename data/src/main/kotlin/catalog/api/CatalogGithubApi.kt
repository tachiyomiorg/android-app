/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.api

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import tachiyomi.core.http.Http
import tachiyomi.core.http.awaitBody
import tachiyomi.core.http.get
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogRemoteApi
import javax.inject.Inject

internal class CatalogGithubApi @Inject constructor(private val http: Http) : CatalogRemoteApi {

  private val repoUrl = "https://raw.githubusercontent.com/tachiyomiorg/extensions/repo"

  override suspend fun fetchCatalogs(): List<CatalogRemote> {
    val body = http.defaultClient.get("$repoUrl/index.min.json").awaitBody()
    val json = Json.Default.decodeFromString<JsonArray>(body)
    return json.map { element ->
      element as JsonObject
      val name = element["name"]!!.jsonPrimitive.content
      val pkgName = element["pkg"]!!.jsonPrimitive.content
      val versionName = element["version"]!!.jsonPrimitive.content
      val versionCode = element["code"]!!.jsonPrimitive.int
      val lang = element["lang"]!!.jsonPrimitive.content
      val apkName = element["apk"]!!.jsonPrimitive.content
      val sourceId = element["id"]!!.jsonPrimitive.long
      val description = element["description"]!!.jsonPrimitive.content
      val nsfw = element["nsfw"]!!.jsonPrimitive.booleanOrNull ?: false

      val apkUrl = "$repoUrl/apk/$apkName"
      val iconUrl = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"

      CatalogRemote(name, description, sourceId, pkgName, versionName, versionCode, lang, apkUrl,
        iconUrl, nsfw)
    }
  }

}
