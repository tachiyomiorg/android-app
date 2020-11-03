/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more.settings

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.StartScreen
import tachiyomi.ui.R
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
  uiPreferences: UiPreferences
) : BaseViewModel() {

  val startScreen = uiPreferences.startScreen().asState()
  val confirmExit = uiPreferences.confirmExit().asState()
  val hideBottomBarOnScroll = uiPreferences.hideBottomBarOnScroll().asState()
}

@Composable
fun SettingsGeneralScreen(navController: NavHostController) {
  val vm = viewModel<SettingsGeneralViewModel>()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.general_label)) },
      navigationIcon = { BackIconButton(navController) }
    )
    PreferencesScrollableColumn {
      ChoicePref(
        preference = vm.startScreen,
        title = R.string.start_screen,
        choices = mapOf(
          StartScreen.Library to R.string.library_label,
          StartScreen.Updates to R.string.updates_label,
          StartScreen.History to R.string.history_label,
          StartScreen.Browse to R.string.browse_label,
          StartScreen.More to R.string.more_label,
        )
      )
      SwitchPref(preference = vm.confirmExit, title = R.string.confirm_exit)
      SwitchPref(preference = vm.hideBottomBarOnScroll, title = R.string.hide_bottom_bar_on_scroll)
    }
  }
}
