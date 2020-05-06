/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.presenter

import com.freeletics.coredux.LogEvent
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.SideEffectLogger
import com.freeletics.coredux.StateAccessor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EmptySideEffect<S : Any, A : Any>(
  override val name: String,
  private val sideEffect: (
    state: StateAccessor<S>,
    action: A
  ) -> (suspend () -> Any?)?
) : SideEffect<S, A> {

  override fun CoroutineScope.start(
    input: ReceiveChannel<A>,
    stateAccessor: StateAccessor<S>,
    output: SendChannel<A>,
    logger: SideEffectLogger
  ) = launch(context = CoroutineName(name)) {
    for (action in input) {
      val handler = sideEffect(stateAccessor, action) ?: continue
      logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }

      launch {
        handler()
      }
    }
  }
}

class SingleSideEffect<S : Any, A : Any>(
  override val name: String,
  private val sideEffect: (
    state: StateAccessor<S>,
    action: A
  ) -> (suspend () -> A?)?
) : SideEffect<S, A> {

  override fun CoroutineScope.start(
    input: ReceiveChannel<A>,
    stateAccessor: StateAccessor<S>,
    output: SendChannel<A>,
    logger: SideEffectLogger
  ) = launch(context = CoroutineName(name)) {
    for (action in input) {
      val producer = sideEffect(stateAccessor, action) ?: continue
      logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }

      launch {
        producer()?.let {
          logger.logSideEffectEvent { LogEvent.SideEffectEvent.DispatchingToReducer(name, it) }
          output.send(it)
        }
      }
    }
  }
}

class SingleSwitchSideEffect<S : Any, A : Any>(
  override val name: String,
  private val sideEffect: (
    state: StateAccessor<S>,
    action: A
  ) -> (suspend () -> A?)?
) : SideEffect<S, A> {

  override fun CoroutineScope.start(
    input: ReceiveChannel<A>,
    stateAccessor: StateAccessor<S>,
    output: SendChannel<A>,
    logger: SideEffectLogger
  ) = launch(context = CoroutineName(name)) {
    var job: Job? = null
    for (action in input) {
      val producer = sideEffect(stateAccessor, action) ?: continue
      logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }

      job?.run {
        if (isActive) {
          logger.logSideEffectEvent {
            LogEvent.SideEffectEvent.Custom(name, "Cancelling previous job on new $action action")
          }
        }
        cancel()
      }

      job = launch {
        producer()?.let {
          logger.logSideEffectEvent { LogEvent.SideEffectEvent.DispatchingToReducer(name, it) }
          output.send(it)
        }
      }
    }
  }
}

class FlowSideEffect<S : Any, A : Any>(
  override val name: String,
  private val sideEffect: (
    state: StateAccessor<S>,
    action: A
  ) -> (suspend () -> Flow<A>)?
) : SideEffect<S, A> {

  override fun CoroutineScope.start(
    input: ReceiveChannel<A>,
    stateAccessor: StateAccessor<S>,
    output: SendChannel<A>,
    logger: SideEffectLogger
  ) = launch(context = CoroutineName(name)) {
    for (action in input) {
      val producer = sideEffect(stateAccessor, action) ?: continue
      logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }

      launch {
        producer().collect {
          logger.logSideEffectEvent { LogEvent.SideEffectEvent.DispatchingToReducer(name, it) }
          output.send(it)
        }
      }
    }
  }
}

class FlowSwitchSideEffect<S : Any, A : Any>(
  override val name: String,
  private val sideEffect: (
    state: StateAccessor<S>,
    action: A
  ) -> (suspend () -> Flow<A>)?
) : SideEffect<S, A> {

  override fun CoroutineScope.start(
    input: ReceiveChannel<A>,
    stateAccessor: StateAccessor<S>,
    output: SendChannel<A>,
    logger: SideEffectLogger
  ) = launch(context = CoroutineName(name)) {
    var job: Job? = null
    for (action in input) {
      val producer = sideEffect(stateAccessor, action) ?: continue
      logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }

      job?.run {
        if (isActive) {
          logger.logSideEffectEvent {
            LogEvent.SideEffectEvent.Custom(name, "Cancelling previous job on new $action action")
          }
        }
        cancel()
      }

      job = launch {
        producer().collect {
          logger.logSideEffectEvent { LogEvent.SideEffectEvent.DispatchingToReducer(name, it) }
          output.send(it)
        }
      }
    }
  }
}
