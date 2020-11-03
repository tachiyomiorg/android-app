/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.round

private val materialColors = listOf(
  Color(0xFFF44336), // RED 500
  Color(0xFFE91E63), // PINK 500
  Color(0xFFFF2C93), // LIGHT PINK 500
  Color(0xFF9C27B0), // PURPLE 500
  Color(0xFF673AB7), // DEEP PURPLE 500
  Color(0xFF3F51B5), // INDIGO 500
  Color(0xFF2196F3), // BLUE 500
  Color(0xFF03A9F4), // LIGHT BLUE 500
  Color(0xFF00BCD4), // CYAN 500
  Color(0xFF009688), // TEAL 500
  Color(0xFF4CAF50), // GREEN 500
  Color(0xFF8BC34A), // LIGHT GREEN 500
  Color(0xFFCDDC39), // LIME 500
  Color(0xFFFFEB3B), // YELLOW 500
  Color(0xFFFFC107), // AMBER 500
  Color(0xFFFF9800), // ORANGE 500
  Color(0xFF795548), // BROWN 500
  Color(0xFF607D8B), // BLUE GREY 500
  Color(0xFF9E9E9E), // GREY 500
)

@Composable
fun ColorPickerDialog(
  onDismissRequest: () -> Unit,
  onSelected: (Color) -> Unit,
  modifier: Modifier = Modifier,
  title: (@Composable () -> Unit)? = null,
  initialSelectedColor: Color? = null,
) {
  var selectedColor by remember { mutableStateOf(initialSelectedColor ?: materialColors.first()) }
  var selectedShade by remember { mutableStateOf<Color?>(null) }
  val shades = remember(selectedColor) {
    getColorShades(selectedColor)
  }

  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)
  AlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    title = title,
    text = {
      Column {
        AutofitGrid(data = materialColors, columns = 5) { color ->
          ColorPreview(
            color = color,
            borderColor = borderColor,
            isSelected = selectedShade == null && selectedColor == color,
            onClick = {
              selectedColor = color
              selectedShade = null
            }
          )
        }
        Spacer(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth().height(1.dp)
          .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f)))

        LazyRowFor(shades) { color ->
          ColorPreview(
            color = color,
            borderColor = borderColor,
            isSelected = selectedShade == color,
            onClick = { selectedShade = color }
          )
        }
      }
    },
    buttons = {
      Row(Modifier.fillMaxWidth().padding(8.dp), Arrangement.End) {
        TextButton(onClick = {
          onSelected(selectedShade ?: selectedColor)
        }) {
          Text("SELECT")
        }
      }
    }
  )
}

@Composable
private fun ColorPreview(
  color: Color,
  borderColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(alignment = Alignment.Center, modifier = Modifier
    .fillMaxWidth()
    .padding(4.dp)
    .size(48.dp)
    .clip(CircleShape)
    .background(color)
    .border(BorderStroke(1.dp, borderColor), CircleShape)
    .clickable(onClick = { onClick() })
  ) {
    if (isSelected) {
      Icon(
        asset = Icons.Default.Check.copy(defaultWidth = 32.dp, defaultHeight = 32.dp),
        tint = if (color.luminance() > 0.5) Color.Black else Color.White,
      )
    }
  }
}

private fun getColorShades(color: Color): List<Color> {
  val f = String.format("%06X", 0xFFFFFF and color.toArgb()).toLong(16)
  return listOf(
    shadeColor(f, 0.9), shadeColor(f, 0.7), shadeColor(f, 0.5),
    shadeColor(f, 0.333), shadeColor(f, 0.166), shadeColor(f, -0.125),
    shadeColor(f, -0.25), shadeColor(f, -0.375), shadeColor(f, -0.5),
    shadeColor(f, -0.675), shadeColor(f, -0.7), shadeColor(f, -0.775)
  )
}

private fun shadeColor(f: Long, percent: Double): Color {
  val t = if (percent < 0) 0.0 else 255.0
  val p = if (percent < 0) percent * -1 else percent
  val r = f shr 16
  val g = f shr 8 and 0x00FF
  val b = f and 0x0000FF

  val red = (round((t - r) * p) + r).toInt()
  val green = (round((t - g) * p) + g).toInt()
  val blue = (round((t - b) * p) + b).toInt()
  return Color(red = red, green = green, blue = blue, alpha = 255)
}
