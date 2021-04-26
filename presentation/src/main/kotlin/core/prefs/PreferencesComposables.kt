/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.prefs

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Ellipsis
import androidx.compose.ui.unit.dp
import tachiyomi.ui.core.components.ColorPickerDialog

@Composable
fun PreferenceRow(
  title: String,
  icon: ImageVector? = null,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  subtitle: String? = null,
  action: @Composable (() -> Unit)? = null,
) {
  val height = if (subtitle != null) 72.dp else 56.dp

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .requiredHeight(height)
      .combinedClickable(
        onLongClick = onLongClick,
        onClick = onClick
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .size(24.dp),
        tint = MaterialTheme.colors.primary,
        contentDescription = null
      )
    }
    Column(
      Modifier
        .padding(horizontal = 16.dp)
        .weight(1f)
    ) {
      Text(
        text = title,
        overflow = Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.subtitle1,
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          overflow = Ellipsis,
          maxLines = 1,
          color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
          style = MaterialTheme.typography.subtitle1
        )
      }
    }
    if (action != null) {
      Box(Modifier.widthIn(min = 56.dp)) {
        action()
      }
    }
  }
}

@Composable
fun PreferenceRow(
  @StringRes title: Int,
  icon: ImageVector? = null,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  subtitle: String? = null,
  action: @Composable (() -> Unit)? = null,
) {
  PreferenceRow(stringResource(title), icon, onClick, onLongClick, subtitle, action)
}

@Composable
fun SwitchPreference(
  preference: PreferenceMutableState<Boolean>,
  title: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
) {
  PreferenceRow(
    title = title,
    subtitle = subtitle,
    icon = icon,
    action = { Switch(checked = preference.value, onCheckedChange = null) },
    onClick = { preference.value = !preference.value }
  )
}

@Composable
fun SwitchPreference(
  preference: PreferenceMutableState<Boolean>,
  @StringRes title: Int,
  subtitle: Int? = null,
  icon: ImageVector? = null,
) {
  SwitchPreference(preference, stringResource(title), subtitle?.let { stringResource(it) }, icon)
}

@Composable
fun <Key> ChoicePreference(
  preference: PreferenceMutableState<Key>,
  choices: Map<Key, Int>,
  @StringRes title: Int,
  subtitle: String? = null
) {
  ChoicePreference(
    preference, choices.mapValues { stringResource(it.value) }, stringResource(title), subtitle
  )
}

@Composable
fun <Key> ChoicePreference(
  preference: PreferenceMutableState<Key>,
  choices: Map<Key, String>,
  title: String,
  subtitle: String? = null
) {
  var showDialog by remember { mutableStateOf(false) }

  PreferenceRow(
    title = title,
    subtitle = if (subtitle == null) choices[preference.value] else null,
    onClick = { showDialog = true }
  )

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      buttons = {},
      title = { Text(title) },
      text = {
        LazyColumn {
          items(choices.toList()) { (value, text) ->
            Row(
              modifier = Modifier
                .requiredHeight(48.dp)
                .fillMaxWidth()
                .clickable(onClick = {
                  preference.value = value
                  showDialog = false
                }),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = value == preference.value,
                onClick = null,
              )
              Text(text = text, modifier = Modifier.padding(start = 24.dp))
            }
          }
        }
      }
    )
  }
}

@Composable
fun ColorPreference(
  preference: PreferenceMutableState<Color>,
  title: String,
  subtitle: String? = null,
  unsetColor: Color = Color.Unspecified
) {
  var showDialog by remember { mutableStateOf(false) }
  val initialColor = preference.value.takeOrElse { unsetColor }

  PreferenceRow(
    title = title,
    subtitle = subtitle,
    onClick = { showDialog = true },
    onLongClick = { preference.value = Color.Unspecified },
    action = {
      if (preference.value != Color.Unspecified || unsetColor != Color.Unspecified) {
        val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)
        Box(
          modifier = Modifier
            .padding(4.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(color = initialColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
        )
      }
    }
  )

  if (showDialog) {
    ColorPickerDialog(
      title = { Text(title) },
      onDismissRequest = { showDialog = false },
      onSelected = {
        preference.value = it
        showDialog = false
      },
      initialColor = initialColor
    )
  }
}
