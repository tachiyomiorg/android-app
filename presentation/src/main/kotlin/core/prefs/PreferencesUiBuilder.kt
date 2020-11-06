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
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.ui.core.components.ColorPickerDialog

private typealias DialogComposable = @Composable () -> Unit

class PreferenceScope(dialog: MutableState<DialogComposable?>) {
  var dialog by dialog

  @Composable
  fun SwitchPref(
    preference: PreferenceMutableState<Boolean>,
    title: String,
    subtitle: String? = null,
  ) {
    PreferenceRow(
      title = title,
      subtitle = subtitle,
      action = { ReadOnlySwitch(checked = preference.value) },
      onClick = { preference.value = !preference.value }
    )
  }

  @Composable
  fun SwitchPref(
    preference: PreferenceMutableState<Boolean>,
    @StringRes title: Int,
    subtitle: String? = null,
  ) {
    SwitchPref(preference, stringResource(title), subtitle)
  }

  @Composable
  fun <Key> ChoicePref(
    preference: PreferenceMutableState<Key>,
    choices: Map<Key, String>,
    title: String,
    subtitle: String? = null
  ) {
    PreferenceRow(
      title = title,
      subtitle = if (subtitle == null) choices[preference.value] else null,
      onClick = {
        dialog = {
          ChoiceDialog(
            items = choices.toList(),
            selected = preference.value,
            title = { Text(title) },
            onDismissRequest = { dialog = null },
            onSelected = { selected ->
              preference.value = selected
              dialog = null
            }
          )
        }
      }
    )
  }

  @Composable
  fun <Key> ChoicePref(
    preference: PreferenceMutableState<Key>,
    choices: Map<Key, Int>,
    @StringRes title: Int,
    subtitle: String? = null
  ) {
    ChoicePref(preference, choices.mapValues { stringResource(it.value) }, stringResource(title),
      subtitle)
  }

  @Composable
  fun ColorPref(
    preference: PreferenceMutableState<Color>,
    title: String,
    subtitle: String? = null
  ) {
    PreferenceRow(
      title = title,
      subtitle = subtitle,
      onClick = {
        dialog = {
          ColorPickerDialog(
            title = { Text(title) },
            onDismissRequest = { dialog = null },
            onSelected = {
              preference.value = it
              dialog = null
            },
            initialSelectedColor = preference.value
          )
        }
      },
      onLongClick = { preference.value = Color.Unspecified },
      action = {
        if (preference.value != Color.Unspecified) {
          val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)
          Box(modifier = Modifier
            .padding(4.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(preference.value)
            .border(BorderStroke(1.dp, borderColor), CircleShape))
        }
      }
    )
  }

}

@Composable
fun PreferencesScrollableColumn(
  modifier: Modifier = Modifier,
  children: @Composable PreferenceScope.() -> Unit
) {
  val dialog = remember { mutableStateOf<DialogComposable?>(null) }
  Box {
    ScrollableColumn(modifier) {
      val scope = PreferenceScope(dialog)
      scope.children()
    }
    dialog.value?.invoke()
  }
}

@Composable
fun PreferenceRow(
  title: String,
  icon: VectorAsset? = null,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  subtitle: String? = null,
  action: @Composable (() -> Unit)? = null,
) {
  val height = if (subtitle != null) 72.dp else 56.dp

  Row(
    modifier = Modifier.fillMaxWidth().height(height).clickable(onClick = onClick)
      .longPressGestureFilter { onLongClick() },
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (icon != null) {
      Icon(
        asset = icon,
        modifier = Modifier.padding(horizontal = 16.dp).size(24.dp),
        tint = MaterialTheme.colors.secondaryVariant
      )
    }
    Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
      Text(
        text = title,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.subtitle1,
      )
      if (subtitle != null) {
        ProvideEmphasis(AmbientEmphasisLevels.current.medium) {
          Text(
            text = subtitle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.subtitle1,
          )
        }
      }
    }
    if (action != null) {
      Box(Modifier.preferredWidthIn(min = 56.dp)) {
        action()
      }
    }
  }
}

@Composable
fun PreferenceRow(
  @StringRes title: Int,
  icon: VectorAsset? = null,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  subtitle: String? = null,
  action: @Composable (() -> Unit)? = null,
) {
  PreferenceRow(stringResource(title), icon, onClick, onLongClick, subtitle, action)
}

@Composable
fun <T> ChoiceDialog(
  items: List<Pair<T, String>>,
  selected: T?,
  onDismissRequest: () -> Unit,
  onSelected: (T) -> Unit,
  title: (@Composable () -> Unit)? = null,
  buttons: @Composable () -> Unit = emptyContent()
) {
  AlertDialog(onDismissRequest = onDismissRequest, buttons = buttons, title = title, text = {
    LazyColumnFor(items = items) { (value, text) ->
      Row(
        modifier = Modifier.height(48.dp).fillMaxWidth().clickable(onClick = { onSelected(value) }),
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButton(
          selected = value == selected,
          onClick = { onSelected(value) },
        )
        Text(text = text, modifier = Modifier.padding(start = 24.dp))
      }
    }
  })
}

@Composable
fun ReadOnlySwitch(checked: Boolean) {
  val colors = SwitchConstants.defaultColors(
    disabledCheckedThumbColor = MaterialTheme.colors.secondaryVariant,
    disabledCheckedTrackColor = MaterialTheme.colors.secondaryVariant,
    disabledUncheckedThumbColor = MaterialTheme.colors.surface,
    disabledUncheckedTrackColor = MaterialTheme.colors.onSurface
  )
  Switch(checked, onCheckedChange = {}, enabled = false, colors = colors)
}
