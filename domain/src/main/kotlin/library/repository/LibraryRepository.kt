/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySorting

interface LibraryRepository {

  fun subscribeAll(sort: LibrarySorting): Flow<List<LibraryManga>>

  fun subscribeUncategorized(sort: LibrarySorting): Flow<List<LibraryManga>>

  fun subscribeToCategory(categoryId: Long, sort: LibrarySorting): Flow<List<LibraryManga>>

  suspend fun findAll(sort: LibrarySorting): List<LibraryManga>

  suspend fun findUncategorized(sort: LibrarySorting): List<LibraryManga>

  suspend fun findForCategory(categoryId: Long, sort: LibrarySorting): List<LibraryManga>

  suspend fun findFavoriteSourceIds(): List<Long>

}
