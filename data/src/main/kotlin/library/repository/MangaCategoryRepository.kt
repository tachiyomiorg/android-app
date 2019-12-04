/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.repository

import tachiyomi.data.AppDatabase
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.library.repository.MangaCategoryRepository
import javax.inject.Inject

internal class MangaCategoryRepositoryImpl @Inject constructor(
  private val db: AppDatabase
) : MangaCategoryRepository {

  private val dao = db.mangaCategory

  override suspend fun replaceAll(mangaCategories: List<MangaCategory>) {
    dao.replaceAll(mangaCategories)
  }

  override suspend fun deleteForManga(mangaId: Long) {
    dao.deleteForManga(mangaId)
  }

  override suspend fun deleteForMangas(mangaIds: List<Long>) {
    dao.deleteForMangas(mangaIds)
  }

}
