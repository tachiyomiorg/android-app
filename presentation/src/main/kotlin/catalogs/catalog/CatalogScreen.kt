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
import androidx.compose.material.Button
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onActive
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tachiyomi.source.model.MangaInfo
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.AutofitGrid
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.MangaGridItem
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
        sourceId = sourceId,
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
  sourceId: Long,
  mangas: List<MangaInfo>,
  isLoading: Boolean = false,
  hasNextPage: Boolean = false,
  loadNextPage: () -> Unit = {}
) {
  if (mangas.isEmpty()) {
    LoadingScreen()
  } else {
    Column {
      // TODO: this should happen automatically on scroll
      Button(onClick = { loadNextPage() }, enabled = hasNextPage) {
        Text(text = if (isLoading) "Loading..." else "Load next page")
      }

      AutofitGrid(data = mangas, defaultColumnWidth = 160.dp) { manga ->
        MangaGridItem(
          title = manga.title,
          cover = MangaCover.from(manga, sourceId)
        )
      }
    }
  }
}
