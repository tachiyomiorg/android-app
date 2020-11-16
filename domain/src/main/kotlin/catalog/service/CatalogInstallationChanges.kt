/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.service

import kotlinx.coroutines.flow.Flow

interface CatalogInstallationChanges {
  val flow: Flow<CatalogInstallationChange>
}

sealed class CatalogInstallationChange {
  abstract val pkgName: String

  data class SystemInstall(override val pkgName: String) : CatalogInstallationChange()
  data class SystemUninstall(override val pkgName: String) : CatalogInstallationChange()

  data class LocalInstall(override val pkgName: String) : CatalogInstallationChange()
  data class LocalUninstall(override val pkgName: String) : CatalogInstallationChange()
}
