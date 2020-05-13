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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.service.CatalogInstaller

class UninstallCatalogTest : StringSpec({

  val installer = mockk<CatalogInstaller>(relaxed = true)
  val interactor = UninstallCatalog(installer)
  afterTest { clearAllMocks() }

  "calls the implementation" {
    val catalog = mockk<CatalogInstalled> {
      every { pkgName } returns "a.package"
    }
    interactor.await(catalog)

    coVerify { installer.uninstall("a.package") }
  }
})
