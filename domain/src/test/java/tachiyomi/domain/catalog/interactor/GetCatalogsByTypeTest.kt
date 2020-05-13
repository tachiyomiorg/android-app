/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote

class GetCatalogsByTypeTest : StringSpec({

  val localCatalogs = mockk<GetLocalCatalogs>(relaxed = true)
  val remoteCatalogs = mockk<GetRemoteCatalogs>(relaxed = true)
  val updatableCatalogs = mockk<GetUpdatableCatalogs>(relaxed = true)
  val interactor = GetCatalogsByType(localCatalogs, remoteCatalogs, updatableCatalogs)
  afterTest { clearAllMocks() }

  "subscribes to catalogs" {
    coEvery { localCatalogs.subscribe(any()) } returns flowOf()
    interactor.subscribe()
    coVerify { localCatalogs.subscribe(any()) }
    coVerify { remoteCatalogs.subscribe(any()) }
  }
  "returns all catalogs" {
    coEvery { localCatalogs.subscribe(any()) } returns flowOf(listOf(
      mockCatalogInstalled("a"),
      mockCatalogInstalled("b")
    ))
    coEvery { remoteCatalogs.subscribe() } returns flowOf(listOf(
      mockCatalogRemote("a"),
      mockCatalogRemote("c")
    ))

    val (upToDate, _, remote) = interactor.subscribe(excludeRemoteInstalled = false).first()
    upToDate shouldHaveSize 2
    remote shouldHaveSize 2
  }
  "filters remote installed" {
    coEvery { localCatalogs.subscribe(any()) } returns flowOf(listOf(
      mockCatalogInstalled("a"),
      mockCatalogInstalled("b")
    ))
    coEvery { remoteCatalogs.subscribe() } returns flowOf(listOf(
      mockCatalogRemote("a"),
      mockCatalogRemote("c")
    ))

    val (upToDate, _, remote) = interactor.subscribe(excludeRemoteInstalled = true).first()
    upToDate shouldHaveSize 2
    remote shouldHaveSize 1
  }
  "filters up to date catalogs when present in updatable" {
    val sameCatalogMock = mockCatalogInstalled("a")
    coEvery { localCatalogs.subscribe(any()) } returns flowOf(listOf(
      sameCatalogMock,
      mockCatalogInstalled("b")
    ))
    coEvery { updatableCatalogs.get() } returns listOf(
      sameCatalogMock
    )
    coEvery { remoteCatalogs.subscribe() } returns flowOf(emptyList())

    val (upToDate, updatable, _) = interactor.subscribe().first()
    upToDate shouldHaveSize 1
    updatable shouldHaveSize 1
  }

})

private fun mockCatalogInstalled(mockPkgName: String): CatalogInstalled {
  return mockk {
    every { pkgName } returns mockPkgName
  }
}

private fun mockCatalogRemote(mockPkgName: String): CatalogRemote {
  return mockk {
    every { pkgName } returns mockPkgName
  }
}
