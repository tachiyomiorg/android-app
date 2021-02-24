/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components.manga

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.util.Typefaces

// TODO remove this file. Use specific ones

@Composable
fun MangaGridItem(
  title: String,
  cover: MangaCover,
  onClick: () -> Unit = {},
) {
  val fontStyle = LocalTextStyle.current.merge(
    TextStyle(letterSpacing = 0.sp, fontFamily = Typefaces.ptSansFont, fontSize = 14.sp)
  )

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(3f / 4f)
      .padding(4.dp)
      .clickable(onClick = onClick),
    elevation = 4.dp,
    shape = RoundedCornerShape(4.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      CoilImage(model = cover)
      Box(modifier = Modifier.fillMaxSize().then(shadowGradient))
      Text(
        text = title,
        color = Color.White,
        style = fontStyle,
        modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
          .align(Alignment.BottomStart)
          .padding(8.dp)
      )
    }
  }
}

private val shadowGradient = Modifier.drawWithCache {
  val gradient = Brush.linearGradient(
    0.75f to Color.Transparent,
    1.0f to Color(0xAA000000),
    start = Offset(0f, 0f),
    end = Offset(0f, size.height)
  )
  onDrawBehind {
    drawRect(gradient)
  }
}
