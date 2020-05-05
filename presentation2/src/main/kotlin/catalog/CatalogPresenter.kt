/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import androidx.compose.Composable
import androidx.compose.State
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tachiyomi.domain.catalog.interactor.GetCatalogs
import tachiyomi.domain.catalog.interactor.InstallCatalog
import tachiyomi.domain.catalog.interactor.FetchRemoteCatalogs
import tachiyomi.domain.catalog.interactor.UpdateCatalog
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.collectAsState
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.FlowSideEffect
import tachiyomi.ui.presenter.FlowSwitchSideEffect
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getCatalogs: GetCatalogs,
  private val installCatalog: InstallCatalog,
  private val updateCatalog: UpdateCatalog,
  private val fetchRemoteCatalogs: FetchRemoteCatalogs
) : BasePresenter() {

  private val initialState = getInitialViewState()

  private val store = scope.createStore(
    name = "Catalog presenter",
    initialState = initialState,
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.dispatch(Action.Init)
  }

  @Composable
  fun state(): State<ViewState> {
    return store.asFlow().collectAsState(initialState)
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += FlowSwitchSideEffect("Subscribe to catalogs") f@{ stateFn, action ->
      if (action !is Action.Init && action !is Action.SetLanguageChoice) return@f null
      val choice = stateFn().languageChoice

      suspend {
        getCatalogs.subscribe(excludeRemoteInstalled = true).map { (local, remote) ->
          val (updatable, upToDate) = local.partition { it is CatalogInstalled && it.hasUpdate }
          Action.ItemsUpdate(
            localCatalogs = upToDate,
            updatableCatalogs = updatable,
            remoteCatalogs = getRemoteCatalogsForLanguageChoice(remote, choice),
            languageChoices = getLanguageChoices(remote, local)
          )
        }
      }
    }

    sideEffects += FlowSideEffect("Install catalog") { _, action ->
      when (action) {
        is Action.InstallCatalog -> suspend {
          val catalog = action.catalog
          installCatalog.await(catalog).map { Action.InstallStepUpdate(catalog.pkgName, it) }
        }
        is Action.UpdateCatalog -> suspend {
          val catalog = action.catalog
          updateCatalog.await(catalog).map { Action.InstallStepUpdate(catalog.pkgName, it) }
        }
        else -> null
      }
    }

    sideEffects += FlowSwitchSideEffect("Refresh catalogs") f@{ _, action ->
      if (action != Action.Init && action !is Action.RefreshCatalogs) return@f null

      suspend {
        val force = if (action is Action.RefreshCatalogs) action.force else false

        // TODO there should be a better way to do this
        val deferred = scope.async { fetchRemoteCatalogs.await(force) }
        flow {
          emit(Action.RefreshingCatalogs(true))
          runCatching { deferred.await() }
          emit(Action.RefreshingCatalogs(false))
        }
          // Debounce for a frame. Sometimes this operation returns immediately, so with this
          // we avoid showing the progress bar if not really needed
          .debounce(16)
      }
    }

    return sideEffects
  }

  private fun getLanguageChoices(
    remote: List<CatalogRemote>,
    local: List<CatalogLocal>
  ): List<LanguageChoice> {
    val knownLanguages = mutableListOf<LanguageChoice.One>()
    val unknownLanguages = mutableListOf<Language>()

    val languageComparators = UserLanguagesComparator()
      .then(InstalledLanguagesComparator(local))
      .thenBy { it.code }

    remote.asSequence()
      .map { Language(it.lang) }
      .distinct()
      .sortedWith(languageComparators)
      .forEach { code ->
        if (code.toEmoji() != null) {
          knownLanguages.add(LanguageChoice.One(code))
        } else {
          unknownLanguages.add(code)
        }
      }

    val languages = mutableListOf<LanguageChoice>()
    languages.add(LanguageChoice.All)
    languages.addAll(knownLanguages)
    if (unknownLanguages.isNotEmpty()) {
      languages.add(LanguageChoice.Others(unknownLanguages))
    }

    return languages
  }

  private fun getRemoteCatalogsForLanguageChoice(
    catalogs: List<CatalogRemote>,
    choice: LanguageChoice
  ): List<CatalogRemote> {
    return when (choice) {
      is LanguageChoice.All -> catalogs
      is LanguageChoice.One -> catalogs.filter { choice.language.code == it.lang }
      is LanguageChoice.Others -> {
        val codes = choice.languages.map { it.code }
        catalogs.filter { it.lang in codes }
      }
    }
  }

  fun setLanguageChoice(languageChoice: LanguageChoice) {
    store.dispatch(Action.SetLanguageChoice(languageChoice))
  }

  fun installCatalog(catalog: Catalog) {
    when (catalog) {
      is CatalogInstalled -> store.dispatch(Action.UpdateCatalog(catalog))
      is CatalogRemote -> store.dispatch(Action.InstallCatalog(catalog))
    }
  }

  fun refreshCatalogs() {
    store.dispatch(Action.RefreshCatalogs(true))
  }

}
