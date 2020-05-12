/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.onDispose
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.ConfigurationAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.paint
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.currentTextStyle
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Rect
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.LinearGradient
import androidx.ui.graphics.Paint
import androidx.ui.graphics.painter.Painter
import androidx.ui.layout.Column
import androidx.ui.layout.Stack
import androidx.ui.layout.Table
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.wrapContentHeight
import androidx.ui.material.Surface
import androidx.ui.material.TopAppBar
import androidx.ui.res.stringResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.font
import androidx.ui.text.font.fontFamily
import androidx.ui.unit.Dp
import androidx.ui.unit.Px
import androidx.ui.unit.PxSize
import androidx.ui.unit.PxSize.Companion.UnspecifiedSize
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import androidx.ui.unit.toRect
import tachiyomi.core.di.AppScope
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.coil.CoilImage
import tachiyomi.ui.coil.MangaCover

val ptSansFont = fontFamily(font(R.font.ptsans_bold))

@Composable
fun LibraryScreen() {
  val presenter = remember { AppScope.getInstance<LibraryPresenter>() }
  onDispose {
    presenter.destroy()
  }
  val state = presenter.state()
  Column {
    TopAppBar(title = { Text(stringResource(R.string.label_library2)) })
    Box(Modifier.padding(2.dp)) {
      VerticalScroller {
        LibraryTable(state)
      }
    }
  }
}

@Composable
fun LibraryTable(state: State<LibraryState>) {
  val gradient = LinearGradient(
    0.75f to Color.Transparent,
    1.0f to Color(0xAA000000),
    startX = Px.Zero,
    startY = Px.Zero,
    endX = Px.Zero,
    endY = Px.Zero
  )
  val painter = GradientPainter(gradient)

  AutofitGrid(data = state.value.library, defaultColumnWidth = 160.dp) { manga ->
    LibraryTableGridItem(manga = manga, gradientPainter = painter)
  }
}

@Composable
fun LibraryTableGridItem(manga: LibraryManga, gradientPainter: GradientPainter) {
  val cover = MangaCover(manga.id, manga.sourceId, manga.cover, true)
  val fontStyle = currentTextStyle().merge(
    TextStyle(letterSpacing = 0.sp, fontFamily = ptSansFont, fontSize = 14.sp)
  )

  Surface(
    modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f).padding(4.dp),
    elevation = 4.dp,
    shape = RoundedCornerShape(4.dp)
  ) {
    Stack(modifier = Modifier.fillMaxSize()) {
      CoilImage(model = cover)
      Box(modifier = Modifier.fillMaxSize().paint(gradientPainter))
      Text(
        text = manga.title,
        color = Color.White,
        style = fontStyle,
        modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
          .gravity(Alignment.BottomStart)
          .padding(8.dp)
      )
    }
  }

}

@Composable
fun <T> AutofitGrid(
  columns: Int = 0,
  defaultColumnWidth: Dp = 100.dp,
  data: List<T>,
  children: @Composable() (T) -> Unit
) {
  val numColumns = if (columns == 0) {
    ConfigurationAmbient.current.screenWidthDp / defaultColumnWidth.value.toInt()
  } else {
    columns
  }
  Table(columns = numColumns) {
    for (i in data.indices step numColumns) {
      tableRow {
        for (j in 0 until numColumns) {
          if (i + j >= data.size) break

          val item = data[i + j]
          children(item)
        }
      }
    }
  }
}

data class GradientPainter(val gradient: LinearGradient) : Painter() {
  private val paint = Paint()
  private var currentBounds: PxSize? = null
  private var rect: Rect? = null

  override fun onDraw(canvas: Canvas, bounds: PxSize) {
    if (this.currentBounds != bounds) {
      gradient.copy(startY = Px.Zero, endY = bounds.height).applyTo(paint)
      currentBounds = bounds
      rect = bounds.toRect()
    }

    canvas.drawRect(rect!!, paint)
  }

  override val intrinsicSize: PxSize = UnspecifiedSize
}
