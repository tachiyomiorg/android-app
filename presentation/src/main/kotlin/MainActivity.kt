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
import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import tachiyomi.ui.catalog.CatalogScreen
import tachiyomi.ui.library.LibraryScreen

sealed class Screen {
  object Library : Screen()
  object Catalogs : Screen()
  object Updates : Screen()
  object History : Screen()
  object More : Screen()
}

object HomeScreen {
  var current by mutableStateOf<Screen>(Screen.Catalogs)
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme(colors = darkColors()) {
        Column {
          Surface(modifier = Modifier.weight(1f)) {
            when (HomeScreen.current) {
              Screen.Library -> LibraryScreen()
              Screen.Catalogs -> CatalogScreen()
              Screen.Updates -> Box()
              Screen.History -> Box()
              Screen.More -> Box()
            }
          }
          HomeBottomNav()
        }
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
private fun HomeBottomNav() {
  data class Item(val text: Int, val icon: VectorAsset, val screen: Screen)

  val items = listOf(
    Item(R.string.label_library2, Icons.Default.Book, Screen.Library),
    Item(R.string.label_catalogues, Icons.Default.Explore, Screen.Catalogs),
    Item(R.string.label_updates, Icons.Default.NewReleases, Screen.Updates),
    Item(R.string.label_history, Icons.Default.History, Screen.History),
    Item(R.string.label_more, Icons.Default.MoreHoriz, Screen.More)
  )

  BottomNavigation {
    for (item in items) {
      BottomNavigationItem(
        label = { Text(stringResource(item.text), maxLines = 1) },
        icon = { Icon(item.icon) },
        selected = HomeScreen.current == item.screen,
        onSelect = { HomeScreen.current = item.screen }
      )
    }
  }
}
