/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import tachiyomi.ui.R
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn

@Composable
fun SettingsSecurityScreen(navController: NavHostController) {
  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.security_label)) },
      navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.Default.ArrowBack)
        }
      }
    )
    PreferencesScrollableColumn {
    }
  }
}
