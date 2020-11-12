/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.ui

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import tachiyomi.core.prefs.getEnum
import tachiyomi.domain.ui.model.StartScreen
import tachiyomi.domain.ui.model.ThemeMode

class UiPreferences(private val preferenceStore: PreferenceStore) {

  fun themeMode(): Preference<ThemeMode> {
    return preferenceStore.getEnum("theme_mode", ThemeMode.System)
  }

  fun lightTheme(): Preference<Int> {
    return preferenceStore.getInt("theme_light", 0)
  }

  fun darkTheme(): Preference<Int> {
    return preferenceStore.getInt("theme_dark", 0)
  }

  fun colorPrimaryLight(): Preference<Int> {
    return preferenceStore.getInt("color_primary_light", 0)
  }

  fun colorPrimaryDark(): Preference<Int> {
    return preferenceStore.getInt("color_primary_dark", 0)
  }

  fun colorSecondaryLight(): Preference<Int> {
    return preferenceStore.getInt("color_secondary_light", 0)
  }

  fun colorSecondaryDark(): Preference<Int> {
    return preferenceStore.getInt("color_secondary_dark", 0)
  }

  fun colorBarsLight(): Preference<Int> {
    return preferenceStore.getInt("color_bar_light", 0)
  }

  fun colorBarsDark(): Preference<Int> {
    return preferenceStore.getInt("color_bar_dark", 0)
  }

  fun startScreen(): Preference<StartScreen> {
    return preferenceStore.getEnum("start_screen", StartScreen.Library)
  }

  fun confirmExit(): Preference<Boolean> {
    return preferenceStore.getBoolean("confirm_exit", false)
  }

  fun hideBottomBarOnScroll(): Preference<Boolean> {
    return preferenceStore.getBoolean("hide_bottom_bar_on_scroll", true)
  }

  fun language(): Preference<String> {
    return preferenceStore.getString("language", "")
  }

  fun dateFormat(): Preference<String> {
    return preferenceStore.getString("date_format", "")
  }

  fun downloadedOnly(): Preference<Boolean> {
    return preferenceStore.getBoolean("downloaded_only", false)
  }

  fun incognitoMode(): Preference<Boolean> {
    return preferenceStore.getBoolean("incognito_mode", false)
  }

}
