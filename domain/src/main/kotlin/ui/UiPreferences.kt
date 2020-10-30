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

}
