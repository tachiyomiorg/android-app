/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse.catalog.manga

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.components.manga.MangaScreen
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CatalogMangaScreen(navController: NavHostController, mangaId: Long) {
  val vm = viewModel<CatalogMangaViewModel> {
    CatalogMangaViewModel.Params(mangaId)
  }

  Scaffold(
    topBar = {
      Toolbar(
        title = { Text(vm.manga?.title ?: "$mangaId") },
        navigationIcon = { BackIconButton(navController) },
      )
    },
    bodyContent = { MangaScreen(navController, vm.manga, vm.chapters, onFavorite = vm::favorite) }
  )
}
