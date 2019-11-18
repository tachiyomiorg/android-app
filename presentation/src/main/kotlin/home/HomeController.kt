/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.Router
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.android.synthetic.main.home_controller.*
import tachiyomi.ui.R
import tachiyomi.ui.catalog.CatalogController
import tachiyomi.ui.controller.BaseController
import tachiyomi.ui.controller.withFadeTransition
import tachiyomi.ui.controller.withoutTransition
import tachiyomi.ui.library.LibraryController
import tachiyomi.ui.settings.SettingsController

class HomeController : BaseController() {

  private val childRouterChangeListener = HomeControllerChangeListener()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.home_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    val router = getChildRouter(view.findViewById(R.id.home_controller_container))
    router.addChangeListener(childRouterChangeListener)

    home_bottomnav?.setOnNavigationItemSelectedListener { onSetSelectedItem(it.itemId, router) }

    if (!router.hasRootController()) {
      performSetSelectedItem(R.id.nav_drawer_library)
    } else {
      syncTopController(null, router.backstack.last().controller(), true)
    }
  }

  override fun onDestroyView(view: View) {
    home_bottomnav?.setOnNavigationItemSelectedListener(null)
    childRouters.forEach { it.removeChangeListener(childRouterChangeListener) }
    super.onDestroyView(view)
  }

  private fun performSetSelectedItem(itemId: Int) {
    home_bottomnav?.selectedItemId = itemId
  }

  private fun onSetSelectedItem(id: Int, router: Router): Boolean {
    val currentRoot = router.backstack.firstOrNull()
    if (currentRoot?.tag()?.toIntOrNull() != id) {
      when (id) {
        R.id.nav_drawer_library -> setRoot(router, LibraryController(), id)
        R.id.nav_drawer_catalogues -> setRoot(router, CatalogController(), id)
        R.id.nav_drawer_settings -> setRoot(router, SettingsController(), id)
      }
    }
    return true
  }

  private fun setRoot(
    router: Router,
    controller: Controller,
    id: Int
  ) {
    val transaction = if (!router.hasRootController()) {
      controller.withoutTransition()
    } else {
      controller.withFadeTransition()
    }
    router.setRoot(transaction.tag("$id"))
  }

  private fun syncTopController(from: Controller?, to: Controller?, isPush: Boolean) {
    if (from is HomeChildController.FAB) {
      view?.findViewById<ViewGroup>(R.id.home_fab_container)?.removeAllViews()
    }
    if (to is HomeChildController.FAB) {
      view?.findViewById<ViewGroup>(R.id.home_fab_container)?.let { it.addView(to.createFAB(it)) }
    }
    if (from is HomeChildController.Drawer) {
      view?.findViewById<DrawerLayout>(R.id.home_drawer)?.let { drawer ->
        while (drawer.childCount > 1) {
          drawer.removeViewAt(1)
        }
      }
    }
    if (to is HomeChildController.Drawer) {
      view?.findViewById<ViewGroup>(R.id.home_drawer)?.let { drawer ->
        drawer.addView(to.createNavView(drawer))
      }
    }

    // If going back, restore bottom nav visibility, in case the new view can't scroll
    if (!isPush) {
      val nav = home_bottomnav
      if (nav != null) {
        val params = nav.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as? HideBottomViewOnScrollBehavior
        behavior?.onNestedScroll(home_coordinator_bottomnav, nav, nav,
          0, -1, 0, 0, ViewCompat.TYPE_TOUCH)
      }
    }
  }

  inner class HomeControllerChangeListener : ControllerChangeHandler.ControllerChangeListener {

    override fun onChangeStarted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler
    ) {
      syncTopController(from, to, isPush)
    }

    override fun onChangeCompleted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler
    ) {

    }

  }

}
