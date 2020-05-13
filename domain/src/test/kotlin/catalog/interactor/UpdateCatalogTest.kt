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
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogRemoteRepository

class UpdateCatalogTest : StringSpec({

  val repository = mockk<CatalogRemoteRepository>(relaxed = true)
  val install = mockk<InstallCatalog>(relaxed = true)
  val interactor = UpdateCatalog(repository, install)
  afterTest { clearAllMocks() }

  "updates the installed catalog if an update is found" {
    val toUpdate = mockk<CatalogInstalled> {
      every { pkgName } returns "a.package"
    }
    val newCatalog = mockk<CatalogRemote> {
      every { pkgName } returns "a.package"
    }
    coEvery { repository.getRemoteCatalogs() } returns listOf(newCatalog)

    interactor.await(toUpdate)
    coVerify { install.await(newCatalog) }
  }
  "doesn't update the installed catalog if an update isn't found" {
    val toUpdate = mockk<CatalogInstalled> {
      every { pkgName } returns "a.package"
    }
    val newCatalog = mockk<CatalogRemote> {
      every { pkgName } returns "b.package"
    }
    coEvery { repository.getRemoteCatalogs() } returns listOf(newCatalog)

    interactor.await(toUpdate)
    coVerify(exactly = 0) { install.await(newCatalog) }
  }
})
