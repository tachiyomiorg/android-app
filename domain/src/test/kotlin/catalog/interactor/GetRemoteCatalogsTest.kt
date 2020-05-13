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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import tachiyomi.domain.catalog.service.CatalogRemoteRepository

class GetRemoteCatalogsTest : StringSpec({

  val repository = mockk<CatalogRemoteRepository>(relaxed = true)
  val interactor = GetRemoteCatalogs(repository)
  afterTest { clearAllMocks() }

  "calls the repository" {
    interactor.await()
    coVerify { repository.getRemoteCatalogs() }
  }
  "subscribes the repository" {
    interactor.subscribe()
    verify { repository.getRemoteCatalogsFlow() }
  }
  "subscribe returns all catalogs" {
    every { repository.getRemoteCatalogsFlow() } returns flowOf(listOf(
      mockk { every { nsfw } returns true },
      mockk { every { nsfw } returns false }
    ))

    val catalogs = interactor.subscribe().first()
    catalogs shouldHaveSize 2
  }
  "subscribe filters out nsfw" {
    every { repository.getRemoteCatalogsFlow() } returns flowOf(listOf(
      mockk { every { nsfw } returns true },
      mockk { every { nsfw } returns false }
    ))

    val catalogs = interactor.subscribe(withNsfw = false).first()
    catalogs shouldHaveSize 1
  }

})
