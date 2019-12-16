/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.service

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep

interface CatalogInstaller {

  fun install(catalog: CatalogRemote): Flow<InstallStep>

  suspend fun uninstall(pkgName: String): Boolean

}
