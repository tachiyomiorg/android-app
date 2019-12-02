/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import tachiyomi.domain.catalog.repository.CatalogInstallationReceiver
import javax.inject.Inject

internal class AndroidCatalogInstallationReceiver @Inject constructor(
  private val context: Application
) : BroadcastReceiver(),
  CatalogInstallationReceiver {

  private var listener: CatalogInstallationReceiver.Listener? = null

  override fun register(listener: CatalogInstallationReceiver.Listener) {
    this.listener = listener

    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_PACKAGE_ADDED)
      addAction(Intent.ACTION_PACKAGE_REMOVED)
      addDataScheme("package")
    }
    context.registerReceiver(this, filter)
  }

  override fun unregister() {
    this.listener = null
    context.unregisterReceiver(this)
  }

  override fun onReceive(ctx: Context?, intent: Intent?) {
    if (intent == null) return

    val pkgName = intent.data?.encodedSchemeSpecificPart ?: return

    when (intent.action) {
      Intent.ACTION_PACKAGE_ADDED -> listener?.onInstalled(pkgName)
      Intent.ACTION_PACKAGE_REMOVED -> listener?.onUninstalled(pkgName)
    }
  }

}
