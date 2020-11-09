/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

data class Theme(
  val id: Int,
  val colors: Colors,
  val customColors: CustomColors
)

val themes = listOf(
  // Pure white
  Theme(1, lightColors(), CustomColors(
    bars = Color.White,
    onBars = Color.Black
  )),
  // Tachiyomi 0.x default colors
  Theme(2, lightColors(
    primary = Color(0xFF2979FF),
    primaryVariant = Color(0xFF2979FF),
    onPrimary = Color.White,
    secondary = Color(0xFF2979FF),
    secondaryVariant = Color(0xFF2979FF),
    onSecondary = Color.White
  ), CustomColors(
    bars = Color(0xFF54759E),
    onBars = Color.White
  )),
  // Tachiyomi 0.x dark theme
  Theme(3, darkColors(), CustomColors(
    bars = Color(0xFF212121),
    onBars = Color.White
  )),
  // AMOLED theme
  Theme(4, darkColors(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.Black
  ), CustomColors(
    bars = Color.Black,
    onBars = Color.White
  )),
)
