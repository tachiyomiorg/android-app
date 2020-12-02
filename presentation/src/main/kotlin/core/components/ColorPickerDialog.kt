/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
import androidx.compose.ui.drawWithCache
import androidx.compose.ui.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.useOrElse
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.round

private val presetColors = listOf(
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
  initialColor: Color = Color.Unspecified,
) {
  var currentColor by mutableStateOf(initialColor)
  var showPresets by mutableStateOf(true)

  AlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    title = title,
    text = {
      if (showPresets) {
        ColorPresets(
          initialColor = currentColor,
          onColorChanged = { currentColor = it }
        )
      } else {
        ColorPalette(
          initialColor = currentColor,
          onColorChanged = { currentColor = it }
        )
      }
    },
    buttons = {
      Row(Modifier.fillMaxWidth().padding(8.dp)) {
        TextButton(onClick = {
          showPresets = !showPresets
        }) {
          Text(if (showPresets) "Custom" else "Presets")
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = {
          onSelected(currentColor)
        }) {
          Text("Select")
        }
      }
    }
  )
}

@Composable
private fun ColorPresets(
  initialColor: Color,
  onColorChanged: (Color) -> Unit
) {
  val presets = remember {
    if (initialColor.isSpecified) {
      (listOf(initialColor) + presetColors).distinct()
    } else {
      presetColors
    }
  }

  var selectedColor by remember { mutableStateOf(initialColor.useOrElse { presets.first() }) }
  var selectedShade by remember { mutableStateOf<Color?>(null) }

  val shades = remember(selectedColor) { getColorShades(selectedColor) }

  val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)

  Column {
    AutofitGrid(data = presets, columns = 5) { color ->
      ColorPresetItem(
        color = color,
        borderColor = borderColor,
        isSelected = selectedShade == null && initialColor == color,
        onClick = {
          selectedShade = null
          selectedColor = color
          onColorChanged(color)
        }
      )
    }
    Spacer(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth().height(1.dp)
      .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f)))

    LazyRowFor(shades) { color ->
      ColorPresetItem(
        color = color,
        borderColor = borderColor,
        isSelected = selectedShade == color,
        onClick = {
          selectedShade = color
          onColorChanged(color)
        }
      )
    }
  }
}

@Composable
private fun ColorPresetItem(
  color: Color,
  borderColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(contentAlignment = Alignment.Center, modifier = Modifier
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
        imageVector = Icons.Default.Check.copy(defaultWidth = 32.dp, defaultHeight = 32.dp),
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

@OptIn(ExperimentalLayout::class)
@Composable
fun ColorPalette(
  initialColor: Color = Color.White,
  onColorChanged: (Color) -> Unit = {}
) {
  var selectedColor by remember { mutableStateOf(initialColor) }
  var textFieldHex by remember { mutableStateOf(initialColor.toHexString()) }

  var hue by remember { mutableStateOf(initialColor.toHsv()[0]) }
  var hueCursor by remember { mutableStateOf(0f) }

  var matrixSize by remember { mutableStateOf(IntSize(0, 0)) }
  var matrixCursor by remember { mutableStateOf(Offset(0f, 0f)) }

  val saturationGradient = remember(hue, matrixSize) {
    LinearGradient(
      colors = listOf(Color.White, hueToColor(hue)),
      startX = 0f, startY = 0f, endX = matrixSize.width.toFloat(), endY = 0f
    )
  }
  val valueGradient = remember(matrixSize) {
    LinearGradient(
      colors = listOf(Color.White, Color.Black),
      startX = 0f, startY = 0f, endX = 0f, endY = matrixSize.height.toFloat())
  }

  val cursorColor = MaterialTheme.colors.onBackground
  val cursorStroke = Stroke(4f)
  val borderStroke = Stroke(1f)

  fun setSelectedColor(color: Color, invalidate: Boolean = false) {
    selectedColor = color
    textFieldHex = color.toHexString()
    if (invalidate) {
      val hsv = color.toHsv()
      hue = hsv[0]
      matrixCursor = satValToCoordinates(hsv[1], hsv[2], matrixSize)
      hueCursor = hueToCoordinate(hsv[0], matrixSize)
    }
    onColorChanged(color)
  }

  Column {
    Row(Modifier.preferredHeight(IntrinsicSize.Max)) {
      Box(Modifier
        .aspectRatio(1f)
        .weight(1f)
        .onSizeChanged {
          matrixSize = it
          val hsv = selectedColor.toHsv()
          matrixCursor = satValToCoordinates(hsv[1], hsv[2], it)
          hueCursor = hueToCoordinate(hue, it)
        }
        .drawWithContent {
          drawRect(brush = valueGradient)
          drawRect(brush = saturationGradient, blendMode = BlendMode.Multiply)
          drawRect(Color.LightGray, size = size, style = borderStroke)
          drawCircle(Color.Black, radius = 8f, center = matrixCursor, style = cursorStroke)
          drawCircle(Color.LightGray, radius = 12f, center = matrixCursor, style = cursorStroke)
        }
        .pointerInteropFilter { ev ->
          if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            val safeX = ev.x.coerceIn(0f, matrixSize.width.toFloat())
            val safeY = ev.y.coerceIn(0f, matrixSize.height.toFloat())
            matrixCursor = Offset(safeX, safeY)
            val newColor = matrixCoordinatesToColor(hue, safeX, safeY, matrixSize)
            setSelectedColor(newColor)
          }
          true
        })

      Box(Modifier
        .fillMaxHeight()
        .width(48.dp)
        .padding(start = 8.dp)
        .drawWithCache {
          var h = 360f
          val colors = MutableList(size.height.toInt()) {
            hueToColor(h).also {
              h -= 360f / size.height
            }
          }
          val cursorSize = Size(size.width, 10f)
          val cursorTopLeft = Offset(0f, hueCursor - (cursorSize.height / 2))
          onDrawBehind {
            colors.forEachIndexed { i, color ->
              val pos = i.toFloat()
              drawLine(color, Offset(0f, pos), Offset(size.width, pos))
            }
            drawRect(Color.LightGray, size = size, style = borderStroke)
            drawRect(cursorColor, topLeft = cursorTopLeft, size = cursorSize, style = cursorStroke)
          }
        }
        .pointerInteropFilter { ev ->
          if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            val safeY = ev.y.coerceIn(0f, matrixSize.height.toFloat())
            hueCursor = safeY
            hue = hueCoordinatesToHue(safeY, matrixSize)
            val newColor = matrixCoordinatesToColor(hue, matrixCursor.x, matrixCursor.y, matrixSize)
            setSelectedColor(newColor)
          }
          true
        })
    }

    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
      Box(Modifier.size(72.dp, 48.dp).background(selectedColor)
        .border(1.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.54f)))
      Spacer(Modifier.width(32.dp))
      OutlinedTextField(
        value = textFieldHex,
        onValueChange = {
          val newColor = hexStringToColor(it)
          if (newColor != null) {
            setSelectedColor(newColor, invalidate = true)
          } else {
            textFieldHex = it
          }
        }
      )
    }
  }
}

// Coordinates <-> Color

private fun matrixCoordinatesToColor(hue: Float, x: Float, y: Float, size: IntSize): Color {
  val saturation = 1f / size.width * x
  val value = 1f - (1f / size.height * y)
  return hsvToColor(hue, saturation, value)
}

private fun hueCoordinatesToHue(y: Float, size: IntSize): Float {
  val hue = 360f - y * 360f / size.height
  return hsvToColor(hue, 1f, 1f).toHsv()[0]
}

private fun satValToCoordinates(saturation: Float, value: Float, size: IntSize): Offset {
  return Offset(saturation * size.width, ((1f - value) * size.height))
}

private fun hueToCoordinate(hue: Float, size: IntSize): Float {
  return size.height - (hue * size.height / 360f)
}

// Color space conversions

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
  val f = floatArrayOf(hue, saturation, value)
  return Color(android.graphics.Color.HSVToColor(f))
}

private fun Color.toHsv(): FloatArray {
  val result = floatArrayOf(0f, 0f, 0f)
  android.graphics.Color.colorToHSV(toArgb(), result)
  return result
}

private fun hueToColor(hue: Float): Color {
  return hsvToColor(hue, 1f, 1f)
}

private fun Color.toHexString(): String {
  return String.format("#%06X", (0xFFFFFF and toArgb()))
}

private fun hexStringToColor(hex: String): Color? {
  return try {
    Color(android.graphics.Color.parseColor(hex))
  } catch (e: Exception) {
    null
  }
}
