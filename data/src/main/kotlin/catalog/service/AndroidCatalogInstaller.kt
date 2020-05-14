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
import tachiyomi.core.log.Logger
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.domain.catalog.service.CatalogInstaller
import java.io.File
import javax.inject.Inject

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
internal class AndroidCatalogInstaller @Inject constructor(
  private val context: Application,
  private val http: Http,
  private val installationChanges: AndroidCatalogInstallationChanges
) : CatalogInstaller {

  /**
   * The client used for http requests.
   */
  private val client get() = http.defaultClient

  /**
   * Adds the given extension to the downloads queue and returns an observable containing its
   * step in the installation process.
   *
   * @param catalog The catalog to install.
   */
  override fun install(catalog: CatalogRemote) = flow {
    emit(InstallStep.Downloading)
    val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
    val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
    try {
      val apkResponse = client.get(catalog.pkgUrl, cache = http.noStore).awaitSuccess()
      apkResponse.saveTo(tmpApkFile)

      val iconResponse = client.get(catalog.iconUrl, cache = http.noStore).awaitSuccess()
      iconResponse.saveTo(tmpIconFile)

      emit(InstallStep.Installing)

      val extDir = File(context.filesDir, "catalogs/${catalog.pkgName}").apply { mkdirs() }
      val apkFile = File(extDir, tmpApkFile.name)
      val iconFile = File(extDir, tmpIconFile.name)

      val apkSuccess = tmpApkFile.renameTo(apkFile)
      val iconSuccess = tmpIconFile.renameTo(iconFile)
      val success = apkSuccess && iconSuccess
      if (success) {
        installationChanges.notifyAppInstall(catalog.pkgName)
      }

      emit(if (success) InstallStep.Completed else InstallStep.Error)
    } catch (e: Exception) {
      Logger.warn(e, "Error installing package")
      emit(InstallStep.Error)
    } finally {
      tmpApkFile.delete()
      tmpIconFile.delete()
    }
  }

  /**
   * Starts an intent to uninstall the extension by the given package name.
   *
   * @param pkgName The package name of the extension to uninstall
   */
  override suspend fun uninstall(pkgName: String): Boolean {
    val file = File(context.filesDir, "catalogs/${pkgName}")
    val deleted = file.deleteRecursively()
    installationChanges.notifyAppUninstall(pkgName)
    return deleted
  }

}
