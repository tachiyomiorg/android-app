/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.manga.interactor.ListMangaPageFromCatalogSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogViewModel @Inject constructor(
  private val params: Params,
  private val getLocalCatalog: GetLocalCatalog,
  private val listMangaPageFromCatalogSource: ListMangaPageFromCatalogSource,
  private val mangaInitializer: MangaInitializer
) : BaseViewModel() {

  private var page: Int = 1

  var catalog by mutableStateOf(getLocalCatalog.get(params.sourceId))
    private set
  var isRefreshing by mutableStateOf(false)
    private set
  val mangas = mutableStateListOf<Manga>()
  var hasNextPage by mutableStateOf(true)
    private set

  fun toggle() {
    isRefreshing = !isRefreshing
  }

  fun getNextPage() {
    isRefreshing = true

    scope.launch {
      if (catalog?.source is CatalogSource) {
        val mangaPage = listMangaPageFromCatalogSource.await((catalog!!.source as CatalogSource),
          null, page)

        mangas.addAll(mangaPage.mangas)
        hasNextPage = mangaPage.hasNextPage

        // TODO maybe there should be a global task to not launch once per page
        scope.launch {
          for (manga in mangaPage.mangas) {
            val initialized = mangaInitializer.await(manga) ?: continue
            val position = mangas.indexOfFirst { it.id == initialized.id }
            if (position != -1) {
              mangas[position] = initialized
            }
          }
        }

        page++
      }
    }

    isRefreshing = false
  }

  data class Params(val sourceId: Long)
}
