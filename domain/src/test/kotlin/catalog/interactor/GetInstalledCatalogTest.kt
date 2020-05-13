/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.service.CatalogStore

class GetInstalledCatalogTest : StringSpec({

  val store = mockk<CatalogStore>(relaxed = true)
  val interactor = GetInstalledCatalog(store)
  afterTest { clearAllMocks() }

  "returns the requested catalog" {
    val catalog = mockCatalogInstalled("a.package")
    every { store.catalogs } returns listOf(catalog)

    val result = interactor.get("a.package")
    result shouldNotBe null
  }
  "returns null if the requested catalog isn't found" {
    val catalog = mockCatalogInstalled("a.package")
    every { store.catalogs } returns listOf(catalog)

    val result = interactor.get("b.package")
    result shouldBe null
  }
  "subscribes to the changes of the requested catalog" {
    val catalog = mockCatalogInstalled("a.package")
    every { store.getCatalogsFlow() } returns flowOf(listOf(catalog))

    val result = interactor.subscribe("a.package").toList()
    result.size shouldBe 1
    result[0] shouldBe catalog
  }
  "subscribes to the changes of the requested catalog and emit null when not found" {
    val catalog = mockCatalogInstalled("b.package")
    every { store.getCatalogsFlow() } returns flowOf(emptyList(), listOf(catalog))

    val result = interactor.subscribe("a.package").toList()
    result.size shouldBe 1
    result[0] shouldBe null
  }
  "subscribes to the changes of the requested catalog and ignores same emissions" {
    val catalog = mockCatalogInstalled("a.package")
    every { store.getCatalogsFlow() } returns flowOf(listOf(catalog), listOf(catalog))

    val result = interactor.subscribe("a.package").toList()
    result.size shouldBe 1
    result[0] shouldBe catalog
  }
  "subscribes to the changes of the requested catalog and emits new emissions" {
    val catalog = mockCatalogInstalled("a.package")
    val updatedCatalog = mockCatalogInstalled("a.package")
    every { store.getCatalogsFlow() } returns flowOf(listOf(catalog), listOf(updatedCatalog))

    val result = interactor.subscribe("a.package").toList()
    result.size shouldBe 2
    result[0] shouldBe catalog
    result[1] shouldBe updatedCatalog
  }

})

private fun mockCatalogInstalled(mockPkgName: String): CatalogInstalled {
  return mockk {
    every { pkgName } returns mockPkgName
  }
}
