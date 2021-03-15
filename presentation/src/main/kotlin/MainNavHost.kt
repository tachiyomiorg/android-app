/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import tachiyomi.ui.browse.CatalogsScreen
import tachiyomi.ui.browse.catalog.CatalogScreen
import tachiyomi.ui.browse.catalog.manga.CatalogMangaScreen
import tachiyomi.ui.categories.CategoriesScreen
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.downloads.DownloadQueueScreen
import tachiyomi.ui.history.HistoryScreen
import tachiyomi.ui.library.LibraryScreen
import tachiyomi.ui.library.manga.LibraryMangaScreen
import tachiyomi.ui.more.AboutScreen
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(startRoute: Route) {
  val navController = rememberNavController()

  Scaffold(
    content = { paddingValues ->
      Box(Modifier.padding(paddingValues)) {
        NavHost(navController, startDestination = startRoute.id) {
          // TODO: Have a NavHost per individual top-level route?

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
            val mangaId = backStackEntry.arguments?.getLong("mangaId") as Long
            CatalogMangaScreen(navController, mangaId)
          }

          composable(Route.More.id) { MoreScreen(navController) }
          composable(Route.Categories.id) { CategoriesScreen(navController) }
          composable(Route.DownloadQueue.id) { DownloadQueueScreen(navController) }
          composable(Route.About.id) { AboutScreen(navController) }

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
      val currentRoute = currentScreen?.arguments?.getString(KEY_ROUTE)

      AnimatedVisibility(
        visible = TopLevelRoutes.isTopLevelRoute(currentRoute),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
      ) {
        BottomNavigation(
          backgroundColor = CustomColors.current.bars,
          contentColor = CustomColors.current.onBars,
        ) {
          TopLevelRoutes.values.forEach {
            BottomNavigationItem(
              icon = { Icon(it.icon, contentDescription = null) },
              label = {
                Text(stringResource(it.text), maxLines = 1, overflow = TextOverflow.Ellipsis)
              },
              selected = currentRoute == it.route.id,
              onClick = {
                if (currentRoute != it.route.id) {
                  navController.popBackStack(navController.graph.startDestination, false)
                  navController.navigate(it.route.id)
                }
              },
            )
          }
        }
      }
    }
  )
}

private enum class TopLevelRoutes(val route: Route, val text: Int, val icon: ImageVector) {
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
