/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tachiyomi.data.AppDatabase
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate
import tachiyomi.domain.manga.repository.ChapterRepository
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
  db: AppDatabase
) : ChapterRepository {

  private val dao = db.chapter

  override fun subscribeForManga(mangaId: Long): Flow<List<Chapter>> {
    return dao.subscribeForManga(mangaId).distinctUntilChanged()
  }

  override fun subscribe(chapterId: Long): Flow<Chapter?> {
    return dao.subscribe(chapterId).distinctUntilChanged()
  }

  override suspend fun findForManga(mangaId: Long): List<Chapter> {
    return dao.findForManga(mangaId)
  }

  override suspend fun find(chapterId: Long): Chapter? {
    return dao.find(chapterId)
  }

  override suspend fun find(chapterKey: String, mangaId: Long): Chapter? {
    return dao.find(chapterKey, mangaId)
  }

  override suspend fun insert(chapters: List<Chapter>) {
    dao.insert(chapters)
  }

  override suspend fun update(chapters: List<Chapter>) {
    dao.update(chapters)
  }

  override suspend fun updatePartial(updates: List<ChapterUpdate>) {
    dao.updatePartial(updates)
  }

  override suspend fun updateOrder(chapters: List<Chapter>) {
    dao.updateOrder(chapters)
  }

  override suspend fun delete(chapters: List<Chapter>) {
    dao.delete(chapters)
  }

}
