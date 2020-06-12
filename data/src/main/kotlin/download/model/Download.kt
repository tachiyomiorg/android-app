/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.download.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = Chapter::class,
      parentColumns = ["id"],
      childColumns = ["chapterId"],
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class Download(
  @PrimaryKey val chapterId: Long,
  val mangaId: Long,
  val priority: Int
)

data class DownloadProjection(
  val chapterId: Long,
  val mangaId: Long,
  val priority: Int,
  @Relation(entity = Manga::class, parentColumn = "mangaId", entityColumn = "id")
  val manga: MangaData,
  @Relation(entity = Chapter::class, parentColumn = "chapterId", entityColumn = "id")
  val chapter: ChapterData
) {

  data class MangaData(val sourceId: Long, val title: String)

  data class ChapterData(val key: String, val name: String, val scanlator: String)
}
