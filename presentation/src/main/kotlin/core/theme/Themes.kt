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
  val name: String,
  val colors: Colors,
  val customColors: CustomColors
)

val themes = listOf(
  Theme(1, "White", lightColors(), CustomColors(
    bars = Color.White,
    onBars = Color.Black
  )),
  Theme(2, "White/Blue", lightColors(
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
  Theme(3, "Dark", darkColors(), CustomColors(
    bars = Color(0xFF212121),
    onBars = Color.White
  )),
  Theme(4, "AMOLED", darkColors(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.Black
  ), CustomColors(
    bars = Color.Black,
    onBars = Color.White
  )),
)

val themesById = themes.associateBy { it.id }
