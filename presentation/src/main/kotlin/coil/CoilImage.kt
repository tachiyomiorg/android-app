/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import android.graphics.drawable.Drawable
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.onCommit
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.core.toAndroidRect
import androidx.ui.foundation.Canvas
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.IntPx
import androidx.ui.unit.toRect
import coil.request.LoadRequest
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Scale
import tachiyomi.core.log.Logger

@Composable
fun <T> CoilImage(
  model: T,
  scale: Scale = Scale.FILL,
  modifier: Modifier = Modifier.fillMaxSize()
) {
  WithConstraints { constraints, _ ->
    var drawable by state<Drawable?> { null }
    val context = ContextAmbient.current
    onCommit(model) {
      val width =
        if (constraints.maxWidth > IntPx.Zero && constraints.maxWidth < IntPx.Infinity) {
          constraints.maxWidth.value
        } else {
          0
        }

      val height =
        if (constraints.maxHeight > IntPx.Zero && constraints.maxHeight < IntPx.Infinity) {
          constraints.maxHeight.value
        } else {
          0
        }
      val size = if (width == 0 || height == 0) OriginalSize else PixelSize(width, height)

      val request = LoadRequest.Builder(context)
        .data(model)
        .size(size)
        .scale(scale)
        .listener(onError = { _, t -> Logger.warn(t) })
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
        theDrawable.draw(nativeCanvas)
      }
    }
  }
}
