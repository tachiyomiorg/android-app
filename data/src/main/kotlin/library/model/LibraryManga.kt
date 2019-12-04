/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:Suppress("PackageDirectoryMismatch")

package tachiyomi.domain.library.model

import androidx.room.DatabaseView
import tachiyomi.domain.manga.model.MangaBase

@DatabaseView("SELECT * FROM manga WHERE favorite = 1", viewName = "library")
data class LibraryManga(
  override val id: Long,
  override val sourceId: Long,
  override val key: String,
  override val title: String,
  val status: Int,
  val cover: String,
  val lastUpdate: Long = 0,
  val unread: Int
) : MangaBase
