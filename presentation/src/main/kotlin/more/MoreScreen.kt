/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AmbientElevationOverlay
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
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
import tachiyomi.ui.core.components.NoElevationOverlay
import tachiyomi.ui.core.prefs.PreferenceRow
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class MoreViewModel @Inject constructor(
) : BaseViewModel() {

}

@Composable
fun MoreScreen(navController: NavController) {
  val vm = viewModel<MoreViewModel>()

  val scroll = rememberScrollState()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.label_more)) },
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
    ScrollableColumn(scrollState = scroll, modifier = Modifier.fillMaxSize()) {
      PreferenceRow(
        title = "Appearance",
        icon = Icons.Default.Palette,
        onClick = {
          navController.navigate("themes")
        }
      )
    }
  }
}

