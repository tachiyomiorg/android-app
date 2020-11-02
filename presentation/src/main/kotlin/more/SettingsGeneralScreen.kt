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
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.StartScreen
import tachiyomi.ui.R
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
  private val uiPreferences: UiPreferences
) : BaseViewModel() {

  var startScreen = uiPreferences.startScreen().asState()
}

@Composable
fun SettingsGeneralScreen(navController: NavHostController) {
  val vm = viewModel<SettingsGeneralViewModel>()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.general_label)) },
      navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.Default.ArrowBack)
        }
      }
    )
    PreferencesScrollableColumn {
      ChoicePref(
        preference = vm.startScreen,
        title = stringResource(R.string.start_screen),
        choices = mapOf(
          StartScreen.Library to stringResource(R.string.library_label),
          StartScreen.Updates to stringResource(R.string.updates_label),
          StartScreen.History to stringResource(R.string.history_label),
          StartScreen.Browse to stringResource(R.string.browse_label),
          StartScreen.More to stringResource(R.string.more_label),
        )
      )
    }
  }
}
