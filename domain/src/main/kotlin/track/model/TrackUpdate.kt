/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.model

data class TrackUpdate(
  val id: Long,
  val entryId: Long? = null,
  val mediaId: Long? = null,
  val mediaUrl: String? = null,
  val title: String? = null,
  val lastRead: Float? = null,
  val totalChapters: Int? = null,
  val score: Float? = null,
  val status: TrackStatus? = null,
  val startReadTime: Long? = null,
  val endReadTime: Long? = null
)
