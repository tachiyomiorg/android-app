/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.download.service

import tachiyomi.domain.download.model.SavedDownload

interface DownloadRepository {

  suspend fun findAll(): List<SavedDownload>

  suspend fun insert(downloads: List<SavedDownload>)

  suspend fun delete(downloads: List<SavedDownload>)

  suspend fun delete(chapterId: Long)

}
