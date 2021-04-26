/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.ui.R
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.prefs.SwitchPreference
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import javax.inject.Inject

class SettingsLibraryViewModel @Inject constructor(
  libraryPreferences: LibraryPreferences
) : BaseViewModel() {

  val showAllCategory = libraryPreferences.showAllCategory().asState()
}

@Composable
fun SettingsLibraryScreen(navController: NavHostController) {
  val vm = viewModel<SettingsLibraryViewModel>()

  Column {
    Toolbar(
      title = { Text(stringResource(R.string.library_label)) },
      navigationIcon = { BackIconButton(navController) }
    )
    LazyColumn {
      item {
        SwitchPreference(preference = vm.showAllCategory, title = "Show all category")
      }
    }
  }
}
