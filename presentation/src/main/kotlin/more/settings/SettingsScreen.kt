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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import tachiyomi.ui.R
import tachiyomi.ui.Route
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.prefs.PreferenceRow
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn

@Composable
fun SettingsScreen(navController: NavHostController) {
  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.settings_label)) },
      navigationIcon = { BackIconButton(navController) }
    )
    PreferencesScrollableColumn {
      PreferenceRow(
        title = R.string.general_label,
        icon = Icons.Default.Tune,
        onClick = { navController.navigate(Route.SettingsGeneral.id) }
      )
      PreferenceRow(
        title = R.string.appearance_label,
        icon = Icons.Default.Palette,
        onClick = { navController.navigate(Route.SettingsAppearance.id) }
      )
      PreferenceRow(
        title = R.string.library_label,
        icon = Icons.Default.CollectionsBookmark,
        onClick = { navController.navigate(Route.SettingsLibrary.id) }
      )
      PreferenceRow(
        title = R.string.reader_label,
        icon = Icons.Default.ChromeReaderMode,
        onClick = { navController.navigate(Route.SettingsReader.id) }
      )
      PreferenceRow(
        title = R.string.downloads_label,
        icon = Icons.Default.GetApp,
        onClick = { navController.navigate(Route.SettingsDownloads.id) }
      )
      PreferenceRow(
        title = R.string.tracking_label,
        icon = Icons.Default.Sync,
        onClick = { navController.navigate(Route.SettingsTracking.id) }
      )
      PreferenceRow(
        title = R.string.browse_label,
        icon = Icons.Default.Explore,
        onClick = { navController.navigate(Route.SettingsBrowse.id) }
      )
      PreferenceRow(
        title = R.string.backup_label,
        icon = Icons.Default.Backup,
        onClick = { navController.navigate(Route.SettingsBackup.id) }
      )
      PreferenceRow(
        title = R.string.security_label,
        icon = Icons.Default.Security,
        onClick = { navController.navigate(Route.SettingsSecurity.id) }
      )
      PreferenceRow(
        title = R.string.parental_controls_label,
        icon = Icons.Default.PeopleOutline,
        onClick = { navController.navigate(Route.SettingsParentalControls.id) }
      )
      PreferenceRow(
        title = R.string.advanced_label,
        icon = Icons.Default.Code,
        onClick = { navController.navigate(Route.SettingsAdvanced.id) }
      )
    }
  }
}
