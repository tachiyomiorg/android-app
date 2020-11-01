/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavType
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import tachiyomi.core.di.AppScope
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.browse.CatalogsScreen
import tachiyomi.ui.browse.catalog.CatalogScreen
import tachiyomi.ui.browse.catalog.manga.CatalogMangaScreen
import tachiyomi.ui.core.activity.BaseActivity
import tachiyomi.ui.core.theme.Theme
import tachiyomi.ui.core.theme.themes
import tachiyomi.ui.history.HistoryScreen
import tachiyomi.ui.library.LibraryScreen
import tachiyomi.ui.library.manga.LibraryMangaScreen
import tachiyomi.ui.more.MoreScreen
import tachiyomi.ui.more.SettingsAdvancedScreen
import tachiyomi.ui.more.SettingsAppearance
import tachiyomi.ui.more.SettingsBackupScreen
import tachiyomi.ui.more.SettingsBrowseScreen
import tachiyomi.ui.more.SettingsDownloadsScreen
import tachiyomi.ui.more.SettingsGeneralScreen
import tachiyomi.ui.more.SettingsLibraryScreen
import tachiyomi.ui.more.SettingsParentalControlsScreen
import tachiyomi.ui.more.SettingsReaderScreen
import tachiyomi.ui.more.SettingsScreen
import tachiyomi.ui.more.SettingsSecurityScreen
import tachiyomi.ui.more.SettingsTrackingScreen
import tachiyomi.ui.updates.UpdatesScreen

sealed class Route(val id: String) {
  object Library : Route("library")
  object LibraryManga : Route("library/manga")

  object Updates : Route("updates")

  object History : Route("history")

  object Browse : Route("browse")
  object BrowseCatalog : Route("browse/catalog")
  object BrowseCatalogManga : Route("browse/catalog/manga")

  object More : Route("more")

  object Settings : Route("settings")
  object SettingsGeneral : Route("settings/general")
  object SettingsAppearance : Route("settings/appearance")
  object SettingsLibrary : Route("settings/library")
  object SettingsReader : Route("settings/reader")
  object SettingsDownloads : Route("settings/downloads")
  object SettingsTracking : Route("settings/tracking")
  object SettingsBrowse : Route("settings/browse")
  object SettingsBackup : Route("settings/backup")
  object SettingsSecurity : Route("settings/security")
  object SettingsParentalControls : Route("settings/parentalControls")
  object SettingsAdvanced : Route("settings/advanced")
}

class MainActivity : BaseActivity() {

  private val uiPrefs = AppScope.getInstance<UiPreferences>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val themeMode by uiPrefs.themeMode().asState()
    val lightTheme by uiPrefs.lightTheme().asState()
    val darkTheme by uiPrefs.darkTheme().asState()

    setContent {
      val theme = getCurrentTheme(themeMode, lightTheme, darkTheme)
      tintSystemBars(theme.colors)

      MaterialTheme(colors = theme.colors) {
        MainNavHost()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    if (!handleIntentAction(intent)) {
      super.onNewIntent(intent)
    }
  }

  private fun handleIntentAction(intent: Intent): Boolean {
    when (intent.action) {
      SHORTCUT_DEEPLINK_CHAPTER -> {
      }
      SHORTCUT_DEEPLINK_MANGA -> {
      }
      else -> return false
    }
    return true
  }

  @Composable
  private fun getCurrentTheme(themeMode: ThemeMode, lightTheme: Int, darkTheme: Int): Theme {
    return remember(themeMode, lightTheme, darkTheme) {
      fun getTheme(id: Int, fallbackIsLight: Boolean): Theme {
        return themes.find { it.id == id } ?: themes.first { it.colors.isLight == fallbackIsLight }
      }

      when (themeMode) {
        ThemeMode.System -> {
          if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
          ) {
            getTheme(darkTheme, false)
          } else {
            getTheme(lightTheme, true)
          }
        }
        ThemeMode.Light -> getTheme(lightTheme, true)
        ThemeMode.Dark -> getTheme(darkTheme, false)
      }
    }
  }

  @Composable
  private fun tintSystemBars(colors: Colors) {
    if (Build.VERSION.SDK_INT >= 23) {
      val statusBarColor = colors.primarySurface
      window.statusBarColor = statusBarColor.toArgb()
      with(window.decorView) {
        systemUiVisibility = if (statusBarColor.luminance() > 0.5f) {
          systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
          systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
      }
    }
    if (Build.VERSION.SDK_INT >= 26) {
      val navBarColor = colors.primarySurface
      window.navigationBarColor = navBarColor.toArgb()
      with(window.decorView) {
        systemUiVisibility = if (navBarColor.luminance() > 0.5f) {
          systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
          systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
      }
    }
  }

  companion object {
    const val SHORTCUT_DEEPLINK_MANGA = "tachiyomi.action.DEEPLINK_MANGA"
    const val SHORTCUT_DEEPLINK_CHAPTER = "tachiyomi.action.DEEPLINK_CHAPTER"
  }
}

@Composable
private fun MainNavHost() {
  data class TopLevelRoute(val text: Int, val icon: VectorAsset, val route: Route)

  val navController = rememberNavController()

  val topLevelRoutes = listOf(
    TopLevelRoute(R.string.library_label, Icons.Default.Book, Route.Library),
    TopLevelRoute(R.string.updates_label, Icons.Default.NewReleases, Route.Updates),
    TopLevelRoute(R.string.history_label, Icons.Default.History, Route.History),
    TopLevelRoute(R.string.browse_label, Icons.Default.Explore, Route.Browse),
    TopLevelRoute(R.string.more_label, Icons.Default.MoreHoriz, Route.More)
  )

  Scaffold(
    bodyContent = { paddingValues ->
      Box(Modifier.padding(paddingValues)) {
        NavHost(navController, startDestination = Route.Browse.id) {
          composable(Route.Library.id) { LibraryScreen(navController) }
          composable(
            "${Route.LibraryManga.id}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
          ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") as Long
            LibraryMangaScreen(navController, id)
          }

          composable(Route.Updates.id) { UpdatesScreen(navController) }

          composable(Route.History.id) { HistoryScreen(navController) }

          // TODO: Have a NavHost per individual top-level route?
          composable(Route.Browse.id) { CatalogsScreen(navController) }
          composable(
            "${Route.BrowseCatalog.id}/{sourceId}",
            arguments = listOf(navArgument("sourceId") { type = NavType.LongType })
          ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getLong("sourceId") as Long
            CatalogScreen(navController, sourceId)
          }
          composable(
            "${Route.BrowseCatalogManga.id}/{sourceId}/{mangaId}",
            arguments = listOf(
              navArgument("sourceId") { type = NavType.LongType },
              navArgument("mangaId") { type = NavType.LongType },
            )
          ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getLong("sourceId") as Long
            val mangaId = backStackEntry.arguments?.getLong("mangaId") as Long
            CatalogMangaScreen(navController, sourceId, mangaId)
          }

          composable(Route.More.id) { MoreScreen(navController) }

          composable(Route.Settings.id) { SettingsScreen(navController) }
          composable(Route.SettingsGeneral.id) { SettingsGeneralScreen(navController) }
          composable(Route.SettingsAppearance.id) { SettingsAppearance(navController) }
          composable(Route.SettingsLibrary.id) { SettingsLibraryScreen(navController) }
          composable(Route.SettingsReader.id) { SettingsReaderScreen(navController) }
          composable(Route.SettingsDownloads.id) { SettingsDownloadsScreen(navController) }
          composable(Route.SettingsTracking.id) { SettingsTrackingScreen(navController) }
          composable(Route.SettingsBrowse.id) { SettingsBrowseScreen(navController) }
          composable(Route.SettingsBackup.id) { SettingsBackupScreen(navController) }
          composable(Route.SettingsSecurity.id) { SettingsSecurityScreen(navController) }
          composable(Route.SettingsParentalControls.id) {
            SettingsParentalControlsScreen(navController)
          }
          composable(Route.SettingsAdvanced.id) { SettingsAdvancedScreen(navController) }
        }
      }
    },
    bottomBar = {
      // TODO: should only be shown if at a top-level route
      BottomNavigation {
        val currentScreen by navController.currentBackStackEntryAsState()
        val entryRoute = currentScreen?.arguments?.getString(KEY_ROUTE)
        topLevelRoutes.forEach {
          BottomNavigationItem(
            icon = { Icon(it.icon) },
            label = {
              Text(stringResource(it.text), maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            selected = entryRoute == it.route.id,
            onClick = { navController.navigate(it.route.id) },
          )
        }
      }
    }
  )
}
