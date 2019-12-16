/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogSort
import tachiyomi.domain.catalog.service.CatalogStore
import tachiyomi.domain.library.service.LibraryRepository
import javax.inject.Inject

class GetLocalCatalogs @Inject constructor(
  private val catalogStore: CatalogStore,
  private val libraryRepository: LibraryRepository
) {

  suspend fun subscribe(sort: CatalogSort = CatalogSort.Favorites): Flow<List<CatalogLocal>> {
    val flow = catalogStore.getCatalogsFlow()

    return when (sort) {
      CatalogSort.Name -> sortByName(flow)
      CatalogSort.Favorites -> sortByFavorites(flow)
    }
  }

  private fun sortByName(catalogsFlow: Flow<List<CatalogLocal>>): Flow<List<CatalogLocal>> {
    return catalogsFlow.map { catalogs ->
      catalogs.sortedBy { it.name }
    }
  }

  private suspend fun sortByFavorites(
    catalogsFlow: Flow<List<CatalogLocal>>
  ): Flow<List<CatalogLocal>> {
    var position = 0
    val favoriteIds = libraryRepository.findFavoriteSourceIds().associateWith { position++ }

    return catalogsFlow.map { catalogs ->
      catalogs.sortedWith(FavoritesComparator(favoriteIds).thenBy { it.name })
    }
  }

  private class FavoritesComparator(val favoriteIds: Map<Long, Int>) : Comparator<CatalogLocal> {

    override fun compare(c1: CatalogLocal, c2: CatalogLocal): Int {
      val pos1 = favoriteIds[c1.source.id] ?: Int.MAX_VALUE
      val pos2 = favoriteIds[c2.source.id] ?: Int.MAX_VALUE

      return pos1.compareTo(pos2)
    }

  }

}
