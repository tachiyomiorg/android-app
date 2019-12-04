/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:Suppress("PackageDirectoryMismatch")

package tachiyomi.domain.library.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import tachiyomi.domain.manga.model.Manga

@Entity(
  primaryKeys = ["mangaId", "categoryId"],
  foreignKeys = [
    ForeignKey(
      entity = Manga::class,
      parentColumns = ["id"],
      childColumns = ["mangaId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = Category::class,
      parentColumns = ["id"],
      childColumns = ["categoryId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(name = "mangacategory_categories_idx", value = ["categoryId"])
  ]
)
data class MangaCategory(
  val mangaId: Long,
  val categoryId: Long
)
