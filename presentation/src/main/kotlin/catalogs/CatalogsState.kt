/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep

data class ViewState(
  val localCatalogs: List<CatalogLocal> = emptyList(),
  val updatableCatalogs: List<CatalogLocal> = emptyList(),
  val remoteCatalogs: List<CatalogRemote> = emptyList(),
  val languageChoice: LanguageChoice = LanguageChoice.All,
  val languageChoices: List<LanguageChoice> = emptyList(),
  val installingCatalogs: Map<String, InstallStep> = emptyMap(),
  val isRefreshing: Boolean = false
)

sealed class Action {

  object Init : Action()

  data class ItemsUpdate(
    val localCatalogs: List<CatalogLocal>,
    val updatableCatalogs: List<CatalogLocal>,
    val remoteCatalogs: List<CatalogRemote>,
    val languageChoices: List<LanguageChoice>
  ) : Action() {
    override fun reduce(state: ViewState): ViewState {
      val safeChoice = if (state.languageChoice in languageChoices)
        state.languageChoice
      else
        LanguageChoice.All

      return state.copy(
        localCatalogs = localCatalogs,
        updatableCatalogs = updatableCatalogs,
        remoteCatalogs = remoteCatalogs,
        languageChoices = languageChoices,
        languageChoice = safeChoice
      )
    }
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
