/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.Route
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.LoadingScreen
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.components.manga.MangaGridItem
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CatalogScreen(navController: NavHostController, sourceId: Long) {
  val vm = viewModel<CatalogViewModel> {
    CatalogViewModel.Params(sourceId)
  }

  DisposableEffect(Unit) {
    vm.getNextPage()

    onDispose { }
  }

  Column {
    val catalog = vm.catalog
    if (catalog == null) {
      // TODO empty screen
      Toolbar(
        title = { Text("Catalog not found") },
        navigationIcon = { BackIconButton(navController) },
      )
    } else {
      Toolbar(
        title = { Text(catalog.name) },
        navigationIcon = { BackIconButton(navController) },
      )

      MangaTable(
        mangas = vm.mangas,
        isLoading = vm.isRefreshing,
        hasNextPage = vm.hasNextPage,
        loadNextPage = { vm.getNextPage() },
        onClickManga = {
          navController.navigate("${Route.BrowseCatalogManga.id}/$sourceId/${it.id}")
        },
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaTable(
  mangas: List<Manga>,
  isLoading: Boolean = false,
  hasNextPage: Boolean = false,
  loadNextPage: () -> Unit = {},
  onClickManga: (Manga) -> Unit = {}
) {
  if (mangas.isEmpty()) {
    LoadingScreen()
  } else {
    Column {
      // TODO: this should happen automatically on scroll
      Button(onClick = { loadNextPage() }, enabled = hasNextPage) {
        Text(text = if (isLoading) "Loading..." else "Load next page")
      }

      LazyVerticalGrid(GridCells.Adaptive(160.dp)) {
        items(mangas) { manga ->
          MangaGridItem(
            title = manga.title,
            cover = MangaCover.from(manga),
            onClick = { onClickManga(manga) }
          )
        }
      }
    }
  }
}
