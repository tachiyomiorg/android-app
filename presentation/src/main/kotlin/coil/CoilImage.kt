/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.setValue
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.drawscope.drawCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.platform.ContextAmbient
import coil.request.LoadRequest
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Scale
import tachiyomi.core.log.Log

@Composable
fun <T> CoilImage(
  model: T,
  scale: Scale = Scale.FILL,
  modifier: Modifier = Modifier.fillMaxSize()
) {
  WithConstraints {
    var drawable by state<Drawable?> { null }
    val context = ContextAmbient.current
    onCommit(model) {
      val width =
        if (constraints.maxWidth > 0 && constraints.maxWidth < Int.MAX_VALUE) {
          constraints.maxWidth
        } else {
          0
        }

      val height =
        if (constraints.maxHeight > 0 && constraints.maxHeight < Int.MAX_VALUE) {
          constraints.maxHeight
        } else {
          0
        }
      val size = if (width == 0 || height == 0) OriginalSize else PixelSize(width, height)

      val request = LoadRequest.Builder(context)
        .data(model)
        .size(size)
        .scale(scale)
        .listener(onError = { _, t -> Log.warn(t) })
        .target(onSuccess = { drawable = it })
        .build()

      val disposable = CoilLoader.execute(request)

      onDispose {
        disposable.dispose()
        drawable = null
      }
    }

    val theDrawable = drawable
    if (theDrawable != null) {
      Canvas(modifier = modifier) {
        theDrawable.bounds = size.toRect().toAndroidRect()
        drawCanvas { canvas, pxSize ->
          theDrawable.draw(canvas.nativeCanvas)
        }
      }
    }
  }
}
