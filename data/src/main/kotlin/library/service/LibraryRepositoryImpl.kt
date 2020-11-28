/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.service

import kotlinx.coroutines.flow.Flow
import tachiyomi.data.AppDatabase
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.service.LibraryRepository
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  db: AppDatabase
) : LibraryRepository {

  private val dao = db.library

  override fun subscribeAll(sort: LibrarySort): Flow<List<LibraryManga>> {
    return dao.subscribeAll(sort)
  }

  override fun subscribeUncategorized(sort: LibrarySort): Flow<List<LibraryManga>> {
    return dao.subscribeUncategorized(sort)
  }

  override fun subscribeToCategory(
    categoryId: Long,
    sort: LibrarySort
  ): Flow<List<LibraryManga>> {
    return dao.subscribeCategory(categoryId, sort)
  }

  override suspend fun findAll(sort: LibrarySort): List<LibraryManga> {
    return dao.findAll(sort)
  }

  override suspend fun findUncategorized(sort: LibrarySort): List<LibraryManga> {
    return dao.findUncategorized(sort)
  }

  override suspend fun findForCategory(categoryId: Long, sort: LibrarySort): List<LibraryManga> {
    return dao.findCategory(categoryId, sort)
  }

  override suspend fun findFavoriteSourceIds(): List<Long> {
    return dao.findFavoriteSourceIds()
  }

}
