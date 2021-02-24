/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.coil

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Scale
import coil.size.Scale.FILL
import tachiyomi.core.log.Log

@Composable
fun <T> CoilImage(
  model: T,
  @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.fillMaxSize(),
  scale: Scale = FILL
) {
  BoxWithConstraints {
    val drawable: MutableState<Drawable?> = remember { mutableStateOf(null) }
    val context = LocalContext.current
    DisposableEffect(model) {
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

      val request = ImageRequest.Builder(context)
        .data(model)
        .size(size)
        .scale(scale)
        .listener(onError = { _, t -> Log.warn(t) })
        .target(onSuccess = { drawable.value = it })
        .build()

      val disposable = CoilLoader.enqueue(request)

      onDispose {
        disposable.dispose()
        drawable.value = null
      }
    }

    Canvas(modifier = modifier) {
      val value = drawable.value
      if (value != null) {
        value.bounds = size.toRect().toAndroidRect()
        drawIntoCanvas {
          value.draw(it.nativeCanvas)
        }
      }
    }
  }
}
