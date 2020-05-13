/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.kotest.core.spec.style.StringSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import tachiyomi.core.prefs.Preference
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogPreferences
import tachiyomi.domain.catalog.service.CatalogRemoteApi
import tachiyomi.domain.catalog.service.CatalogRemoteRepository

class SyncRemoteCatalogsTest : StringSpec({

  val repository = mockk<CatalogRemoteRepository>(relaxed = true)
  val api = mockk<CatalogRemoteApi>(relaxed = true)
  val preferences = mockk<CatalogPreferences>(relaxed = true)
  val interactor = SyncRemoteCatalogs(repository, api, preferences)
  afterTest { clearAllMocks() }

  "checks for updates" {
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck()
    interactor.await(true)
    coVerify { api.fetchCatalogs() }
  }
  "doesn't check for updates too quickly" {
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck()
    interactor.await(false)
    coVerify(exactly = 0) { api.fetchCatalogs() }
  }
  "checks for updates if some time has passed" {
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck(
      System.currentTimeMillis() - SyncRemoteCatalogs.minTimeApiCheck - 1000
    )
    interactor.await(false)
    coVerify { api.fetchCatalogs() }
  }
  "doesn't check for updates if not enough time has passed" {
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck(
      System.currentTimeMillis() - SyncRemoteCatalogs.minTimeApiCheck + 1000
    )
    interactor.await(false)
    coVerify(exactly = 0) { api.fetchCatalogs() }
  }
  "saves the new catalogs if operation succeeds" {
    val catalog = mockk<CatalogRemote>()
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck()
    coEvery { api.fetchCatalogs() } returns listOf(catalog)

    interactor.await(true)

    coVerify { repository.setRemoteCatalogs(listOf(catalog)) }
  }
  "doesn't save catalogs if network operation fails" {
    every { preferences.lastRemoteCheck() } returns mockLastRemoteCheck()
    coEvery { api.fetchCatalogs() } throws Exception()

    interactor.await(true)

    coVerify(exactly = 0) { repository.setRemoteCatalogs(any()) }
  }
  "sets last check if operation succeeds" {
    val preference = mockLastRemoteCheck()
    val catalog = mockk<CatalogRemote>()
    every { preferences.lastRemoteCheck() } returns preference
    coEvery { api.fetchCatalogs() } returns listOf(catalog)

    interactor.await(true)

    coVerify { preference.set(any()) }
  }
  "doesn't set last check if network operation fails" {
    val preference = mockLastRemoteCheck()
    every { preferences.lastRemoteCheck() } returns preference
    coEvery { api.fetchCatalogs() } throws Exception()

    interactor.await(true)

    coVerify(exactly = 0) { preference.set(any()) }
  }

})

private fun mockLastRemoteCheck(time: Long = System.currentTimeMillis()): Preference<Long> {
  return mockk(relaxed = true) {
    every { get() } returns time
  }
}

private fun mockCatalogRemote(): CatalogRemote {
  return mockk()
}
