/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tachiyomi.ui.core.theme.CustomColors

@Composable
fun Toolbar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  backgroundColor: Color = CustomColors.current.bars,
  contentColor: Color = CustomColors.current.onBars,
  elevation: Dp = 4.dp
) {
  CompositionLocalProvider(LocalElevationOverlay provides NoElevationOverlay) {
    TopAppBar(
      title = title,
      modifier = modifier,
      navigationIcon = navigationIcon,
      actions = actions,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      elevation = elevation
    )
  }
}
