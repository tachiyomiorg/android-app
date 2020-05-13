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
import io.mockk.mockk
import io.mockk.verify
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogInstaller

class InstallCatalogTest : StringSpec({

  val installer = mockk<CatalogInstaller>(relaxed = true)
  val interactor = InstallCatalog(installer)
  afterTest { clearAllMocks() }

  "calls the implementation" {
    val catalog = mockk<CatalogRemote>()
    interactor.await(catalog)

    verify { installer.install(catalog) }
  }
})
