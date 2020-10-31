/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onActive
import androidx.navigation.NavController
import tachiyomi.source.model.MangaInfo
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CatalogScreen(navController: NavController, sourceId: Long) {
  val vm = viewModel<CatalogViewModel> {
    CatalogViewModel.Params(sourceId)
  }

  onActive {
    vm.getNextPage()
  }

  Column {
    val catalog = vm.catalog
    if (catalog == null) {
      // TODO empty screen
      TopAppBar(title = { Text("Catalog not found") })
    } else {
      TopAppBar(title = { Text(catalog.name) })

      MangaList(
        mangas = vm.mangas,
        isLoading = vm.isRefreshing,
        hasNextPage = vm.hasNextPage,
        loadNextPage = { vm.getNextPage() }
      )
    }
  }
}

@Composable
fun MangaList(
  mangas: List<MangaInfo>,
  isLoading: Boolean = false,
  hasNextPage: Boolean = false,
  loadNextPage: () -> Unit = {}
) {
  if (mangas.isEmpty()) {
    LoadingScreen()
  } else {
    ScrollableColumn {
      mangas.forEach {
        Row {
          Text(text = it.title)
        }
      }

      Button(onClick = { loadNextPage() }, enabled = hasNextPage) {
        if (isLoading) {
          CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
        } else {
          Text(text = "Load next page")
        }
      }
    }
  }
}