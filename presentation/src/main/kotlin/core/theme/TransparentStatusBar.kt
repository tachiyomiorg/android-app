/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * TODO: not sure if this is the best approach. Maybe we should try to expose the AppTheme viewmodel
 * instead and call a method there
 */
@Composable
fun TransparentStatusBar(content: @Composable () -> Unit) {
  val state = LocalTransparentStatusBar.current
  DisposableEffect(Unit) {
    state.enabled = true
    onDispose {
      state.enabled = false
    }
  }
  content()
}

val LocalTransparentStatusBar = staticCompositionLocalOf { TransparentStatusBar(false) }

class TransparentStatusBar(enabled: Boolean) {
  var enabled by mutableStateOf(enabled)
}
