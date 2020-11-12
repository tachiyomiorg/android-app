/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import tachiyomi.domain.ui.UiPreferences
import tachiyomi.domain.ui.model.StartScreen
import tachiyomi.ui.R
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.prefs.Pref
import tachiyomi.ui.core.prefs.PreferencesScrollableColumn
import tachiyomi.ui.core.prefs.SwitchPref
import tachiyomi.ui.core.viewmodel.BaseViewModel
import tachiyomi.ui.core.viewmodel.viewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
  uiPreferences: UiPreferences
) : BaseViewModel() {

  val startScreen = uiPreferences.startScreen().asState()
  val confirmExit = uiPreferences.confirmExit().asState()
  val hideBottomBarOnScroll = uiPreferences.hideBottomBarOnScroll().asState()
  val language = uiPreferences.language().asState()
  val dateFormat = uiPreferences.dateFormat().asState()
}

@Composable
fun SettingsGeneralScreen(navController: NavHostController) {
  val vm = viewModel<SettingsGeneralViewModel>()
  val context = ContextAmbient.current
  Column {
    Toolbar(
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
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Pref(title = R.string.manage_notifications, onClick = {
          val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
          }
          context.startActivity(intent)
        })
      }
      Divider()
      val currentLocaleDisplayName = Locale.getDefault().let { it.getDisplayName(it).capitalize() }
      ChoicePref(
        preference = vm.language,
        title = stringResource(R.string.language),
        choices = mapOf(
          "" to "${stringResource(R.string.system_default)} ($currentLocaleDisplayName)"
        ),
      )
      val now = remember { Date() }
      ChoicePref(
        preference = vm.dateFormat,
        title = stringResource(R.string.date_format),
        choices = mapOf(
          "" to stringResource(R.string.system_default),
          "MM/dd/yy" to "MM/dd/yy",
          "dd/MM/yy" to "dd/MM/yy",
          "yyyy-MM-dd" to "yyyy-MM-dd"
        ).mapValues { "${it.value} (${getFormattedDate(it.key, now)})" }
      )
    }
  }
}

private fun getFormattedDate(prefValue: String, date: Date): String {
  return when (prefValue) {
    "" -> DateFormat.getDateInstance(DateFormat.SHORT)
    else -> SimpleDateFormat(prefValue, Locale.getDefault())
  }.format(date.time)
}
