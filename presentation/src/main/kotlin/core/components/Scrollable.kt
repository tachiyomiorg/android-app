/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollableColumn(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(
    modifier = modifier.scrollable(state = rememberScrollState(),
      orientation = Orientation.Vertical)
  ) {
    content()
  }
}

@Composable
fun ScrollableRow(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Row(
    modifier = modifier.scrollable(state = rememberScrollState(),
      orientation = Orientation.Horizontal)
  ) {
    content()
  }
}