/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import tachiyomi.core.di.AppScope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.ktp.extension.getInstance

@Composable
inline fun <reified P : BasePresenter> presenter(
  submodule: Module? = null,
): P {
  val subscope = remember {
    if (submodule != null) {
      AppScope.subscope(submodule).also {
        it.installModules(submodule)
      }
    } else {
      null
    }
  }
  val presenter = remember {
    val instanceScope = subscope ?: AppScope
    instanceScope.getInstance<P>()
  }
  onDispose {
    presenter.destroy()
    subscope?.let { Toothpick.closeScope(submodule) }
  }
  return presenter
}
