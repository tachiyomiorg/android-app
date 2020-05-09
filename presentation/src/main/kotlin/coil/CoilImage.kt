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
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.toAndroidRect
import androidx.ui.foundation.Canvas
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.toRect
import coil.request.LoadRequest
import tachiyomi.core.log.Logger

@Composable
fun <T> CoilImage(
  model: T,
  contentScale: ContentScale = ContentScale.Inside,
  modifier: Modifier = Modifier.fillMaxSize()
) {
  var drawable by state<Drawable?> { null }
  val context = ContextAmbient.current
  onCommit(model) {
    val request = LoadRequest.Builder(context)
      .data(model)
      .listener(onError = { _, t -> Logger.warn(t) })
      .target(onSuccess = { drawable = it })
      .build()

    CoilLoader.execute(request)

    onDispose {
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
