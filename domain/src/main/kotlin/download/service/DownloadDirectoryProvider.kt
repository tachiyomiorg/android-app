/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import tachiyomi.domain.download.model.QueuedDownload
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is used to provide the directories where the downloads should be saved.
 * It uses the following path scheme: /<root downloads dir>/<source name>/<manga>/<chapter>
 */
@Singleton
class DownloadDirectoryProvider @Inject internal constructor(
  private val preferences: DownloadPreferences
) {

  val downloadsDir = preferences.downloadsDir().get() // TODO update value

  fun getChapterDir(download: QueuedDownload): File {
    TODO()
    //return File()
  }

  internal fun getTempChapterDir(download: QueuedDownload): File {
    TODO()
    //return File("_tmp")
  }

}
