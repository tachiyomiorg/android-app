/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.CatalogSort
import javax.inject.Inject

class GetCatalogsByType @Inject constructor(
  private val localCatalogs: GetLocalCatalogs,
  private val remoteCatalogs: GetRemoteCatalogs,
  private val updatableCatalogs: GetUpdatableCatalogs
) {

  suspend fun subscribe(
    sort: CatalogSort = CatalogSort.Favorites,
    excludeRemoteInstalled: Boolean = false,
    withNsfw: Boolean = true
  ): Flow<Catalogs> {
    val localFlow = localCatalogs.subscribe(sort)
    val remoteFlow = remoteCatalogs.subscribe(withNsfw = withNsfw)
    return localFlow.combine(remoteFlow) { local, remote ->
      val updatable = updatableCatalogs.get()
      val upToDate = local.filter { it !in updatable }

      if (excludeRemoteInstalled) {
        val installedPkgs = local
          .asSequence()
          .filterIsInstance<CatalogInstalled>()
          .map { it.pkgName }
          .toSet()

        Catalogs(upToDate, updatable, remote.filter { it.pkgName !in installedPkgs })
      } else {
        Catalogs(upToDate, updatable, remote)
      }
    }
  }

  data class Catalogs(
    val upToDate: List<CatalogLocal>,
    val updatable: List<CatalogInstalled>,
    val remote: List<CatalogRemote>
  )

}
