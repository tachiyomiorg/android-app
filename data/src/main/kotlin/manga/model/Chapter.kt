/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:Suppress("PackageDirectoryMismatch")

package tachiyomi.domain.manga.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
  ForeignKey(
    entity = Manga::class,
    parentColumns = ["id"],
    childColumns = ["mangaId"],
    onDelete = ForeignKey.CASCADE)
], indices = [
  Index(name = "manga_chapters_idx", value = ["mangaId"])
])
data class Chapter(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
)
