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
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import tachiyomi.core.di.AppScope
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.StartScreen
import tachiyomi.ui.core.activity.BaseActivity
import tachiyomi.ui.core.theme.AppTheme

class MainActivity : BaseActivity() {

  private val uiPrefs = AppScope.getInstance<UiPreferences>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val startRoute = uiPrefs.startScreen().get().toRoute()

    WindowCompat.setDecorFitsSystemWindows(window, false)

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
