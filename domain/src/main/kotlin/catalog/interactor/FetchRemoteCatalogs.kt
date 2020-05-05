/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import tachiyomi.domain.catalog.service.CatalogPreferences
import tachiyomi.domain.catalog.service.CatalogRemoteApi
import tachiyomi.domain.catalog.service.CatalogRemoteRepository
import timber.log.Timber
import timber.log.warn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FetchRemoteCatalogs @Inject constructor(
  private val catalogRemoteRepository: CatalogRemoteRepository,
  private val catalogRemoteApi: CatalogRemoteApi,
  private val catalogPreferences: CatalogPreferences
) {

  suspend fun await(forceRefresh: Boolean): Boolean {
    val remoteCheckPref = catalogPreferences.lastRemoteCheck()
    val lastCheck = remoteCheckPref.get()

    if (forceRefresh || System.currentTimeMillis() - lastCheck > minTimeApiCheck) {
      try {
        val newCatalogs = catalogRemoteApi.findCatalogs()
        catalogRemoteRepository.setRemoteCatalogs(newCatalogs)
        remoteCheckPref.set(System.currentTimeMillis())
        return true
      } catch (e: Exception) {
        Timber.warn { "Failed to fetch remote catalogs: $e" }
      }
    }

    return false
  }

  private companion object {
    val minTimeApiCheck = TimeUnit.MINUTES.toMillis(5)
  }

}
