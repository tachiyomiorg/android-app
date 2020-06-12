/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.model

data class SavedDownload(
  val chapterId: Long,
  val mangaId: Long,
  val priority: Int,
  val sourceId: Long,
  val mangaName: String,
  val chapterKey: String,
  val chapterName: String,
  val scanlator: String
)
