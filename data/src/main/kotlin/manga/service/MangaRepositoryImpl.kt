/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tachiyomi.data.AppDatabase
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.service.MangaRepository
import javax.inject.Inject

internal class MangaRepositoryImpl @Inject constructor(
  db: AppDatabase
) : MangaRepository {

  private val dao = db.manga

  override fun subscribe(mangaId: Long): Flow<Manga?> {
    return dao.subscribe(mangaId).distinctUntilChanged()
  }

  override fun subscribe(key: String, sourceId: Long): Flow<Manga?> {
    return dao.subscribe(key, sourceId).distinctUntilChanged()
  }

  override suspend fun findFavorites(): List<Manga> {
    return dao.getFavorites()
  }

  override suspend fun find(mangaId: Long): Manga? {
    return dao.find(mangaId)
  }

  override suspend fun find(key: String, sourceId: Long): Manga? {
    return dao.find(key, sourceId)
  }

  override suspend fun insert(manga: Manga): Long {
    return dao.insert(manga)
  }

  override suspend fun updatePartial(update: MangaUpdate) {
    dao.update(update)
  }

  override suspend fun deleteNonFavorite() {
    dao.deleteNonFavorite()
  }

}
