/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.useOrElse
import androidx.compose.ui.platform.ContextAmbient
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.core.prefs.asColor
import tachiyomi.ui.core.theme.Theme
import tachiyomi.ui.core.theme.themes
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

@Composable
fun AppTheme(content: @Composable () -> Unit) {
  val vm = viewModel<AppThemeViewModel>()
  val colors = vm.getColors()
  tintSystemBars(colors)

  MaterialTheme(colors = colors, content = content)
}

class AppThemeViewModel @Inject constructor(
  private val uiPreferences: UiPreferences
) : BaseViewModel() {
  private val themeMode by uiPreferences.themeMode().asState()
  private val lightTheme by uiPreferences.lightTheme().asState()
  private val darkTheme by uiPreferences.darkTheme().asState()
  private val colorPrimary by uiPreferences.colorPrimary().asColor().asState()
  private val colorSecondary by uiPreferences.colorSecondary().asColor().asState()

  @Composable
  fun getColors(): Colors {
    val baseTheme = getBaseTheme(themeMode, lightTheme, darkTheme)
    return getAppColors(baseTheme.colors, colorPrimary, colorSecondary)
  }

  @Composable
  private fun getBaseTheme(
    themeMode: ThemeMode,
    lightTheme: Int,
    darkTheme: Int
  ): Theme {
    fun getTheme(id: Int, fallbackIsLight: Boolean): Theme {
      return themes.find { it.id == id } ?: themes.first { it.colors.isLight == fallbackIsLight }
    }

    return when (themeMode) {
      ThemeMode.System -> if (!isSystemInDarkTheme()) {
        getTheme(lightTheme, true)
      } else {
        getTheme(darkTheme, false)
      }
      ThemeMode.Light -> getTheme(lightTheme, true)
      ThemeMode.Dark -> getTheme(darkTheme, false)
    }
  }

  @Composable
  private fun getAppColors(
    baseColors: Colors,
    colorPrimary: Color,
    colorSecondary: Color
  ): Colors {
    val primary = colorPrimary.useOrElse { baseColors.primary }
    val secondary = colorSecondary.useOrElse { baseColors.secondary }
    return baseColors.copy(
      primary = primary,
      primaryVariant = primary,
      secondary = secondary,
      secondaryVariant = secondary,
      onPrimary = if (primary.luminance() > 0.5) Color.Black else Color.White,
      onSecondary = if (secondary.luminance() > 0.5) Color.Black else Color.White,
    )
  }

}

@Composable
private fun tintSystemBars(colors: Colors) {
  val activity = ContextAmbient.current as Activity
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val statusBarColor = colors.primarySurface
    activity.window.statusBarColor = statusBarColor.toArgb()
    with(activity.window.decorView) {
      systemUiVisibility = if (statusBarColor.luminance() > 0.5f) {
        systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      } else {
        systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
      }
    }
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val navBarColor = colors.primarySurface
    activity.window.navigationBarColor = navBarColor.toArgb()
    with(activity.window.decorView) {
      systemUiVisibility = if (navBarColor.luminance() > 0.5f) {
        systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
      } else {
        systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
      }
    }
  }
}
