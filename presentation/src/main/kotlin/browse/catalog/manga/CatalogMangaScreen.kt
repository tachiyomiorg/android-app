/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse.catalog.manga

import androidx.compose.foundation.Text
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.manga.MangaScreen
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CatalogMangaScreen(navController: NavHostController, sourceId: Long, mangaId: Long) {
  val vm = viewModel<CatalogMangaViewModel> {
    CatalogMangaViewModel.Params(sourceId, mangaId)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(vm.manga?.title ?: "$sourceId/$mangaId") },
        navigationIcon = { BackIconButton(navController) },
      )
    },
    bodyContent = { MangaScreen(navController, vm.manga, vm.chapters) }
  )
}
