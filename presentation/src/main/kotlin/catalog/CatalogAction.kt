/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep

sealed class Action {

  object Init : Action()

  data class ItemsUpdate(val items: List<Any>) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(items = items)
  }

  data class SetLanguageChoice(val choice: LanguageChoice) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(languageChoice = choice)
  }

  data class InstallingCatalogsUpdate(val installingCatalogs: Map<String, InstallStep>) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(installingCatalogs = installingCatalogs)
  }

  data class InstallStepUpdate(val pkgName: String, val step: InstallStep) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(installingCatalogs = if (step == InstallStep.Completed) {
        state.installingCatalogs - pkgName
      } else {
        state.installingCatalogs + (pkgName to step)
      })
  }

  data class RefreshingCatalogs(val isRefreshing: Boolean) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(isRefreshing = isRefreshing)
  }

  data class InstallCatalog(val catalog: CatalogRemote) : Action()

  data class UpdateCatalog(val catalog: CatalogInstalled) : Action()

  data class RefreshCatalogs(val force: Boolean) : Action()

  open fun reduce(state: ViewState) = state

}
