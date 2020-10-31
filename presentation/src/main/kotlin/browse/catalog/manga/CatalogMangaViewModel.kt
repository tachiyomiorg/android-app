/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse.catalog.manga

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tachiyomi.domain.manga.interactor.GetOrAddMangaFromSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogMangaViewModel @Inject constructor(
  private val params: Params,
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource,
) : BaseViewModel() {

  var manga by mutableStateOf<Manga?>(null)
    private set
  var isRefreshing by mutableStateOf(false)
    private set

  init {
    // TODO: ideally we'd get the fully MangaInfo object, but navigation doesn't let you
    // pass around parcelable/serializable objects

//    scope.launch {
//      manga = getOrAddMangaFromSource.await(params.manga, params.sourceId)
//    }
  }

  data class Params(val sourceId: Long, val key: String)
}
