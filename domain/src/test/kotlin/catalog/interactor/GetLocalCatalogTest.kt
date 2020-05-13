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
import io.mockk.verifySequence
import tachiyomi.domain.catalog.service.CatalogStore

class GetLocalCatalogTest : StringSpec({

  val store = mockk<CatalogStore>(relaxed = true)
  val interactor = GetLocalCatalog(store)
  afterTest { clearAllMocks() }

  "calls the store" {
    interactor.get(1)
    verify { store.get(1) }
  }
  "called in order" {
    interactor.get(1)
    interactor.get(2)
    interactor.get(4)

    verifySequence {
      store.get(1)
      store.get(2)
      store.get(4)
    }
  }

})
