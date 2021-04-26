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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.core.log.Log
import tachiyomi.domain.catalog.service.CatalogStore
import tachiyomi.domain.library.interactor.ChangeMangaFavorite
import tachiyomi.domain.manga.interactor.GetChapters
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.MangaInitializer
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
  private val syncChaptersFromSource: SyncChaptersFromSource,
  private val mangaInitializer: MangaInitializer
) : BaseViewModel() {

  var isRefreshing by mutableStateOf(false)
    private set

  var source by mutableStateOf<Source?>(null)
    private set

  var expandedSummary by mutableStateOf(false)
    private set

  val manga by getManga.subscribe(params.mangaId)
    .onEach(::onMangaUpdate)
    .asState(null)

  val chapters by getChapters.subscribeForManga(params.mangaId).asState(emptyList())

  private fun onMangaUpdate(manga: Manga?) {
    if (manga != null && source == null) {
      source = store.get(manga.sourceId)?.source

      // Update in a new coroutine to read manga from the view model property
      scope.launch { updateManga(metadata = true) }
    }
  }

  fun toggleFavorite() {
    scope.launch {
      manga?.let { changeMangaFavorite.await(it) }
    }
  }

  fun toggleExpandedSummary() {
    expandedSummary = !expandedSummary
  }

  private fun updateManga(
    metadata: Boolean = false,
    chapters: Boolean = false,
    tracking: Boolean = false
  ) {
    val manga = manga ?: return
    scope.launch {
      withContext(Dispatchers.IO) {
        isRefreshing = true
        try {
          if (chapters) {
            syncChaptersFromSource.await(manga)
          }
          if (metadata) {
            mangaInitializer.await(manga, force = false)
          }
          if (tracking) {
            // TODO
          }
        } catch (e: Throwable) {
          Log.error(e, "Error while refreshing manga")
        }
        isRefreshing = false
      }
    }
  }

  data class Params(val mangaId: Long)
}
