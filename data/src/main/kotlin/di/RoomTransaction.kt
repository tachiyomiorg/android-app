/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import androidx.room.withTransaction
import tachiyomi.core.db.Transaction
import tachiyomi.data.AppDatabase
import javax.inject.Inject

class RoomTransaction @Inject constructor(private val db: AppDatabase) : Transaction {

  override suspend fun <T> withAction(action: suspend () -> T?) {
    db.withTransaction(action)
  }

}
