/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import tachiyomi.ui.catalogs.CatalogsScreen
import tachiyomi.ui.catalogs.catalog.CatalogScreen
import tachiyomi.ui.history.HistoryScreen
import tachiyomi.ui.library.LibraryScreen
import tachiyomi.ui.more.MoreScreen
import tachiyomi.ui.updates.UpdatesScreen

sealed class Route(val id: String) {
  object Library : Route("library")
  object Catalogs : Route("catalogs")
  object Catalog : Route("catalog")
  object Updates : Route("updates")
  object History : Route("history")
  object More : Route("more")
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme(colors = darkColors()) {
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

  companion object {
    const val SHORTCUT_DEEPLINK_MANGA = "tachiyomi.action.DEEPLINK_MANGA"
    const val SHORTCUT_DEEPLINK_CHAPTER = "tachiyomi.action.DEEPLINK_CHAPTER"
  }

}

@Composable
private fun MainNavHost() {
  data class Item(val text: Int, val icon: VectorAsset, val route: Route)

  val navController = rememberNavController()

  val items = listOf(
    Item(R.string.label_library2, Icons.Default.Book, Route.Library),
    Item(R.string.label_catalogs, Icons.Default.Explore, Route.Catalogs),
    Item(R.string.label_updates, Icons.Default.NewReleases, Route.Updates),
    Item(R.string.label_history, Icons.Default.History, Route.History),
    Item(R.string.label_more, Icons.Default.MoreHoriz, Route.More)
  )

  Scaffold(
    bodyContent = {
      NavHost(navController, startDestination = Route.Catalogs.id) {
        composable(Route.Library.id) { LibraryScreen(navController) }

        // TODO: Have a NavHost per individual top-level route?
        composable(Route.Catalogs.id) { CatalogsScreen(navController) }
        composable(Route.Catalog.id + "?id={id}") { backStackEntry ->
          val id = backStackEntry.arguments?.get("id") as String
          CatalogScreen(navController, id.toLong())
        }

        composable(Route.Updates.id) { UpdatesScreen(navController) }
        composable(Route.History.id) { HistoryScreen(navController) }
        composable(Route.More.id) { MoreScreen(navController) }
      }
    },
    bottomBar = {
      BottomNavigation {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val entryRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
        items.forEach {
          BottomNavigationItem(
            icon = { Icon(it.icon) },
            label = {
              Text(stringResource(it.text), maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            selected = entryRoute == it.route.id,
            onClick = { navController.navigate(it.route.id) }
          )
        }
      }
    }
  )

}
