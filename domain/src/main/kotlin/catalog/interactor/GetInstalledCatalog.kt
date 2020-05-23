/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.service.CatalogStore
import javax.inject.Inject

class GetInstalledCatalog @Inject internal constructor(
  private val catalogStore: CatalogStore
) {

  fun get(pkgName: String): CatalogInstalled? {
    return catalogStore.catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }
      as? CatalogInstalled
  }

  fun subscribe(pkgName: String): Flow<CatalogInstalled?> {
    return catalogStore.getCatalogsFlow()
      .map { catalogs ->
        catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName } as? CatalogInstalled
      }
      .distinctUntilChanged()
  }

}
