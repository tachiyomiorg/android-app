/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import android.content.Context
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import kotlinx.android.synthetic.main.catalogs_catalog_item.*
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.theme.IconTheme
import tachiyomi.ui.util.getColorFromAttr
import tachiyomi.ui.util.inflate
import tachiyomi.ui.util.setGone
import tachiyomi.ui.util.setVisible

class CatalogHolder(
  parent: ViewGroup,
  private val theme: Theme,
  private val glideRequests: GlideRequests,
  adapter: CatalogAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_catalog_item)) {

  init {
    itemView.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
    catalog_install_btn.setOnClickListener {
      adapter.handleInstallClick(adapterPosition)
    }
    catalog_settings_btn.setOnClickListener {
      adapter.handleSettingsClick(adapterPosition)
    }

    theme.icon.apply(catalog_install_btn)
    theme.icon.apply(catalog_settings_btn)
  }

  fun bind(catalog: CatalogInternal) {
    catalog_title.text = catalog.name
    catalog_description.text = catalog.description
    glideRequests.load(catalog).into(catalog_icon)
    catalog_settings_btn.setGone()
    catalog_install_btn.setGone()
  }

  fun bind(catalog: CatalogInstalled, installStep: InstallStep?) {
    catalog_title.text = getTitleWithVersion(catalog.name, catalog.versionCode)
    catalog_description.text = catalog.description
    glideRequests.load(catalog).into(catalog_icon)
    catalog_settings_btn.setVisible()
    bindInstallButton(catalog, installStep)
  }

  fun bind(catalog: CatalogRemote, installStep: InstallStep?) {
    catalog_title.text = getTitleWithVersion(catalog.name, catalog.versionCode)
    catalog_description.text = catalog.description
    glideRequests.load(catalog).into(catalog_icon)
    catalog_settings_btn.setGone()
    bindInstallButton(catalog, installStep)
  }

  fun bindInstallButton(catalog: CatalogInstalled, step: InstallStep?) {
    when {
      step != null && !step.isFinished() -> showInstalling()
      catalog.hasUpdate -> showInstall()
      else -> hideInstall()
    }
  }

  fun bindInstallButton(catalog: CatalogRemote, step: InstallStep?) {
    if (step != null && !step.isFinished()) {
      showInstalling()
    } else {
      showInstall()
    }
  }

  override fun recycle() {
    glideRequests.clear(catalog_icon)
  }

  private fun showInstalling() {
    catalog_install_flip.setVisible()
    catalog_install_flip.run { if (displayedChild != 1) displayedChild = 1 }
  }

  private fun showInstall() {
    catalog_install_flip.setVisible()
    catalog_install_flip.run { if (displayedChild != 0) displayedChild = 0 }
  }

  private fun hideInstall() {
    catalog_install_flip.setGone()
  }

  private fun getTitleWithVersion(title: String, version: Int): SpannedString {
    return buildSpannedString {
      append("$title ")

      inSpans(AbsoluteSizeSpan(12, true), ForegroundColorSpan(theme.textColorSecondary)) {
        append("v$version")
      }
    }
  }

  class Theme(context: Context) {
    val icon = IconTheme(context)
    val textColorSecondary = context.getColorFromAttr(android.R.attr.textColorSecondary)
  }

}
