/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.core.prefs.Preference
import tachiyomi.ui.core.prefs.PreferenceMutableState

abstract class BaseViewModel {

  protected val scope = MainScope()

  fun destroy() {
    scope.cancel()
    onDestroy()
  }

  open fun onDestroy() {
  }

  fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)

  fun <T> Flow<T>.asState(initialValue: T): State<T> {
    val state = mutableStateOf(initialValue)
    scope.launch {
      collect { state.value = it }
    }
    return state
  }

  fun <T> StateFlow<T>.asState(): State<T> {
    val state = mutableStateOf(value)
    scope.launch {
      collect { state.value = it }
    }
    return state
  }

}
