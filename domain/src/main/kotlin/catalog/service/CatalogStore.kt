/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogStore @Inject constructor(
  private val loader: CatalogLoader,
  catalogRemoteRepository: CatalogRemoteRepository,
  installationChanges: CatalogInstallationChanges
) {

  var catalogs = emptyList<CatalogLocal>()
    private set(value) {
      field = value
      updatableCatalogs = field.filterUpdatable()
      catalogsBySource = field.associateBy { it.sourceId }
      catalogsFlow.value = field
    }

  var updatableCatalogs = emptyList<CatalogInstalled>()
    private set

  private var remoteCatalogs = emptyList<CatalogRemote>()

  private var catalogsBySource = emptyMap<Long, CatalogLocal>()

  private val catalogsFlow = MutableStateFlow(catalogs)

  init {
    catalogs = loader.loadAll()

    installationChanges.flow
      .onEach { change ->
        when (change) {
          is CatalogInstallationChange.SystemInstall -> onInstalled(change.pkgName, false)
          is CatalogInstallationChange.SystemUninstall -> onUninstalled(change.pkgName, false)
          is CatalogInstallationChange.LocalInstall -> onInstalled(change.pkgName, true)
          is CatalogInstallationChange.LocalUninstall -> onUninstalled(change.pkgName, true)
        }
      }
      .launchIn(GlobalScope)

    catalogRemoteRepository.getRemoteCatalogsFlow()
      .onEach {
        remoteCatalogs = it
        synchronized(this@CatalogStore) {
          catalogs = catalogs // Force an update check
        }
      }
      .launchIn(GlobalScope)
  }

  fun get(sourceId: Long): CatalogLocal? {
    return catalogsBySource[sourceId]
  }

  fun getCatalogsFlow(): Flow<List<CatalogLocal>> {
    return catalogsFlow
  }

  private fun List<CatalogLocal>.filterUpdatable(): List<CatalogInstalled> {
    val catalogs = mutableListOf<CatalogInstalled>()
    val remoteCatalogs = remoteCatalogs
    for (installedCatalog in this) {
      if (installedCatalog !is CatalogInstalled) continue

      val pkgName = installedCatalog.pkgName
      val remoteCatalog = remoteCatalogs.find { it.pkgName == pkgName } ?: continue

      val hasUpdate = remoteCatalog.versionCode > installedCatalog.versionCode
      if (hasUpdate) {
        catalogs.add(installedCatalog)
      }
    }
    return catalogs
  }

  private fun onInstalled(pkgName: String, isLocalInstall: Boolean) {
    GlobalScope.launch(Dispatchers.Default) {
      synchronized(this@CatalogStore) {
        val previousCatalog = catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }

        // Don't replace system catalogs with local catalogs
        if (!isLocalInstall && previousCatalog is CatalogInstalled.Locally) {
          return@launch
        }

        val catalog = if (isLocalInstall) {
          loader.loadLocalCatalog(pkgName)
        } else {
          loader.loadSystemCatalog(pkgName)
        } ?: return@launch

        val newInstalledCatalogs = catalogs.toMutableList()
        if (previousCatalog != null) {
          newInstalledCatalogs -= previousCatalog
        }
        newInstalledCatalogs += catalog
        catalogs = newInstalledCatalogs
      }
    }
  }

  private fun onUninstalled(pkgName: String, isLocalInstall: Boolean) {
    GlobalScope.launch(Dispatchers.Default) {
      synchronized(this@CatalogStore) {
        val installedCatalog = catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }
        if (installedCatalog != null &&
          installedCatalog is CatalogInstalled.Locally == isLocalInstall
        ) {
          catalogs = catalogs - installedCatalog
        }
      }
    }
  }

}
