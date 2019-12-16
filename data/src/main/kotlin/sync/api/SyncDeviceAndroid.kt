/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.sync.api

import android.os.Build
import tachiyomi.domain.sync.api.SyncDevice
import tachiyomi.domain.sync.service.SyncPreferences
import java.util.UUID
import javax.inject.Inject

internal class SyncDeviceAndroid @Inject constructor(
  private val preferences: SyncPreferences
) : SyncDevice {

  // TODO: this should be common for each platform, there's no need to implement here
  override fun getId(): String {
    val preference = preferences.deviceId()
    if (preference.isSet()) {
      return preference.get()
    }

    val newId = UUID.randomUUID().toString()
    preference.set(newId)
    return newId
  }

  override fun getName(): String {
    return Build.MODEL
  }

  override fun getPlatform(): String {
    return "ANDROID"
  }

}
