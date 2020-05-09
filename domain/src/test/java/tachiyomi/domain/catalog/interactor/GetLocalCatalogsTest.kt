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
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogSort
import tachiyomi.domain.catalog.service.CatalogStore
import tachiyomi.domain.library.service.LibraryRepository

class GetLocalCatalogsTest : StringSpec({

  val store = mockk<CatalogStore>(relaxed = true)
  val libraryRepository = mockk<LibraryRepository>()
  val interactor = GetLocalCatalogs(store, libraryRepository)
  afterTest { clearAllMocks() }

  "called when subscribed" {
    interactor.subscribe(CatalogSort.Name)
    verify { store.getCatalogsFlow() }
  }
  "sorts by name" {
    every { store.getCatalogsFlow() } returns flowOf(listOf(
      mockCatalog("B"),
      mockCatalog("C"),
      mockCatalog("A")
    ))

    interactor.subscribe(CatalogSort.Name).collect { catalogs ->
      catalogs shouldHaveSize 3
      catalogs[0].name shouldBe "A"
      catalogs[1].name shouldBe "B"
      catalogs[2].name shouldBe "C"
    }
  }
  "sorts by favorites then name" {
    every { store.getCatalogsFlow() } returns flowOf(listOf(
      mockCatalog("B", 2),
      mockCatalog("D", 4),
      mockCatalog("C", 3),
      mockCatalog("A", 1)
    ))

    coEvery { libraryRepository.findFavoriteSourceIds() } returns listOf(2, 3)

    interactor.subscribe(CatalogSort.Favorites).collect { catalogs ->
      catalogs shouldHaveSize 4
      catalogs[0].sourceId shouldBe 2
      catalogs[1].sourceId shouldBe 3
      catalogs[2].name shouldBe "A"
      catalogs[3].name shouldBe "D"
    }
  }

})

private fun mockCatalog(mockName: String, mockSourceId: Long = 0): CatalogLocal {
  return mockk {
    every { name } returns mockName
    every { sourceId } returns mockSourceId
  }
}
