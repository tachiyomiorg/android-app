/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.app.initializers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tachiyomi.core.di.AppScope
import tachiyomi.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class CatalogRepositoryInitializer @Inject constructor() {

  init {
    // Create the catalog repository in an IO thread, because the expensive initializations are
    // the extensions which are already created in computation threads and we don't want to waste
    // one of them waiting for the extensions.
    GlobalScope.launch(Dispatchers.IO) {
      AppScope.getInstance<CatalogRepository>()
    }
  }

}
