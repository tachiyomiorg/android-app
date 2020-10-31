/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more

import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.theme.Theme
import tachiyomi.ui.core.theme.themesById
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class ThemesViewModel @Inject constructor(
  private val uiPreferences: UiPreferences,
) : BaseViewModel() {

  var themeMode = uiPreferences.themeMode().asState()
  var lightTheme = uiPreferences.lightTheme().asState()
  var darkTheme = uiPreferences.darkTheme().asState()

}

@Composable
fun ThemesScreen(navController: NavHostController) {
  val vm = viewModel<ThemesViewModel>()

  Column {
    TopAppBar(
      title = { Text("Appearance") },
      navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.Default.ArrowBack)
        }
      },
    )
    PreferencesScrollableColumn {
      ChoicePref(
        preference = vm.themeMode,
        choices = mapOf(
          ThemeMode.System to "System default",
          ThemeMode.Light to "Light",
          ThemeMode.Dark to "Dark"
        ),
        title = "Theme mode"
      )
      if (vm.themeMode.value != ThemeMode.Dark) {
        ChoicePref(
          preference = vm.lightTheme,
          choices = themesById,
          title = "Light theme",
          subtitle = themesById[vm.lightTheme.value]?.name
        ) { _, theme ->
          if (theme.colors.isLight) {
            MaterialTheme(theme.colors) {
              Surface(modifier = Modifier.padding(8.dp), elevation = 4.dp) {
                ThemeItem(theme)
              }
            }
          }
        }
      }
      if (vm.themeMode.value != ThemeMode.Light) {
        ChoicePref(
          preference = vm.darkTheme,
          choices = themesById,
          title = "Light theme",
          subtitle = themesById[vm.darkTheme.value]?.name
        ) { _, theme ->
          if (!theme.colors.isLight) {
            MaterialTheme(theme.colors) {
              Surface(modifier = Modifier.padding(8.dp), elevation = 4.dp) {
                ThemeItem(theme)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ThemeItem(theme: Theme) {
  Column {
    TopAppBar(
      title = { Text(theme.name) },
      actions = {
        IconButton(onClick = {}, enabled = false) {
          Icon(Icons.Default.MoreVert)
        }
      }
    )
    Surface(modifier = Modifier.fillMaxWidth().height(124.dp)) {
      Box(Modifier.padding(16.dp)) {
        Text(text = "Primary text")
        StatelessFloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd)) {
          Icon(Icons.Default.Edit)
        }
      }
    }
  }
}

@Composable
private fun StatelessFloatingActionButton(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
  backgroundColor: Color = MaterialTheme.colors.secondary,
  contentColor: Color = contentColorFor(backgroundColor),
  elevation: Dp = 6.dp,
  icon: @Composable () -> Unit
) {
  Surface(
    modifier = modifier,
    shape = shape,
    color = backgroundColor,
    contentColor = contentColor,
    elevation = elevation
  ) {
    ProvideTextStyle(MaterialTheme.typography.button) {
      Box(modifier = Modifier.size(56.dp), alignment = Alignment.Center) {
        icon()
      }
    }
  }
}
