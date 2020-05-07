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
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.core.log.Logger
import javax.inject.Inject

class AndroidAppState @Inject constructor(
  context: Application
) : AppState, LifecycleObserver {

  override var hasNetwork = false
    private set(value) {
      field = value
      networkChannel.offer(value)
    }

  override var isInForeground = false
    private set(value) {
      field = value
      foregroundChannel.offer(value)
    }

  private val foregroundChannel = ConflatedBroadcastChannel<Boolean>()
  override val foregroundRelay = foregroundChannel.asFlow()

  private val networkChannel = ConflatedBroadcastChannel<Boolean>()
  override val networkRelay = networkChannel.asFlow()

  init {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    GlobalScope.launch {
      context.isNetworkConnectedFlow.collect { hasNetwork = it }
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  private fun setForeground() {
    Logger.debug("Application now in foreground")
    isInForeground = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  private fun setBackground() {
    Logger.debug("Application went to background")
    isInForeground = false
  }

}
