/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.model

data class Track(
  val id: Long = 0,
  val mangaId: Long,
  val siteId: Int,
  val entryId: Long,
  val mediaId: Long = 0,
  val mediaUrl: String = "",
  val title: String = "",
  val lastRead: Float = 0f,
  val totalChapters: Int = 0,
  val score: Float = 0f,
  val status: TrackStatus = TrackStatus.Reading,
  val startReadTime: Long = 0,
  val endReadTime: Long = 0
)
