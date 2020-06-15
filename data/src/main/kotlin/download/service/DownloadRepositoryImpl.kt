/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.download.service

import tachiyomi.data.AppDatabase
import tachiyomi.domain.download.model.SavedDownload
import tachiyomi.domain.download.service.DownloadRepository
import javax.inject.Inject

class DownloadRepositoryImpl @Inject constructor(
  db: AppDatabase
) : DownloadRepository {

  private val dao = db.download

  override suspend fun findAll(): List<SavedDownload> {
    return dao.findAll().map {
      SavedDownload(
        chapterId = it.chapterId,
        mangaId = it.mangaId,
        priority = it.priority,
        sourceId = it.manga.sourceId,
        mangaName = it.manga.title,
        chapterKey = it.chapter.key,
        chapterName = it.chapter.name,
        scanlator = it.chapter.scanlator
      )
    }
  }

  override suspend fun insert(downloads: List<SavedDownload>) {
    TODO("not implemented")
  }

  override suspend fun delete(downloads: List<SavedDownload>) {
    TODO("not implemented")
  }

  override suspend fun delete(chapterId: Long) {
    dao.delete(chapterId)
  }

}
