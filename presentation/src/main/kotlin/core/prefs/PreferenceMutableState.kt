/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.prefs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tachiyomi.core.prefs.Preference

class PreferenceMutableState<T>(
  private val preference: Preference<T>,
  scope: CoroutineScope
) : MutableState<T> {

  private val state = mutableStateOf(preference.get())

  init {
    preference.changes()
      .onEach { state.value = it }
      .launchIn(scope)
  }

  override var value: T
    get() = state.value
    set(value) {
      preference.set(value)
    }

  override fun component1(): T {
    return state.value
  }

  override fun component2(): (T) -> Unit {
    return { preference.set(it) }
  }

}
