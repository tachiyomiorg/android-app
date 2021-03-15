/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.updates

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import tachiyomi.ui.R
import tachiyomi.ui.core.components.Toolbar

@Composable
fun UpdatesScreen(navController: NavController) {
  Scaffold(
    topBar = {
      Toolbar(title = { Text(stringResource(R.string.updates_label)) })
    }
  ) {
  }
}
