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
import androidx.compose.Composable
import androidx.compose.Model
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.Column
import androidx.ui.material.BottomNavigation
import androidx.ui.material.BottomNavigationItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Book
import androidx.ui.material.icons.filled.Explore
import androidx.ui.material.icons.filled.History
import androidx.ui.material.icons.filled.MoreHoriz
import androidx.ui.material.icons.filled.NewReleases
import androidx.ui.material.lightColorPalette
import androidx.ui.res.stringResource
import tachiyomi.ui.catalog.CatalogScreen
import tachiyomi.ui.library.LibraryScreen

sealed class Screen {
  object Library : Screen()
  object Catalogs : Screen()
  object Updates : Screen()
  object History : Screen()
  object More : Screen()
}

@Model
object HomeScreen {
  var current: Screen = Screen.Catalogs
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme(colors = lightColorPalette()) {
        Column {
          Box(modifier = Modifier.weight(1f)) {
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
        text = { Text(stringResource(item.text), maxLines = 1) },
        icon = { Icon(item.icon) },
        selected = HomeScreen.current == item.screen,
        onSelected = { HomeScreen.current = item.screen }
      )
    }
  }
}
