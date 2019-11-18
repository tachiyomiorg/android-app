/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import tachiyomi.core.http.Http
import tachiyomi.core.prefs.AndroidPreferenceStore
import tachiyomi.core.prefs.LazyPreferenceStore
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.source.Dependencies
import tachiyomi.source.Source
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

/**
 * Class that handles the loading of the extensions installed in the system.
 */
internal class CatalogLoader @Inject constructor(
  private val context: Application,
  private val http: Http
) {

  /**
   * Return a list of all the installed extensions initialized concurrently.
   */
  fun loadExtensions(): List<Result> {
    val pkgManager = context.packageManager
    val installedPkgs = pkgManager.getInstalledPackages(PACKAGE_FLAGS)
    val extPkgs = installedPkgs.filter { isPackageAnExtension(it) }

    if (extPkgs.isEmpty()) return emptyList()

    // Load each extension concurrently and wait for completion
    return runBlocking {
      val deferred = extPkgs.map { pkgInfo ->
        async(Dispatchers.Default) {
          loadExtension(pkgInfo.packageName, pkgInfo)
        }
      }
      deferred.map { it.await() }
    }
  }

  /**
   * Attempts to load an extension from the given package name. It checks if the extension
   * contains the required feature flag before trying to load it.
   */
  fun loadExtensionFromPkgName(pkgName: String): Result {
    val pkgInfo = try {
      context.packageManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
    } catch (error: PackageManager.NameNotFoundException) {
      // Unlikely, but the package may have been uninstalled at this point
      return Result.Error(error)
    }
    if (!isPackageAnExtension(pkgInfo)) {
      return Result.Error("Tried to load a package that wasn't a extension")
    }
    return loadExtension(pkgName, pkgInfo)
  }

  /**
   * Loads an extension given its package name.
   *
   * @param pkgName The package name of the extension to load.
   * @param pkgInfo The package info of the extension.
   */
  private fun loadExtension(pkgName: String, pkgInfo: PackageInfo): Result {
    val pkgManager = context.packageManager

    val appInfo = try {
      pkgManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
    } catch (error: PackageManager.NameNotFoundException) {
      // Unlikely, but the package may have been uninstalled at this point
      return Result.Error(error)
    }

    val extName = pkgManager.getApplicationLabel(appInfo)?.toString().orEmpty()
    @Suppress("DEPRECATION")
    val versionCode = pkgInfo.versionCode
    val versionName = pkgInfo.versionName

    // Validate lib version
    val majorLibVersion = versionName.substringBefore('.').toInt()
    if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
      val exception = Exception("Lib version is $majorLibVersion, while only versions " +
        "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed")
      //Timber.w(exception) // TODO
      return Result.Error(exception)
    }

    val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

    val metadata = appInfo.metaData
    val sourceClassName = metadata.getString(METADATA_SOURCE_CLASS)?.trim()
      ?: return Result.Error("Source class not found in manifest")

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
      Timber.error(e) { "Extension load error: $extName." }
      return Result.Error(e)
    }

    val description = metadata.getString(METADATA_DESCRIPTION)?.takeIf { it.isNotEmpty() }
      ?: "Installed catalog description"

    val catalog = CatalogInstalled(
      source.name, description, source, pkgName, versionName, versionCode)
    return Result.Success(catalog)
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

  sealed class Result {
    class Success(val catalog: CatalogInstalled) : Result()
    class Error(val message: String? = null) : Result() {
      constructor(exception: Throwable) : this(exception.message)
    }
  }

}
