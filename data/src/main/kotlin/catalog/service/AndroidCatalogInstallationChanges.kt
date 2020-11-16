/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.service

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import tachiyomi.domain.catalog.service.CatalogInstallationChange
import tachiyomi.domain.catalog.service.CatalogInstallationChanges
import javax.inject.Inject

internal class AndroidCatalogInstallationChanges @Inject constructor(
  context: Application
) : CatalogInstallationChanges {

  private val sharedFlow = MutableSharedFlow<CatalogInstallationChange>()

  override val flow: Flow<CatalogInstallationChange>
    get() = sharedFlow

  init {
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_PACKAGE_ADDED)
      addAction(Intent.ACTION_PACKAGE_REMOVED)
      addDataScheme("package")
    }
    context.registerReceiver(Receiver(), filter)
  }

  fun notifyAppInstall(pkgName: String) {
    sharedFlow.tryEmit(CatalogInstallationChange.LocalInstall(pkgName))
  }

  fun notifyAppUninstall(pkgName: String) {
    sharedFlow.tryEmit(CatalogInstallationChange.LocalUninstall(pkgName))
  }

  private inner class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent == null) return

      val pkgName = intent.data?.encodedSchemeSpecificPart ?: return

      when (intent.action) {
        Intent.ACTION_PACKAGE_ADDED -> {
          sharedFlow.tryEmit(CatalogInstallationChange.SystemInstall(pkgName))
        }
        Intent.ACTION_PACKAGE_REMOVED -> {
          sharedFlow.tryEmit(CatalogInstallationChange.SystemUninstall(pkgName))
        }
      }
    }
  }

}
