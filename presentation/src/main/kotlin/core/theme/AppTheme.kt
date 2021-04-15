/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import com.google.accompanist.coil.LocalImageLoader
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.core.coil.CoilLoaderFactory
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
  val systemUiController = rememberSystemUiController()
  SideEffect {
    systemUiController.setSystemBarsColor(customColors.bars, darkIcons = customColors.isBarLight)
  }

  CompositionLocalProvider(
    LocalCustomColors provides customColors,
    LocalImageLoader provides vm.coilLoader
  ) {
    ProvideWindowInsets {
      MaterialTheme(colors = colors, content = content)
    }
  }
}

private class AppThemeViewModel @Inject constructor(
  private val uiPreferences: UiPreferences,
  coilLoaderFactory: CoilLoaderFactory
) : BaseViewModel() {
  private val themeMode by uiPreferences.themeMode().asState()
  private val lightTheme by uiPreferences.lightTheme().asState()
  private val darkTheme by uiPreferences.darkTheme().asState()

  private val baseThemeJob = SupervisorJob()
  private val baseThemeScope = CoroutineScope(baseThemeJob)

  val coilLoader = coilLoaderFactory.create()

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
    val rememberedCustom = remember { CustomColors() }.apply { updateFrom(custom) }
    return material to rememberedCustom
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

  override fun onDestroy() {
    baseThemeScope.cancel()
  }

}
