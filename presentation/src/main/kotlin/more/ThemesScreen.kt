/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more

import androidx.compose.foundation.AmbientIndication
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSizeConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.ui.core.components.AutofitGrid
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

  var theme by uiPreferences.theme().asState()
}

@Composable
fun ThemesScreen(navController: NavHostController) {
  val vm = viewModel<ThemesViewModel>()

  Column {
    TopAppBar(
      title = { Text("Theme picker") },
      navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.Default.ArrowBack)
        }
      },
    )
    AutofitGrid(data = themes, defaultColumnWidth = 160.dp) { theme ->
      MaterialTheme(theme.colors) {
        Column(Modifier.padding(4.dp)
          .weight(0.5f)
          .clickable(onClick = { vm.theme = theme.id })
        ) {
          ThemeItem(theme)
        }
      }
    }
  }
}

@Composable
private fun ThemeItem(theme: Theme) {
  TopAppBar(
    title = { Text(theme.name) },
    navigationIcon = {
      IconButton(onClick = {}, enabled = false) {
        Icon(Icons.Default.Menu)
      }
    }
  )
  Surface(modifier = Modifier.fillMaxWidth().height(124.dp)) {
    Box(Modifier.padding(8.dp)) {
      Text(text = "Text preview")
      StatelessFloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd)) {
        Icon(Icons.Default.Edit)
      }
    }
  }
}

@Composable
private fun StatelessFloatingActionButton(
  modifier: Modifier = Modifier,
  interactionState: InteractionState = remember { InteractionState() },
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
      Box(
        modifier = Modifier
          .defaultMinSizeConstraints(minWidth = FabSize, minHeight = FabSize)
          .indication(interactionState, AmbientIndication.current()),
        alignment = Alignment.Center
      ) { icon() }
    }
  }
}

private val FabSize = 56.dp
