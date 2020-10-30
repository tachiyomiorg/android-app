/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AmbientElevationOverlay
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchConstants
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import tachiyomi.ui.R
import tachiyomi.ui.core.components.NoElevationOverlay
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
      SettingsRow(
        title = "Appearance",
        icon = Icons.Default.Palette,
        onClick = {
          navController.navigate("themes")
        }
      )
    }
  }
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

@Composable
fun SettingsRow(
  title: String,
  icon: VectorAsset? = null,
  onClick: () -> Unit = {},
  subtitle: String? = null,
  action: @Composable (() -> Unit)? = null,
) {
  val height = if (subtitle != null) 72.dp else 56.dp

  Row(Modifier.fillMaxWidth().height(height).clickable(onClick = onClick)) {
    if (icon != null) {
      Image(
        asset = icon,
        modifier = Modifier.padding(horizontal = 16.dp).height(height).size(24.dp),
        colorFilter = ColorFilter.tint(MaterialTheme.colors.secondaryVariant)
      )
    }
    Column(Modifier.padding(horizontal = 16.dp).weight(1f).fillMaxHeight().wrapContentHeight()) {
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
      Box(Modifier.preferredWidthIn(min = 56.dp).height(height).wrapContentHeight()) {
        action()
      }
    }
  }
}
