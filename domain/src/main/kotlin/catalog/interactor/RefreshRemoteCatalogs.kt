/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import tachiyomi.domain.catalog.service.CatalogRemoteRepository
import javax.inject.Inject

class RefreshRemoteCatalogs @Inject constructor(
  private val catalogRemoteRepository: CatalogRemoteRepository
) {

  suspend fun await(forceRefresh: Boolean) {
    return catalogRemoteRepository.refreshRemoteCatalogs(forceRefresh)
  }

}
