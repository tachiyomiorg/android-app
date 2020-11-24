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
import tachiyomi.ui.browse.CatalogsScreen
import tachiyomi.ui.browse.catalog.CatalogScreen
import tachiyomi.ui.browse.catalog.manga.CatalogMangaScreen
import tachiyomi.ui.categories.CategoriesScreen
import tachiyomi.ui.core.activity.BaseActivity
import tachiyomi.ui.core.theme.AppTheme
import tachiyomi.ui.core.theme.CustomColors
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

class MainActivity : BaseActivity() {

  private val uiPrefs = AppScope.getInstance<UiPreferences>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val startRoute = uiPrefs.startScreen().get().toRoute()

    setContent {
      AppTheme {
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

  companion object {
    const val SHORTCUT_DEEPLINK_MANGA = "tachiyomi.action.DEEPLINK_MANGA"
    const val SHORTCUT_DEEPLINK_CHAPTER = "tachiyomi.action.DEEPLINK_CHAPTER"
  }
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
