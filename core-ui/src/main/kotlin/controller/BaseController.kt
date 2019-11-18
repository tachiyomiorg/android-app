/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.controller

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.Router
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tachiyomi.core.ui.BuildConfig
import timber.log.Timber
import timber.log.debug

abstract class BaseController(
  bundle: Bundle? = null
) : RestoreViewOnCreateController(bundle), LayoutContainer, LifecycleOwner {

  @Suppress("LeakingThis")
  private val lifecycleRegistry = LifecycleRegistry(this)

  override val containerView: View?
    get() = view

  private var viewLifecycleRegistry: LifecycleRegistry? = null
  private var viewLifecycleOwner: LifecycleOwner? = null

  private var viewContext = Dispatchers.Main
  private var viewScope = CoroutineScope(viewContext + SupervisorJob())

  init {
    addLifecycleListener(object : LifecycleListener() {
      override fun postContextAvailable(controller: Controller, context: Context) {
        if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
      }

      override fun preCreateView(controller: Controller) {
        viewLifecycleOwner = LifecycleOwner {
          if (viewLifecycleRegistry == null) {
            viewLifecycleRegistry = LifecycleRegistry(viewLifecycleOwner!!)
          }
          viewLifecycleRegistry!!
        }
        viewLifecycleRegistry = null
      }

      override fun postCreateView(controller: Controller, view: View) {
        // Initialize the LifecycleRegistry if needed
        viewLifecycleOwner?.lifecycle

        onViewCreated(view)
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
      }

      override fun postAttach(controller: Controller, view: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }

      override fun preDetach(controller: Controller, view: View) {
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
      }

      override fun preDestroyView(controller: Controller, view: View) {
        viewLifecycleRegistry?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      }

      override fun preDestroy(controller: Controller) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.preDestroy(controller)
      }

      override fun onChangeStart(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        if (BuildConfig.DEBUG && changeType.isEnter) {
          printBackstack()
        }
      }
    })
  }

  @CallSuper
  open fun onViewCreated(view: View) {
    if (!viewScope.isActive) {
      viewScope = CoroutineScope(viewContext + SupervisorJob())
    }
  }

  @CallSuper
  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    viewScope.cancel()
    clearFindViewByIdCache()
  }

  override fun getLifecycle(): Lifecycle {
    return lifecycleRegistry
  }

  val viewLifecycle: Lifecycle
    get() = viewLifecycleOwner?.lifecycle ?: throw IllegalStateException(
      "Can't access the Controller View's Lifecycle when getView() is null i.e., " +
        "before onCreateView() or after onDestroyView()")

  fun <T> Flow<T>.collectWithView(onValue: (T) -> Unit): Job {
    return viewScope.launch { collect { onValue(it) } }
  }

  fun findRootRouter(): Router {
    var currentRouter = router
    var parent = parentController
    while (parent != null) {
      currentRouter = parent.router
      parent = parent.parentController
    }
    return currentRouter
  }

  fun printBackstack() {
    val rootRouter = findRootRouter()
    val builder = StringBuilder()
    builder.appendln("Current backstack:")
    printBackstack(builder, rootRouter, 0)
    Timber.debug { builder.toString() }
  }

  private fun printBackstack(builder: StringBuilder, router: Router, level: Int) {
    router.backstack.forEach { transaction ->
      val controller = transaction.controller()
      for (i in 0 until level) {
        builder.append("\t")
      }
      builder.appendln(controller.toString())
      for (childRouter in controller.childRouters) {
        printBackstack(builder, childRouter, level + 1)
      }
    }
  }

}
