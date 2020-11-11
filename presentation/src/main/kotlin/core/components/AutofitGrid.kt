/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ConfigurationAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview

@Composable
fun <T> AutofitGrid(
  columns: Int = 0,
  defaultColumnWidth: Dp = 100.dp,
  data: List<T>,
  modifier: Modifier = Modifier,
  child: @Composable (T) -> Unit
) {
  val numColumns = if (columns == 0) {
    ConfigurationAmbient.current.screenWidthDp / defaultColumnWidth.value.toInt()
  } else {
    columns
  }

  ScrollableColumn(modifier) {
    data.forEachIndexed { index, _ ->
      Row(Modifier.fillMaxWidth()) {
        for (cell in 0 until numColumns) {
          val i = (index * numColumns) + cell
          Box(modifier = Modifier.weight(1f)) {
            if (i < data.size) {
              child(data[i])
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun AutofitGridPreview() {
  AutofitGrid(columns = 3, data = listOf("a", "b", "c")) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(it)
    }
  }
}
