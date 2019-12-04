/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import android.app.Application
import tachiyomi.data.AppDatabase
import javax.inject.Inject
import javax.inject.Provider

internal class RoomDatabaseProvider @Inject constructor(
  private val context: Application
) : Provider<AppDatabase> {

  override fun get(): AppDatabase {
    return AppDatabase.build(context)
  }

}
