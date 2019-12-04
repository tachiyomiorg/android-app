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
import tachiyomi.domain.manga.model.Genres
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetOrAddMangaFromSource @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  suspend fun await(manga: MangaInfo, sourceId: Long) = withContext(Dispatchers.IO) {
    val dbManga = mangaRepository.find(manga.key, sourceId)
    if (dbManga != null) {
      dbManga
    } else {
      val newManga = Manga(
        id = 0,
        sourceId = sourceId,
        key = manga.key,
        title = manga.title,
        artist = manga.artist,
        author = manga.author,
        description = manga.description,
        genres = Genres(manga.genres),
        status = manga.status,
        cover = manga.cover
      )
      val id = mangaRepository.insert(newManga)
      newManga.copy(id = id)
    }
  }

}
