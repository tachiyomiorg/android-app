/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

/**
 * Composable used to apply the application colors to [content].
 * It applies the [MaterialTheme] colors and the app's [CustomColors].
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
  val vm = viewModel<AppThemeViewModel>()
  val (colors, customColors) = vm.getColors()
  val rememberedCustomColors = remember { CustomColors() }.apply {
    updateFrom(customColors)
  }
  vm.tintSystemBars(rememberedCustomColors.bars)

  CompositionLocalProvider(LocalCustomColors provides rememberedCustomColors) {
    MaterialTheme(colors = colors, content = content)
  }
}

private class AppThemeViewModel @Inject constructor(
  private val uiPreferences: UiPreferences
) : BaseViewModel() {
  private val themeMode by uiPreferences.themeMode().asState()
  private val lightTheme by uiPreferences.lightTheme().asState()
  private val darkTheme by uiPreferences.darkTheme().asState()

  private val baseThemeJob = SupervisorJob()
  private val baseThemeScope = CoroutineScope(baseThemeJob)

  @Composable
  fun getColors(): Pair<Colors, CustomColors> {
    val baseTheme = getBaseTheme(themeMode, lightTheme, darkTheme)
    val colors = remember(baseTheme.colors.isLight) {
      baseThemeJob.cancelChildren()

      if (baseTheme.colors.isLight) {
        uiPreferences.getLightColors().asState(baseThemeScope)
      } else {
        uiPreferences.getDarkColors().asState(baseThemeScope)
      }
    }

    val material = getMaterialColors(baseTheme.colors, colors.primary, colors.secondary)
    val custom = getCustomColors(baseTheme.customColors, colors.bars)
    return material to custom
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

  private fun getMaterialColors(
    baseColors: Colors,
    colorPrimary: Color,
    colorSecondary: Color
  ): Colors {
    val primary = colorPrimary.takeOrElse { baseColors.primary }
    val secondary = colorSecondary.takeOrElse { baseColors.secondary }
    return baseColors.copy(
      primary = primary,
      primaryVariant = primary,
      secondary = secondary,
      secondaryVariant = secondary,
      onPrimary = if (primary.luminance() > 0.5) Color.Black else Color.White,
      onSecondary = if (secondary.luminance() > 0.5) Color.Black else Color.White,
    )
  }

  private fun getCustomColors(colors: CustomColors, colorBars: Color): CustomColors {
    val appbar = colorBars.takeOrElse { colors.bars }
    return CustomColors(
      bars = appbar,
      onBars = if (appbar.luminance() > 0.5) Color.Black else Color.White
    )
  }

  @Composable
  fun tintSystemBars(color: Color) {
    val activity = LocalContext.current as Activity
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      activity.window.statusBarColor = color.toArgb()
      with(activity.window.decorView) {
        systemUiVisibility = if (color.luminance() > 0.5f) {
          systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
          systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      activity.window.navigationBarColor = color.toArgb()
      with(activity.window.decorView) {
        systemUiVisibility = if (color.luminance() > 0.5f) {
          systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
          systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
      }
    }
  }

  override fun onDestroy() {
    baseThemeScope.cancel()
  }

}
