/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import tachiyomi.core.di.AppScope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance

@Composable
inline fun <reified VM : BaseViewModel> viewModel(): VM {
  val viewModel = remember {
    AppScope.getInstance<VM>()
  }
  onDispose {
    viewModel.destroy()
  }
  return viewModel
}

@Composable
inline fun <reified VM : BaseViewModel> viewModel(
  crossinline bindings: Module.() -> Unit,
): VM {
  val (viewModel, submodule) = remember {
    val submodule = module { bindings() }
    val subscope = AppScope.subscope(submodule).also {
      it.installModules(submodule)
    }
    val viewModel = subscope.getInstance<VM>()
    Pair(viewModel, submodule)
  }
  onDispose {
    viewModel.destroy()
    Toothpick.closeScope(submodule)
  }
  return viewModel
}
