/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import java.io.File

class DownloadPreferences(
  private val preferenceStore: PreferenceStore,
  private val defaultDownloadsDir: File
) {

  fun downloadsDir(): Preference<File> {
    return preferenceStore.getObject(
      key = "downloads_dir",
      defaultValue = defaultDownloadsDir,
      serializer = { it.absolutePath },
      deserializer = { File(it) }
    )
  }

  fun compress(): Preference<Boolean> {
    return preferenceStore.getBoolean("compress", false)
  }

}
