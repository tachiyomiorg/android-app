/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import tachiyomi.data.manga.db.BaseDao
import tachiyomi.domain.library.model.MangaCategory

@Dao
abstract class MangaCategoryDao : BaseDao<MangaCategory> {

  @Transaction
  open suspend fun replaceAll(mangaCategories: List<MangaCategory>) {
    deleteForMangas(mangaCategories.asSequence().map { it.mangaId }.distinct().toList())
    insert(mangaCategories)
  }

  @Transaction
  open suspend fun deleteForMangas(mangaIds: List<Long>) {
    for (mangaId in mangaIds) {
      deleteForManga(mangaId)
    }
  }

  @Query("DELETE FROM mangaCategory WHERE mangaId = :mangaId")
  abstract suspend fun deleteForManga(mangaId: Long)

}
