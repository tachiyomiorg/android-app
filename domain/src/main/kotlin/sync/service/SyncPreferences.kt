/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.sync.service

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore

class SyncPreferences(private val store: PreferenceStore) {

  fun address(): Preference<String> {
    return store.getString("address", "")
  }

  fun token(): Preference<String> {
    return store.getString("token", "")
  }

  // TODO: consider saving in common package as installation_id
  fun deviceId(): Preference<String> {
    return store.getString("device_id", "")
  }

}
