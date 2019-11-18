/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.Okio
import okio.buffer
import okio.sink
import tachiyomi.core.http.Http
import tachiyomi.core.http.await
import tachiyomi.core.http.get
import tachiyomi.core.http.saveTo
import tachiyomi.core.os.PackageInstaller
import tachiyomi.core.util.getUriCompat
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import timber.log.Timber
import timber.log.error
import timber.log.warn
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
internal class CatalogInstaller @Inject constructor(
  private val context: Application,
  private val packageInstaller: PackageInstaller,
  private val http: Http
) {

  /**
   * Adds the given extension to the downloads queue and returns an observable containing its
   * step in the installation process.
   *
   * @param catalog The catalog to install.
   */
  fun downloadAndInstall(catalog: CatalogRemote) = flow {
    emit(InstallStep.Pending)

    val destFile = File(context.cacheDir, "${catalog.pkgName}.apk")
    try {
      val response = http.defaultClient.get(catalog.apkUrl).await()
      emit(InstallStep.Downloading)
      response.saveTo(destFile)

      emit(InstallStep.Installing)
      val installed = packageInstaller.install(destFile, catalog.pkgName)

      emit(if (installed) InstallStep.Installed else InstallStep.Error)
    } catch (e: Exception) {
      Timber.warn { "Error installing package: $e" }
      emit(InstallStep.Error)
    } finally {
      destFile.delete()
    }
  }

  /**
   * Starts an intent to uninstall the extension by the given package name.
   *
   * @param pkgName The package name of the extension to uninstall
   */
  suspend fun uninstallApk(pkgName: String): Boolean {
    return packageInstaller.uninstall(pkgName)
  }

}
