/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.R
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.theme.AppColorsPreferenceState
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.theme.Theme
import tachiyomi.ui.core.theme.asState
import tachiyomi.ui.core.theme.getDarkColors
import tachiyomi.ui.core.theme.getLightColors
import tachiyomi.ui.core.theme.themes
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class ThemesViewModel @Inject constructor(
  private val uiPreferences: UiPreferences,
) : BaseViewModel() {

  val themeMode = uiPreferences.themeMode().asState()
  val lightTheme = uiPreferences.lightTheme().asState()
  val darkTheme = uiPreferences.darkTheme().asState()
  val lightColors = uiPreferences.getLightColors().asState(scope)
  val darkColors = uiPreferences.getDarkColors().asState(scope)

  @Composable
  fun getActiveColors(): AppColorsPreferenceState {
    return if (MaterialTheme.colors.isLight) lightColors else darkColors
  }
}

@Composable
fun SettingsAppearance(navController: NavHostController) {
  val vm = viewModel<ThemesViewModel>()

  val activeColors = vm.getActiveColors()
  val isLight = MaterialTheme.colors.isLight
  val themesForCurrentMode = remember(isLight) {
    themes.filter { it.colors.isLight == isLight }
  }

  Column {
    Toolbar(
      title = { Text(stringResource(R.string.appearance_label)) },
      navigationIcon = { BackIconButton(navController) },
    )
    PreferencesScrollableColumn {
      ChoicePref(
        preference = vm.themeMode,
        choices = mapOf(
          ThemeMode.System to R.string.follow_system_settings,
          ThemeMode.Light to R.string.light,
          ThemeMode.Dark to R.string.dark
        ),
        title = R.string.theme
      )
      Text("Preset themes", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
      LazyRowFor(themesForCurrentMode, Modifier.padding(horizontal = 8.dp)) { theme ->
        ThemeItem(theme, onClick = {
          (if (isLight) vm.lightTheme else vm.darkTheme).value = it.id
          activeColors.primaryState.value = it.colors.primary
          activeColors.secondaryState.value = it.colors.secondary
          activeColors.barsState.value = it.customColors.bars
        })
      }

      ColorPref(preference = activeColors.primaryState, title = "Color primary",
        subtitle = "Displayed most frequently across your app",
        unsetColor = MaterialTheme.colors.primary)
      ColorPref(preference = activeColors.secondaryState, title = "Color secondary",
        subtitle = "Accents select parts of the UI",
        unsetColor = MaterialTheme.colors.secondary)
      ColorPref(preference = activeColors.barsState, title = "Toolbar color",
        unsetColor = CustomColors.current.bars)
    }
  }
}

@Composable
private fun ThemeItem(
  theme: Theme,
  onClick: (Theme) -> Unit
) {
  val borders = MaterialTheme.shapes.small
  val borderColor = if (theme.colors.isLight) {
    Color.Black.copy(alpha = 0.25f)
  } else {
    Color.White.copy(alpha = 0.15f)
  }
  Surface(elevation = 4.dp, color = theme.colors.background, shape = borders,
    modifier = Modifier
      .size(100.dp, 160.dp)
      .padding(8.dp)
      .border(1.dp, borderColor, borders)
      .clickable(onClick = { onClick(theme) })
  ) {
    Column {
      Toolbar(modifier = Modifier.height(24.dp), title = emptyContent(),
        backgroundColor = theme.customColors.bars)
      Box(Modifier.fillMaxWidth().weight(1f).padding(6.dp)) {
        Text("Text", fontSize = 11.sp)
        Button(
          onClick = {},
          enabled = false,
          contentPadding = PaddingValues(),
          modifier = Modifier.align(Alignment.BottomStart).size(40.dp, 20.dp),
          content = {},
          colors = ButtonConstants.defaultButtonColors(
            disabledBackgroundColor = theme.colors.primary
          )
        )
        Surface(Modifier.size(24.dp).align(Alignment.BottomEnd),
          shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
          color = theme.colors.secondary,
          elevation = 6.dp,
          content = emptyContent()
        )
      }
      BottomAppBar(Modifier.height(24.dp), backgroundColor = theme.customColors.bars) {
      }
    }
  }
}
