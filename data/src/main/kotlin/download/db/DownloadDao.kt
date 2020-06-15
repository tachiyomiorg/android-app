/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.download.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import tachiyomi.data.download.model.Download
import tachiyomi.data.download.model.DownloadProjection
import tachiyomi.data.manga.db.BaseDao

@Dao
abstract class DownloadDao : BaseDao<Download> {

  @Transaction
  @Query("SELECT * FROM download")
  abstract suspend fun findAll(): List<DownloadProjection>

  @Query("DELETE FROM download WHERE chapterId = :chapterId")
  abstract suspend fun delete(chapterId: Long)

}
