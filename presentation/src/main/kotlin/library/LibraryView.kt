/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.Box
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.currentTextStyle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ConfigurationAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
      ScrollableColumn {
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
    startX = 0f,
    startY = 0f,
    endX = 0f,
    endY = 0f
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
  // TODO table was deleted on dev11
//  Table(columns = numColumns) {
//    for (i in data.indices step numColumns) {
//      tableRow {
//        for (j in 0 until numColumns) {
//          if (i + j >= data.size) break
//
//          val item = data[i + j]
//          children(item)
//        }
//      }
//    }
//  }
}

data class GradientPainter(val gradient: LinearGradient) : Painter() {
  private val paint = Paint()
  private var currentBounds: Size? = null
  private var rect: Rect? = null

  override fun DrawScope.onDraw() {
    drawCanvas { canvas, size ->
      if (currentBounds != size) {
        gradient.copy(startY = 0f, endY = size.height).applyTo(paint, 1f)
        currentBounds = size
        rect = size.toRect()
      }
      canvas.drawRect(rect!!, paint)
    }
  }

  override val intrinsicSize: Size
    get() = Size.Unspecified

}
