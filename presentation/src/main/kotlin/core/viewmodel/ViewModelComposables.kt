/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import tachiyomi.core.di.AppScope
import toothpick.Toothpick
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance

@Composable
inline fun <reified VM : BaseViewModel> viewModel(): VM {
  val viewModel = remember {
    AppScope.getInstance<VM>()
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.destroy()
    }
  }
  return viewModel
}

@Composable
inline fun <reified VM : BaseViewModel> viewModel(
  crossinline binding: @DisallowComposableCalls () -> Any,
): VM {
  val (viewModel, submodule) = remember {
    val submodule = module {
      binding().let { bind(it.javaClass).toInstance(it) }
    }
    val subscope = AppScope.subscope(submodule).also {
      it.installModules(submodule)
    }
    val viewModel = subscope.getInstance<VM>()
    Pair(viewModel, submodule)
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.destroy()
      Toothpick.closeScope(submodule)
    }
  }
  return viewModel
}
