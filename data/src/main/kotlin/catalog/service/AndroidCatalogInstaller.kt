/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.service

import android.app.Application
import kotlinx.coroutines.flow.flow
import tachiyomi.core.http.Http
import tachiyomi.core.http.awaitSuccess
import tachiyomi.core.http.get
import tachiyomi.core.http.saveTo
import tachiyomi.core.os.PackageInstaller
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.domain.catalog.service.CatalogInstaller
import timber.log.Timber
import timber.log.warn
import java.io.File
import javax.inject.Inject

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
internal class AndroidCatalogInstaller @Inject constructor(
  private val context: Application,
  private val packageInstaller: PackageInstaller,
  private val http: Http
) : CatalogInstaller {

  /**
   * Adds the given extension to the downloads queue and returns an observable containing its
   * step in the installation process.
   *
   * @param catalog The catalog to install.
   */
  override fun install(catalog: CatalogRemote) = flow {
    emit(InstallStep.Pending)

    val destFile = File(context.cacheDir, "${catalog.pkgName}.apk")
    try {
      val response = http.defaultClient.get(catalog.apkUrl).awaitSuccess()
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
  override suspend fun uninstall(pkgName: String): Boolean {
    return packageInstaller.uninstall(pkgName)
  }

}
