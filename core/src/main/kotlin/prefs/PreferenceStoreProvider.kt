/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager

class PreferenceStoreFactory(private val context: Application) {

  fun create(name: String? = null): PreferenceStore {
    val sharedPreferences = if (!name.isNullOrBlank()) {
      context.getSharedPreferences(name, Context.MODE_PRIVATE)
    } else {
      PreferenceManager.getDefaultSharedPreferences(context)
    }
    return AndroidPreferenceStore(sharedPreferences)
  }

}
