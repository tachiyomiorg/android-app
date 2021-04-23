/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.ui.R
import tachiyomi.ui.core.prefs.asStateIn

@Composable
fun ConfirmExitBackHandler(uiPreferences: UiPreferences) {
  val scope = rememberCoroutineScope()
  val confirmExit by uiPreferences.confirmExit().asStateIn(scope)
  var isConfirmingExit by remember { mutableStateOf(false) }
  val context = LocalContext.current

  BackHandler(enabled = confirmExit && !isConfirmingExit) {
    isConfirmingExit = true
    Toast.makeText(context, R.string.confirm_exit_message, Toast.LENGTH_LONG).show()
    scope.launch {
      delay(2000)
      isConfirmingExit = false
    }
  }
}
