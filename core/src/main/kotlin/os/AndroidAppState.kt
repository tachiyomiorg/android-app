/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.os

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.erikhuizinga.flomo.isNetworkConnectedFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.core.log.Log
import javax.inject.Inject

class AndroidAppState @Inject constructor(
  context: Application
) : AppState, LifecycleObserver {

  private val _networkFlow = MutableStateFlow(false)
  override val networkFlow get() = _networkFlow

  private val _foregroundFlow = MutableStateFlow(false)
  override val foregroundFlow get() = _foregroundFlow

  init {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    GlobalScope.launch {
      context.isNetworkConnectedFlow.collect { _networkFlow.value = it }
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  private fun setForeground() {
    Log.debug("Application now in foreground")
    foregroundFlow.value = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  private fun setBackground() {
    Log.debug("Application went to background")
    foregroundFlow.value = false
  }

}
