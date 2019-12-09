/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

class GetManga @Inject constructor(
  private val mangaRepository: MangaRepository
) {

  fun subscribe(mangaId: Long): Flow<Manga?> {
    return mangaRepository.subscribe(mangaId)
  }

  suspend fun await(mangaId: Long): Manga? {
    return mangaRepository.find(mangaId)
  }

}
