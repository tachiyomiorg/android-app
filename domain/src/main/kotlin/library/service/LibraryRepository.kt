/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort

interface LibraryRepository {

  fun subscribeAll(sort: LibrarySort): Flow<List<LibraryManga>>

  fun subscribeUncategorized(sort: LibrarySort): Flow<List<LibraryManga>>

  fun subscribeToCategory(categoryId: Long, sort: LibrarySort): Flow<List<LibraryManga>>

  suspend fun findAll(sort: LibrarySort): List<LibraryManga>

  suspend fun findUncategorized(sort: LibrarySort): List<LibraryManga>

  suspend fun findForCategory(categoryId: Long, sort: LibrarySort): List<LibraryManga>

  suspend fun findFavoriteSourceIds(): List<Long>

}
