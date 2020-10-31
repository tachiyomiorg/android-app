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
  val colors: Colors
)

val themes = listOf(
  Theme(1, "White", lightColors(
    primary = Color.White,
    primaryVariant = Color.White,
    onPrimary = Color.Black
  )),
  Theme(2, "White/Blue", lightColors()),
  Theme(3, "Dark", darkColors()),
  Theme(4, "AMOLED", darkColors(
    primary = Color.Black,
    onPrimary = Color.White,
    surface = Color.Black
  )),
)

val themesById = themes.associateBy { it.id }
