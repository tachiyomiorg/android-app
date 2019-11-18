/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.FilterList
import javax.inject.Inject

class SearchMangaPageFromCatalogSource @Inject internal constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) {

  suspend fun await(source: CatalogSource, filters: FilterList, page: Int): MangasPage {
    return withContext(Dispatchers.IO) {
      val sourcePage = source.fetchMangaList(filters, page)
      val localPage = sourcePage.mangas.map { getOrAddMangaFromSource.await(it, source.id) }

      MangasPage(page, localPage, sourcePage.hasNextPage)
    }
  }

}
