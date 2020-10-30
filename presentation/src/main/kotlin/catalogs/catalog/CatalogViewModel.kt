/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.MangaInfo
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogViewModel @Inject constructor(
  private val params: Params,
  private val getLocalCatalog: GetLocalCatalog,
) : BaseViewModel() {

  private var page: Int = 0

  var catalog by mutableStateOf<CatalogLocal?>(null)
    private set
  var isRefreshing by mutableStateOf(false)
    private set
  var mangas by mutableStateOf<List<MangaInfo>>(mutableListOf())
    private set
  var hasNextPage by mutableStateOf(true)
    private set

  init {
    catalog = getLocalCatalog.get(params.sourceId)
  }

  fun toggle() {
    isRefreshing = !isRefreshing
  }

  fun getNextPage() {
    scope.launch {
      isRefreshing = true

      if (catalog?.source is CatalogSource) {
        val mangaInfo = (catalog!!.source as CatalogSource).getMangaList(sort = null, page = page)
        mangas += mangaInfo.mangas
        hasNextPage = mangaInfo.hasNextPage

        page++
      }

      isRefreshing = false
    }
  }

  data class Params(val sourceId: Long)
}
