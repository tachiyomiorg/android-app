/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogViewModel @Inject constructor(
  private val params: Params,
  private val getLocalCatalog: GetLocalCatalog,
) : BaseViewModel() {

  var catalog by mutableStateOf<CatalogLocal?>(null)
    private set
  var isRefreshing by mutableStateOf(false)
    private set

  init {
    catalog = getLocalCatalog.get(params.sourceId)
  }

  fun toggle() {
    isRefreshing = !isRefreshing
  }

  data class Params(val sourceId: Long)
}
