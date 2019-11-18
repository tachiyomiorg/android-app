/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.data.catalog.installer.CatalogLoader.Result
import tachiyomi.domain.catalog.model.CatalogInstalled

/**
 * Broadcast receiver that listens for the system's packages installed, updated or removed, and only
 * notifies the given [listener] when the package is an extension.
 *
 * @param listener The listener that should be notified of extension installation events.
 */
internal class CatalogInstallReceiver(
  private val listener: Listener,
  private val loader: CatalogLoader
) : BroadcastReceiver() {

  /**
   * Registers this broadcast receiver
   */
  fun register(context: Context) {
    context.registerReceiver(this, filter)
  }

  /**
   * Returns the intent filter this receiver should subscribe to.
   */
  private val filter
    get() = IntentFilter().apply {
      addAction(Intent.ACTION_PACKAGE_ADDED)
      addAction(Intent.ACTION_PACKAGE_REPLACED)
      addAction(Intent.ACTION_PACKAGE_REMOVED)
      addDataScheme("package")
    }

  /**
   * Called when one of the events of the [filter] is received. When the package is an extension,
   * it's loaded in background and it notifies the [listener] when finished.
   */
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return

    when (intent.action) {
      Intent.ACTION_PACKAGE_ADDED -> {
        GlobalScope.launch(Dispatchers.IO) {
          if (isReplacing(intent)) return@launch

          val result = getExtensionFromIntent(intent)
          when (result) {
            is Result.Success -> listener.onCatalogInstalled(result.catalog)
          }
        }
      }
      Intent.ACTION_PACKAGE_REPLACED -> {
        GlobalScope.launch(Dispatchers.IO) {
          val result = getExtensionFromIntent(intent)
          when (result) {
            is Result.Success -> listener.onCatalogUpdated(result.catalog)
          }
        }
      }
      Intent.ACTION_PACKAGE_REMOVED -> {
        GlobalScope.launch(Dispatchers.IO) {
          if (isReplacing(intent)) return@launch

          val pkgName = getPackageNameFromIntent(intent)
          if (pkgName != null) {
            listener.onPackageUninstalled(pkgName)
          }
        }
      }
    }
  }

  /**
   * Returns true if this package is performing an update.
   *
   * @param intent The intent that triggered the event.
   */
  private fun isReplacing(intent: Intent): Boolean {
    return intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
  }

  /**
   * Returns the extension triggered by the given intent.
   *
   * @param intent The intent containing the package name of the extension.
   */
  private suspend fun getExtensionFromIntent(intent: Intent?): Result {
    val pkgName = getPackageNameFromIntent(intent)
      ?: return Result.Error("Package name not found")

    return withContext(Dispatchers.Default) {
      loader.loadExtensionFromPkgName(pkgName)
    }
  }

  /**
   * Returns the package name of the installed, updated or removed application.
   */
  private fun getPackageNameFromIntent(intent: Intent?): String? {
    return intent?.data?.encodedSchemeSpecificPart ?: return null
  }

  /**
   * Listener that receives extension installation events.
   */
  interface Listener {

    fun onCatalogInstalled(catalog: CatalogInstalled)
    fun onCatalogUpdated(catalog: CatalogInstalled)
    fun onPackageUninstalled(pkgName: String)
  }

}
