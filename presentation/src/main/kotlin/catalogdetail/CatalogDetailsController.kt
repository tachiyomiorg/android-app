/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.catalogdetail_controller.*
import kotlinx.coroutines.flow.asFlow
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.glide.GlideApp
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.util.clicks
import tachiyomi.ui.util.inflate
import tachiyomi.ui.util.scanWithPrevious
import java.util.Locale

class CatalogDetailsController(
  bundle: Bundle
) : MvpController<CatalogDetailsPresenter>(bundle),
  HomeChildController {

  constructor(pkgName: String) : this(Bundle().apply {
    putString(PKGNAME_KEY, pkgName)
  })

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  /**
   * Returns the presenter class used by this controller.
   */
  override fun getPresenterClass() = CatalogDetailsPresenter::class.java

  /**
   * Returns the module of this controller that provides the dependencies of the presenter.
   */
  override fun getModule() = CatalogDetailsModule(this)

  /**
   * Returns the package name of the catalog stored in the [Bundle] of this controller.
   */
  fun getCatalogPkgName() = args.getString(PKGNAME_KEY, "")

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return container.inflate(R.layout.catalogdetail_controller)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    // Setup back navigation
    setupToolbarNavWithHomeController(catalogdetail_toolbar)

    catalogdetail_uninstall_button.clicks()
      .collectWithView { uninstallCatalog() }

    presenter.state.asFlow()
      .scanWithPrevious()
      .collectWithView { (state, prevState) -> render(state, prevState) }
  }

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.isUninstalled) {
      router.popController(this)
    }
    if (state.catalog != null && state.catalog != prevState?.catalog) {
      renderCatalog(state.catalog)
    }
  }

  private fun renderCatalog(catalog: CatalogInstalled) {
    catalogdetail_title.text = catalog.name
    catalogdetail_version.text = catalog.versionName
    catalogdetail_lang.text = Locale(catalog.source.lang).displayLanguage
    catalogdetail_pkg.text = catalog.pkgName

    GlideApp.with(activity!!)
      .load(catalog)
      .into(catalogdetail_icon)
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  private fun uninstallCatalog() {
    presenter.uninstallCatalog()
  }

  private companion object {
    const val PKGNAME_KEY = "pkgname"
  }

}
