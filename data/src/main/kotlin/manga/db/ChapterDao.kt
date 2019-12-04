/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate

@Dao
abstract class ChapterDao : BaseDao<Chapter> {

  @Query("SELECT * FROM chapter WHERE mangaId = :mangaId")
  abstract fun subscribeForManga(mangaId: Long): Flow<List<Chapter>>

  @Query("SELECT * FROM chapter WHERE id = :chapterId")
  abstract fun subscribe(chapterId: Long): Flow<Chapter?>

  @Query("SELECT * FROM chapter WHERE mangaId = :mangaId")
  abstract suspend fun findForManga(mangaId: Long): List<Chapter>

  @Query("SELECT * FROM chapter WHERE id = :chapterId")
  abstract suspend fun find(chapterId: Long): Chapter?

  @Query("SELECT * FROM chapter WHERE mangaId = :mangaId AND `key` = :chapterKey")
  abstract suspend fun find(chapterKey: String, mangaId: Long): Chapter?

  @Query("""UPDATE chapter SET
    `key` = coalesce(:key, `key`),
    name = coalesce(:name, name),
    read = coalesce(:read, read),
    bookmark = coalesce(:bookmark, bookmark),
    progress = coalesce(:progress, progress),
    dateUpload = coalesce(:dateUpload, dateUpload),
    dateFetch = coalesce(:dateFetch, dateFetch),
    sourceOrder = coalesce(:sourceOrder, sourceOrder),
    number = coalesce(:number, number),
    scanlator = coalesce(:scanlator, scanlator)
    WHERE id = :id"""
  )
  abstract suspend fun updatePartial(
    id: Long,
    key: String? = null,
    name: String? = null,
    read: Boolean? = null,
    bookmark: Boolean? = null,
    progress: Int? = null,
    dateUpload: Long? = null,
    dateFetch: Long? = null,
    sourceOrder: Int? = null,
    number: Float? = null,
    scanlator: String? = null
  )

  suspend fun updatePartial(update: ChapterUpdate) {
    updatePartial(
      id = update.id,
      key = update.key,
      name = update.name,
      read = update.read,
      bookmark = update.bookmark,
      dateUpload = update.dateUpload,
      dateFetch = update.dateFetch,
      sourceOrder = update.sourceOrder,
      number = update.number,
      scanlator = update.scanlator
    )
  }

  @Transaction
  open suspend fun updatePartial(updates: List<ChapterUpdate>) {
    for (update in updates) {
      updatePartial(update)
    }
  }

  @Query("UPDATE chapter SET sourceOrder = :order WHERE `key` = :key AND mangaId = :mangaId")
  protected abstract suspend fun updateOrder(key: String, mangaId: Long, order: Int)

  @Transaction
  open suspend fun updateOrder(chapters: List<Chapter>) {
    for (chapter in chapters) {
      updateOrder(chapter.key, chapter.mangaId, chapter.sourceOrder)
    }
  }

}
