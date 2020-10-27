/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.presenter

import androidx.annotation.CallSuper
import com.freeletics.coredux.LogSink
import com.freeletics.coredux.StateReceiver
import com.freeletics.coredux.Store
import com.freeletics.coredux.log.common.LoggerLogSink
import com.freeletics.coredux.subscribeToChangedStateUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import tachiyomi.core.log.Log
import tachiyomi.ui.BuildConfig

abstract class BasePresenter {

  protected val job = SupervisorJob()

  protected val scope = CoroutineScope(job + Dispatchers.Main)

  @CallSuper
  open fun destroy() {
    job.cancel()
  }

  fun <S : Any, A : Any> Store<S, A>.asFlow() = callbackFlow {
    var previousState: S? = null

    val receiver: StateReceiver<S> = { newState ->
      if (newState !== previousState) {
        previousState = newState
        offer(newState)
      }
    }
    subscribe(receiver)
    awaitClose { unsubscribe(receiver) }
  }

  fun <S : Any, A : Any> Store<S, A>.subscribeToChangedStateUpdatesInMain(
    stateReceiver: StateReceiver<S>
  ) {
    subscribeToChangedStateUpdates {
      scope.launch(Dispatchers.Main) { stateReceiver(it) }
    }
  }

  protected fun getLogSinks(): List<LogSink> {
    return if (BuildConfig.DEBUG) {
      listOf(AndroidLogSink())
    } else {
      emptyList()
    }
  }

  private class AndroidLogSink(scope: CoroutineScope = GlobalScope) : LoggerLogSink(scope) {
    override fun debug(tag: String, message: String, throwable: Throwable?) {
      if (throwable == null) {
        Log.debug(message)
      } else {
        Log.debug(throwable, message)
      }
    }

    override fun info(tag: String, message: String, throwable: Throwable?) {
      if (throwable == null) {
        Log.info(message)
      } else {
        Log.info(throwable, message)
      }
    }

    override fun warning(tag: String, message: String, throwable: Throwable?) {
      if (throwable == null) {
        Log.warn(message)
      } else {
        Log.warn(throwable, message)
      }
    }
  }

}
