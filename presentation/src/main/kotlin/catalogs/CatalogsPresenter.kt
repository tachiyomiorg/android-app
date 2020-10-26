/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.interactor.GetCatalogsByType
import tachiyomi.domain.catalog.interactor.InstallCatalog
import tachiyomi.domain.catalog.interactor.SyncRemoteCatalogs
import tachiyomi.domain.catalog.interactor.UninstallCatalog
import tachiyomi.domain.catalog.interactor.UpdateCatalog
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.core.presenter.BasePresenter
import tachiyomi.ui.core.presenter.FlowSideEffect
import tachiyomi.ui.core.presenter.FlowSwitchSideEffect
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getCatalogsByType: GetCatalogsByType,
  private val installCatalog: InstallCatalog,
  private val uninstallCatalog: UninstallCatalog,
  private val updateCatalog: UpdateCatalog,
  private val syncRemoteCatalogs: SyncRemoteCatalogs
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
        getCatalogsByType.subscribe(excludeRemoteInstalled = true).map { (upToDate, updatable, remote) ->
          Action.ItemsUpdate(
            localCatalogs = upToDate,
            updatableCatalogs = updatable,
            remoteCatalogs = getRemoteCatalogsForLanguageChoice(remote, choice),
            languageChoices = getLanguageChoices(remote, upToDate + updatable)
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
        val deferred = scope.async { syncRemoteCatalogs.await(force) }
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

  fun uninstallCatalog(catalog: CatalogInstalled) {
    GlobalScope.launch {
      uninstallCatalog.await(catalog)
    }
  }

  fun refreshCatalogs() {
    store.dispatch(Action.RefreshCatalogs(true))
  }

}
