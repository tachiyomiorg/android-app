/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.LibrarySorting
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryCategory @Inject constructor(
  private val libraryRepository: LibraryRepository) {

  fun execute(
    categoryId: Long,
    sort: LibrarySorting = LibrarySorting(LibrarySort.Title, true)
  ): List<LibraryManga> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.findAll(sort)
      Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized(sort)
      else -> libraryRepository.findForCategory(categoryId, sort)
    }
  }

  suspend fun await(
    categoryId: Long,
    sort: LibrarySorting = LibrarySorting(LibrarySort.Title, true)
  ): List<LibraryManga> {
    return withContext(Dispatchers.IO) {
      when (categoryId) {
        Category.ALL_ID -> libraryRepository.findAll(sort)
        Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized(sort)
        else -> libraryRepository.findForCategory(categoryId, sort)
      }
    }
  }

  fun subscribe(
    categoryId: Long,
    sort: LibrarySorting = LibrarySorting(LibrarySort.Title, true)
  ): Flow<List<LibraryManga>> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.subscribeAll(sort)
      Category.UNCATEGORIZED_ID -> libraryRepository.subscribeUncategorized(sort)
      else -> libraryRepository.subscribeToCategory(categoryId, sort)
    }
  }

}
