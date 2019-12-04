/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

interface BaseDao<T> {

  @Insert
  suspend fun insert(obj: T): Long

  @Insert
  suspend fun insert(obj: List<T>): List<Long>

  @Update
  suspend fun update(obj: T): Int

  @Update
  suspend fun update(obj: List<T>): Int

  @Delete
  suspend fun delete(obj: T): Int

  @Delete
  suspend fun delete(objs: List<T>): Int

}
