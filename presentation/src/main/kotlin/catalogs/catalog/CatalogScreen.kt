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
import androidx.navigation.NavController
import tachiyomi.core.di.bindInstance
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CatalogScreen(navController: NavController, sourceId: Long) {
  val vm = viewModel<CatalogViewModel> {
    bindInstance(CatalogViewModel.Params(sourceId))
  }

  Column {
    val catalog = vm.catalog
    if (catalog == null) {
      // TODO empty screen
      TopAppBar(title = { Text("Catalog not found") })
    } else {
      TopAppBar(title = { Text(catalog.name) })
    }

  }
}
