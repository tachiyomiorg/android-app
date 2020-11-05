/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import tachiyomi.core.prefs.Preference
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.ui.core.prefs.PreferenceMutableState
import tachiyomi.ui.core.prefs.asColor
import tachiyomi.ui.core.prefs.asStateIn

data class PreferenceColors(
  val primary: Preference<Int>,
  val secondary: Preference<Int>,
  val bars: Preference<Int>
)

class PreferenceColorsState(
  val primaryState: PreferenceMutableState<Color>,
  val secondaryState: PreferenceMutableState<Color>,
  val barsState: PreferenceMutableState<Color>
) {
  val primary by primaryState
  val secondary by secondaryState
  val bars by barsState
}

fun UiPreferences.getLightColors(): PreferenceColors {
  return PreferenceColors(
    colorPrimaryLight(),
    colorSecondaryLight(),
    colorBarsLight()
  )
}

fun UiPreferences.getDarkColors(): PreferenceColors {
  return PreferenceColors(
    colorPrimaryDark(),
    colorSecondaryDark(),
    colorBarsDark()
  )
}

fun PreferenceColors.asState(scope: CoroutineScope): PreferenceColorsState {
  return PreferenceColorsState(
    primary.asColor().asStateIn(scope),
    secondary.asColor().asStateIn(scope),
    bars.asColor().asStateIn(scope)
  )
}
