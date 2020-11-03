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
import androidx.compose.ui.graphics.Color
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
import tachiyomi.domain.ui.model.StartScreen
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
import tachiyomi.ui.more.settings.SettingsAdvancedScreen
import tachiyomi.ui.more.settings.SettingsAppearance
import tachiyomi.ui.more.settings.SettingsBackupScreen
import tachiyomi.ui.more.settings.SettingsBrowseScreen
import tachiyomi.ui.more.settings.SettingsDownloadsScreen
import tachiyomi.ui.more.settings.SettingsGeneralScreen
import tachiyomi.ui.more.settings.SettingsLibraryScreen
import tachiyomi.ui.more.settings.SettingsParentalControlsScreen
import tachiyomi.ui.more.settings.SettingsReaderScreen
import tachiyomi.ui.more.settings.SettingsScreen
import tachiyomi.ui.more.settings.SettingsSecurityScreen
import tachiyomi.ui.more.settings.SettingsTrackingScreen
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
    val colorPrimary by uiPrefs.colorPrimary().asState()
    val colorSecondary by uiPrefs.colorSecondary().asState()
    val startRoute = uiPrefs.startScreen().get().toRoute()

    setContent {
      val theme = getCurrentTheme(themeMode, lightTheme, darkTheme, colorPrimary, colorSecondary)
      tintSystemBars(theme.colors)

      MaterialTheme(colors = theme.colors) {
        MainNavHost(startRoute)
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
  private fun getCurrentTheme(
    themeMode: ThemeMode,
    lightTheme: Int,
    darkTheme: Int,
    colorPrimary: Int,
    colorSecondary: Int
  ): Theme {
    return remember(themeMode, lightTheme, darkTheme, colorPrimary, colorSecondary) {
      fun getTheme(id: Int, fallbackIsLight: Boolean): Theme {
        return themes.find { it.id == id } ?: themes.first { it.colors.isLight == fallbackIsLight }
      }

      val baseTheme = when (themeMode) {
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

      val primary = if (colorPrimary != 0) {
        Color(colorPrimary)
      } else {
        baseTheme.colors.primary
      }
      val secondary = if (colorSecondary != 0) {
        Color(colorSecondary)
      } else {
        baseTheme.colors.secondary
      }
      baseTheme.copy(colors = baseTheme.colors.copy(
        primary = primary,
        secondary = secondary,
        secondaryVariant = secondary
      ))
    }
  }

  @Composable
  private fun tintSystemBars(colors: Colors) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
private fun MainNavHost(startRoute: Route) {
  val navController = rememberNavController()

  Scaffold(
    bodyContent = { paddingValues ->
      Box(Modifier.padding(paddingValues)) {
        NavHost(navController, startDestination = startRoute.id) {
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
      val currentScreen by navController.currentBackStackEntryAsState()
      val entryRoute = currentScreen?.arguments?.getString(KEY_ROUTE)

      // TODO: should hide on non-top-level routes. Not sure how to get the proper ID to check.
//      if (TopLevelRoutes.isTopLevelRoute(entryRoute)) {
      BottomNavigation {
        TopLevelRoutes.values.forEach {
          BottomNavigationItem(
            icon = { Icon(it.icon) },
            label = {
              Text(stringResource(it.text), maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            selected = entryRoute == it.route.id,
            onClick = { navController.navigate(it.route.id) },
          )
        }
//        }
      }
    }
  )
}

private fun StartScreen.toRoute(): Route {
  return when (this) {
    StartScreen.Library -> Route.Library
    StartScreen.Updates -> Route.Updates
    StartScreen.History -> Route.History
    StartScreen.Browse -> Route.Browse
    StartScreen.More -> Route.More
  }
}

private enum class TopLevelRoutes(val route: Route, val text: Int, val icon: VectorAsset) {
  Library(Route.Library, R.string.library_label, Icons.Default.Book),
  Updates(Route.Updates, R.string.updates_label, Icons.Default.NewReleases),
  History(Route.History, R.string.history_label, Icons.Default.History),
  Browse(Route.Browse, R.string.browse_label, Icons.Default.Explore),
  More(Route.More, R.string.more_label, Icons.Default.MoreHoriz);

  companion object {
    val values = values().toList()
    fun isTopLevelRoute(route: String?): Boolean {
      return route != null && values.any { it.route.id == route }
    }
  }
}
