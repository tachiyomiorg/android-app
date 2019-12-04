/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import tachiyomi.data.manga.db.BaseDao
import tachiyomi.domain.catalog.model.CatalogRemote

@Dao
abstract class CatalogRemoteDao : BaseDao<CatalogRemote> {

  @Query("SELECT * FROM catalogRemote ORDER BY lang, name")
  abstract suspend fun findAll(): List<CatalogRemote>

  @Query("DELETE FROM catalogRemote")
  abstract suspend fun deleteAll()

  @Transaction
  open suspend fun replaceAll(catalogs: List<CatalogRemote>) {
    deleteAll()
    insert(catalogs)
  }

}
