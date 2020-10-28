/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.viewmodel

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tachiyomi.core.prefs.Preference
import kotlin.reflect.KProperty

class PreferenceStateDelegate<T>(
  private val preference: Preference<T>,
  scope: CoroutineScope
) {

  private val state = mutableStateOf(preference.get())

  init {
    preference.changes()
      .onEach { state.value = it }
      .launchIn(scope)
  }

  operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
    return state.value
  }

  operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: T) {
    preference.set(value)
  }

}
