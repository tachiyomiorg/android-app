/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.onCommit
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Canvas
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Image
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun <T> GlideImage(model: T, contentScale: ContentScale = ContentScale.Inside) {
  var image by state<ImageAsset?> { null }
  var drawable by state<Drawable?> { null }
  val context = ContextAmbient.current
  onCommit(model) {
    val glide = Glide.with(context)
    val target = object : CustomTarget<Bitmap>() {
      override fun onLoadCleared(placeholder: Drawable?) {
        image = null
        drawable = placeholder
      }

      override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
        image = bitmap.asImageAsset()
      }
    }
    glide
      .asBitmap()
      .load(model)
      .into(target)

    onDispose {
      image = null
      drawable = null
      glide.clear(target)
    }
  }

  val theImage = image
  val theDrawable = drawable
  if (theImage != null) {
    // Box is a predefined convenience composable that allows you to apply common draw & layout
    // logic. In addition we also pass a few modifiers to it.

    // You can think of Modifiers as implementations of the decorators pattern that are
    // used to modify the composable that its applied to. In this example, we configure the
    // Box composable to have a max height of 200dp and fill out the entire available
    // width.
    Box(modifier = Modifier.fillMaxSize(), gravity = ContentGravity.Center) {
      // Image is a pre-defined composable that lays out and draws a given [ImageAsset].
      Image(
        asset = theImage,
        modifier = Modifier.fillMaxSize(),
        contentScale = contentScale
      )
    }
  } else if (theDrawable != null) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      theDrawable.draw(this.nativeCanvas)
    }
  }
}

@Composable
fun <T> GlideImageDrawable(model: T, width: Dp = 56.dp, height: Dp = 56.dp) {
  var drawable by state<Drawable?> { null }
  val context = ContextAmbient.current
  onCommit(model) {
    val glide = Glide.with(context)
    val target = object : CustomTarget<Drawable>(width.value.toInt(), height.value.toInt()) {
      override fun onLoadCleared(placeholder: Drawable?) {
        drawable = placeholder
      }

      override fun onResourceReady(d: Drawable, transition: Transition<in Drawable>?) {
        drawable = d
      }
    }
    glide
      .asDrawable()
      .load(model)
      .into(target)

    onDispose {
      drawable = null
      glide.clear(target)
    }
  }

  val theDrawable = drawable

  if (theDrawable != null) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      theDrawable.draw(this.nativeCanvas)
    }
  }
}
