/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import tachiyomi.domain.catalog.model.Catalog

data class ViewState(
  val catalog: Catalog? = null,
  val isRefreshing: Boolean = false
)

sealed class Action {

  object Init : Action()

  data class SetCatalog(val catalog: Catalog) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(catalog = catalog)
  }

  open fun reduce(state: ViewState) = state

}
