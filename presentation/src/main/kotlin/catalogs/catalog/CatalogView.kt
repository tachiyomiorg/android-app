/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import tachiyomi.core.di.AppScope

@Composable
fun CatalogScreen(navController: NavController, pkgName: String) {
  val presenter = remember { AppScope.getInstance<CatalogPresenter>() }
  onDispose { presenter.destroy() }

  val state = presenter.state()
  val currState = state.value

  presenter.setCatalog(pkgName)

  Column {
    TopAppBar(title = { Text(currState.catalog?.name ?: "?") })
  }
}