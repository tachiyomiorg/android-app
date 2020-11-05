/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library.manga

import androidx.compose.foundation.Text
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.components.manga.MangaScreen
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun LibraryMangaScreen(navController: NavController, mangaId: Long) {
  val vm = viewModel<LibraryMangaViewModel> {
    LibraryMangaViewModel.Params(mangaId)
  }

  Scaffold(
    topBar = { Toolbar(title = { Text(vm.manga?.title ?: "$mangaId") }) },
    bodyContent = { MangaScreen(navController, vm.manga) }
  )
}
