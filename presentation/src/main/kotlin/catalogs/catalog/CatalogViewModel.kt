/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogViewModel @Inject constructor(
  private val getLocalCatalog: GetLocalCatalog
) : BaseViewModel() {

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

    return sideEffects
  }

  fun setCatalog(sourceId: Long) {
    getLocalCatalog.get(sourceId)?.let {
      store.dispatch(Action.SetCatalog(it))
    }
  }
}
