/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.service

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.tinylog.kotlin.Logger
import tachiyomi.core.http.Http
import tachiyomi.core.prefs.AndroidPreferenceStore
import tachiyomi.core.prefs.LazyPreferenceStore
import tachiyomi.data.BuildConfig
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.service.CatalogLoader
import tachiyomi.source.Dependencies
import tachiyomi.source.Source
import tachiyomi.source.TestSource
import javax.inject.Inject

/**
 * Class that handles the loading of the extensions installed in the system.
 */
internal class AndroidCatalogLoader @Inject constructor(
  private val context: Application,
  private val http: Http
) : CatalogLoader {

  /**
   * Return a list of all the installed extensions initialized concurrently.
   */
  override fun loadAll(): List<CatalogLocal> {
    val internalCatalogs = mutableListOf<CatalogLocal>()
    if (BuildConfig.DEBUG) {
      val testCatalog = CatalogInternal(TestSource(), "Source used for testing")
      internalCatalogs.add(testCatalog)
    }

    val pkgManager = context.packageManager
    val installedPkgs = pkgManager.getInstalledPackages(PACKAGE_FLAGS)
    val extPkgs = installedPkgs.filter { isPackageAnExtension(it) }

    if (extPkgs.isEmpty()) return internalCatalogs

    // Load each extension concurrently and wait for completion
    val installedCatalogs = runBlocking {
      val deferred = extPkgs.map { pkgInfo ->
        async(Dispatchers.Default) {
          loadExtension(pkgInfo.packageName, pkgInfo)
        }
      }
      deferred.awaitAll()
    }.filterNotNull()

    return internalCatalogs + installedCatalogs
  }

  /**
   * Attempts to load an extension from the given package name. It checks if the extension
   * contains the required feature flag before trying to load it.
   */
  override fun load(pkgName: String): CatalogLocal? {
    val pkgInfo = try {
      context.packageManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
    } catch (error: PackageManager.NameNotFoundException) {
      // Unlikely, but the package may have been uninstalled at this point
      Logger.warn("Failed to load extension: the package $pkgName isn't installed, ignoring...")
      return null
    }
    if (!isPackageAnExtension(pkgInfo)) {
      Logger.warn("The package $pkgName isn't an extension, ignoring...")
      return null
    }
    return loadExtension(pkgName, pkgInfo)
  }

  /**
   * Loads an extension given its package name.
   *
   * @param pkgName The package name of the extension to load.
   * @param pkgInfo The package info of the extension.
   */
  private fun loadExtension(pkgName: String, pkgInfo: PackageInfo): CatalogLocal? {
    val pkgManager = context.packageManager

    val appInfo = try {
      pkgManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
    } catch (error: PackageManager.NameNotFoundException) {
      // Unlikely, but the package may have been uninstalled at this point
      Logger.warn("Failed to load extension: the package $pkgName isn't installed, ignoring...")
      return null
    }

    val extName = pkgManager.getApplicationLabel(appInfo).toString()

    @Suppress("DEPRECATION")
    val versionCode = pkgInfo.versionCode
    val versionName = pkgInfo.versionName

    // Validate lib version
    val majorLibVersion = versionName.substringBefore('.').toInt()
    if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
      val exception = "Failed to load extension: the package $pkgName lib version is " +
        "$majorLibVersion, while only versions " +
        "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed"
      Logger.warn(exception)
      return null
    }

    val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

    val metadata = appInfo.metaData
    val sourceClassName = metadata.getString(METADATA_SOURCE_CLASS)?.trim()
    if (sourceClassName == null) {
      Logger.warn("Failed to load extension: the package $pkgName didn't define source class")
      return null
    }

    val fullSourceClassName = if (sourceClassName.startsWith(".")) {
      pkgInfo.packageName + sourceClassName
    } else {
      sourceClassName
    }

    val dependencies = Dependencies(
      http,
      LazyPreferenceStore(lazy {
        AndroidPreferenceStore(context.getSharedPreferences(pkgName, Context.MODE_PRIVATE))
      })
    )

    val source = try {
      val obj = Class.forName(fullSourceClassName, false, classLoader)
        .getConstructor(Dependencies::class.java)
        .newInstance(dependencies)

      obj as? Source ?: throw Exception("Unknown source class type! ${obj.javaClass}")
    } catch (e: Throwable) {
      Logger.warn(e, "Failed to load extension: the package $pkgName threw an exception")
      return null
    }

    val description = metadata.getString(METADATA_DESCRIPTION).orEmpty()

    return CatalogInstalled(source.name, description, source, pkgName, versionName, versionCode)
  }

  /**
   * Returns true if the given package is an extension.
   *
   * @param pkgInfo The package info of the application.
   */
  private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
    return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
  }

  private companion object {
    const val EXTENSION_FEATURE = "tachiyomix"
    const val METADATA_SOURCE_CLASS = "source.class"
    const val METADATA_DESCRIPTION = "source.description"
    const val METADATA_NSFW = "source.nsfw"
    const val LIB_VERSION_MIN = 1
    const val LIB_VERSION_MAX = 1

    @Suppress("DEPRECATION")
    const val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS
  }

}
