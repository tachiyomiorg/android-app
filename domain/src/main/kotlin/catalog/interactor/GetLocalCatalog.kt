/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.service.CatalogStore
import javax.inject.Inject

class GetLocalCatalog @Inject constructor(
  private val store: CatalogStore
) {

  fun get(sourceId: Long): CatalogLocal? {
    return store.get(sourceId)
  }

}
