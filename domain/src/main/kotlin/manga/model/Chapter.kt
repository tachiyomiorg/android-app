/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.model

data class Chapter(
  val id: Long = 0,
  val mangaId: Long = 0,
  val key: String,
  val name: String,
  val read: Boolean = false,
  val bookmark: Boolean = false,
  val progress: Int = 0,
  val dateUpload: Long = 0,
  val dateFetch: Long = 0,
  val sourceOrder: Int = 0,
  val number: Float = -1f,
  val scanlator: String = ""
) {

  val isRecognizedNumber get() = number >= 0

}
