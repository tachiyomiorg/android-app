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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.ThemeMode
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

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

class ThemesViewModel @Inject constructor(
  private val uiPreferences: UiPreferences
) : BaseViewModel() {

  var themeMode by uiPreferences.themeMode().asState()
  var lightTheme by uiPreferences.lightTheme().asState()
  var darkTheme by uiPreferences.darkTheme().asState()
  var dialog by mutableStateOf<(@Composable () -> Unit)?>(null)

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
    val themeModes = mapOf(
      ThemeMode.System to "System default",
      ThemeMode.Light to "Light",
      ThemeMode.Dark to "Dark"
    )
    SettingsRow(
      title = "Theme mode",
      subtitle = themeModes[vm.themeMode],
      onClick = {
        vm.dialog = {
          ChoiceDialog(
            items = themeModes.toList(),
            selected = vm.themeMode,
            title = { Text("Theme mode") },
            onDismissRequest = { vm.dialog = null },
            onSelected = { selected ->
              vm.themeMode = selected
              vm.dialog = null
            }
          )
        }
      }
    )
    if (vm.themeMode != ThemeMode.Dark) {
      val lightThemes = themes.filter { it.colors.isLight }
      SettingsRow(
        title = "Light theme",
        subtitle = lightThemes.find { it.id == vm.lightTheme }?.name,
        onClick = {
          vm.dialog = {
            AlertDialog(
              onDismissRequest = { vm.dialog = null },
              title = { Text("Select a theme") },
              text = {
                LazyColumnFor(items = lightThemes) { theme ->
                  MaterialTheme(theme.colors) {
                    Surface(
                      modifier = Modifier.padding(8.dp).clickable(onClick = {
                        vm.lightTheme = theme.id
                        vm.dialog = null
                      }),
                      elevation = 4.dp
                    ) {
                      ThemeItem(theme)
                    }
                  }
                }
              },
              buttons = {}
            )
          }
        }
      )
    }
    if (vm.themeMode != ThemeMode.Light) {
      val darkThemes = themes.filter { !it.colors.isLight }
      SettingsRow(
        title = "Dark theme",
        subtitle = darkThemes.find { it.id == vm.darkTheme }?.name,
        onClick = {
          vm.dialog = {
            AlertDialog(
              onDismissRequest = { vm.dialog = null },
              title = { Text("Select a theme") },
              text = {
                LazyColumnFor(items = darkThemes) { theme ->
                  MaterialTheme(theme.colors) {
                    Surface(
                      modifier = Modifier.padding(8.dp).clickable(onClick = {
                        vm.darkTheme = theme.id
                        vm.dialog = null
                      }),
                      elevation = 4.dp
                    ) {
                      ThemeItem(theme)
                    }
                  }
                }
              },
              buttons = {}
            )
          }
        }
      )
    }
  }
  vm.dialog?.invoke()
}

@Composable
fun <T> ChoiceDialog(
  items: List<Pair<T, String>>,
  selected: T?,
  onDismissRequest: () -> Unit,
  onSelected: (T) -> Unit,
  title: (@Composable () -> Unit)? = null,
  buttons: @Composable () -> Unit = emptyContent()
) {
  AlertDialog(onDismissRequest = onDismissRequest, buttons = buttons, title = title, text = {
    LazyColumnFor(items = items) { (value, text) ->
      Row(
        modifier = Modifier.height(48.dp).fillMaxWidth().clickable(onClick = { onSelected(value) }),
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButton(
          selected = value == selected,
          onClick = { onSelected(value) },
        )
        Text(text = text, modifier = Modifier.padding(start = 24.dp))
      }
    }
  })
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
