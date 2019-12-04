/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.model

data class ChapterUpdate(
  val id: Long,
  val mangaId: Long? = null,
  val key: String? = null,
  val name: String? = null,
  val read: Boolean? = null,
  val bookmark: Boolean? = null,
  val progress: Int? = null,
  val dateUpload: Long? = null,
  val dateFetch: Long? = null,
  val sourceOrder: Int? = null,
  val number: Float? = null,
  val scanlator: String? = null
)
