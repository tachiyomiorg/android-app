/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

class CustomColors(
  bars: Color = Color.Unspecified,
  onBars: Color = Color.Unspecified
) {
  var bars by mutableStateOf(bars, structuralEqualityPolicy())
    private set
  var onBars by mutableStateOf(onBars, structuralEqualityPolicy())
    private set

  fun updateFrom(other: CustomColors) {
    bars = other.bars
    onBars = other.onBars
  }

  companion object {
    inline val current
      @ReadOnlyComposable
      @Composable
      get() = LocalCustomColors.current
  }

}

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }
