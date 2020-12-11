/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryFilter.Type.Completed
import tachiyomi.domain.library.model.LibraryFilter.Type.Downloaded
import tachiyomi.domain.library.model.LibraryFilter.Type.Unread
import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.service.LibraryRepository
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetLibraryCategory @Inject internal constructor(
  private val libraryRepository: LibraryRepository
) {

  suspend fun await(
    categoryId: Long,
    sort: LibrarySort = LibrarySort.default,
    filters: List<LibraryFilter> = emptyList()
  ): List<LibraryManga> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.findAll(sort)
      Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized(sort)
      else -> libraryRepository.findForCategory(categoryId, sort)
    }.filteredWith(filters)
  }

  fun subscribe(
    categoryId: Long,
    sort: LibrarySort = LibrarySort.default,
    filters: List<LibraryFilter> = emptyList()
  ): Flow<List<LibraryManga>> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.subscribeAll(sort)
      Category.UNCATEGORIZED_ID -> libraryRepository.subscribeUncategorized(sort)
      else -> libraryRepository.subscribeToCategory(categoryId, sort)
    }.map { it.filteredWith(filters) }
  }

  private fun List<LibraryManga>.filteredWith(filters: List<LibraryFilter>): List<LibraryManga> {
    if (filters.isEmpty()) return this

    var filteredList = this
    for (filter in filters) {
      val filterFn: (LibraryManga) -> Boolean = when (filter.type) {
        Unread -> {
          { it.unread > 0 }
        }
        Completed -> {
          { it.status == MangaInfo.COMPLETED }
        }
        Downloaded -> {
          { false /* TODO */ }
        }
      }
      filteredList = when (filter.value) {
        Included -> filter(filterFn)
        Excluded -> filterNot(filterFn)
        Missing -> this
      }
    }

    return filteredList
  }

}
