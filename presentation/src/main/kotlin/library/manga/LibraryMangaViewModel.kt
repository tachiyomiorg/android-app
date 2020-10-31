/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library.manga

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class LibraryMangaViewModel @Inject constructor(
  private val params: Params,
  private val getManga: GetManga,
) : BaseViewModel() {

  var manga by mutableStateOf<Manga?>(null)
    private set
  var isRefreshing by mutableStateOf(false)
    private set

  init {
    scope.launch {
      manga = getManga.await(params.mangaId)
    }
  }

  data class Params(val mangaId: Long)
}
