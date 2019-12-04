/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.model

data class MangaUpdate(
  val id: Long,
  val sourceId: Long? = null,
  val key: String? = null,
  val title: String? = null,
  val artist: String? = null,
  val author: String? = null,
  val description: String? = null,
  val genres: Genres? = null,
  val status: Int? = null,
  val cover: String? = null,
  val favorite: Boolean? = null,
  val lastUpdate: Long? = null,
  val lastInit: Long? = null,
  val dateAdded: Long? = null,
  val viewer: Int? = null,
  val flags: Int? = null
)
