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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AmbientElevationOverlay
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import tachiyomi.ui.R
import tachiyomi.ui.Route
import tachiyomi.ui.core.components.NoElevationOverlay
import tachiyomi.ui.core.prefs.PreferenceRow
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class MoreViewModel @Inject constructor(
) : BaseViewModel() {
}

@Composable
fun MoreScreen(navController: NavController) {
  val vm = viewModel<MoreViewModel>()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.more_label)) },
      elevation = 0.dp,
      modifier = Modifier.zIndex(1f)
    )
    Providers(AmbientElevationOverlay provides NoElevationOverlay()) {
      Surface(
        color = MaterialTheme.colors.primarySurface,
        modifier = Modifier
          .fillMaxWidth()
          // To ensure that the elevation shadow is drawn behind the TopAppBar
          .zIndex(0f),
        elevation = 4.dp
      ) {
        Icon(vectorResource(R.drawable.ic_tachi), modifier = Modifier.padding(32.dp).size(56.dp))
      }
    }
    PreferencesScrollableColumn {
      PreferenceRow(
        title = stringResource(R.string.settings_label),
        icon = Icons.Default.Settings,
        onClick = { navController.navigate(Route.Settings.id) }
      )
      PreferenceRow(
        title = stringResource(R.string.about_label),
        icon = Icons.Default.Info,
        onClick = { /* TODO */ }
      )
      PreferenceRow(
        title = stringResource(R.string.help_label),
        icon = Icons.Default.Help,
        onClick = { /* TODO */ }
      )
    }
  }
}
