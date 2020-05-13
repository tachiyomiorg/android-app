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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.service.CatalogStore

class GetUpdatableCatalogsTest : StringSpec({

  val store = mockk<CatalogStore>(relaxed = true)
  val interactor = GetUpdatableCatalogs(store)
  afterTest { clearAllMocks() }

  "calls the store" {
    interactor.get()
    verify { store.updatableCatalogs }
  }
  "returns updatable catalogs" {
    val catalog = mockk<CatalogInstalled>()
    every { store.updatableCatalogs } returns listOf(catalog)

    val result = interactor.get()
    result shouldHaveSize 1
    result[0] shouldBe catalog
  }

})
