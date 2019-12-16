/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.sync

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import tachiyomi.domain.sync.interactor.Login
import tachiyomi.domain.sync.service.SyncPreferences
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.FlowSwitchSideEffect
import javax.inject.Inject

class SyncPresenter @Inject constructor(
  private val login: Login,
  private val syncPreferences: SyncPreferences
) : BasePresenter() {

  private val initialState = getInitialViewState()

  val state = ConflatedBroadcastChannel(initialState)

  private val store = scope.createStore(
    name = "Sync presenter",
    initialState = initialState,
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.offer(it) }
    listenLoggedin()
  }

  private fun getInitialViewState(): ViewState {
    val isLogged = syncPreferences.token().isSet()
    return ViewState(
      isLogged = isLogged,
      isLoading = false
    )
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += FlowSwitchSideEffect("Login") f@{ stateFn, action ->
      if (action !is Action.Login || stateFn().isLoading) return@f null

      suspend {
        flow {
          emit(Action.Loading(true))
          val result = login.await(action.address, action.username, action.password)
          emit(Action.Loading(false))
        }
      }
    }

    return sideEffects
  }

  private fun listenLoggedin() {
    scope.launch {
      syncPreferences.token().changes().collect {
        store.dispatch(Action.Logged(it.isNotEmpty()))
      }
    }
  }

  fun login(address: String, username: String, password: String) {
    store.dispatch(Action.Login(address, username, password))
  }

}
