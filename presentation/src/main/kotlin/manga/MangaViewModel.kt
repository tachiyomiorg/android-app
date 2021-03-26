/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.service.CatalogStore
import tachiyomi.domain.library.interactor.ChangeMangaFavorite
import tachiyomi.domain.manga.interactor.GetChapters
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class MangaViewModel @Inject constructor(
  private val params: Params,
  private val getManga: GetManga,
  private val getChapters: GetChapters,
  private val store: CatalogStore,
  private val changeMangaFavorite: ChangeMangaFavorite,
  private val syncChaptersFromSource: SyncChaptersFromSource
) : BaseViewModel() {

  var isRefreshing by mutableStateOf(false)
    private set

  var source by mutableStateOf<Source?>(null)
    private set

  var expandedSummary by mutableStateOf(false)
    private set

  val manga by getManga.subscribe(params.mangaId)
    .onEach(::onMangaChange)
    .asState(null)

  val chapters by getChapters.subscribeForManga(params.mangaId).asState(emptyList())

  init {
//    scope.launch {
//      withContext(Dispatchers.IO) {
//        val manga = getManga.await(params.mangaId)
//        if (manga != null) {
//          syncChaptersFromSource.await(manga)
//        }
//      }
//    }
  }

  fun favorite() {
    scope.launch {
      manga?.let {
        changeMangaFavorite.await(it)
      }
    }
  }

  fun toggleExpandedSummary() {
    expandedSummary = !expandedSummary
  }

  private fun onMangaChange(manga: Manga?) {
    if (manga != null) {
      source = store.get(manga.sourceId)?.source
    }
  }

  data class Params(val mangaId: Long)
}
