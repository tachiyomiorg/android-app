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
import tachiyomi.core.util.Optional
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.Source
import tachiyomi.source.model.MangaInfo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MangaInitializer @Inject internal constructor(
  private val mangaRepository: MangaRepository,
  private val catalogRepository: CatalogRepository,
  private val libraryCovers: LibraryCovers
) {

  // TODO error handling
  suspend fun await(source: Source, manga: Manga, force: Boolean = false): Manga? {
    if (!force && lastInitBelowMinInterval(manga)) return null

    val now = System.currentTimeMillis()

    val infoQuery = MangaInfo(
      key = manga.key,
      title = manga.title,
      artist = manga.artist,
      author = manga.author,
      description = manga.description,
      genres = manga.genres,
      status = manga.status,
      cover = manga.cover
    )
    val newInfo = withContext(Dispatchers.IO) { source.fetchMangaDetails(infoQuery) }

    val update = MangaUpdate(
      id = manga.id,
      key = if (newInfo.key.isEmpty() || newInfo.key == manga.key) {
        Optional.None
      } else {
        Optional.of(newInfo.key)
      },
      title = if (newInfo.title.isEmpty() || newInfo.title == manga.title) {
        Optional.None
      } else {
        Optional.of(newInfo.title)
      },
      artist = Optional.of(newInfo.artist),
      author = Optional.of(newInfo.author),
      description = Optional.of(newInfo.description),
      genres = Optional.of(newInfo.genres),
      status = Optional.of(newInfo.status),
      cover = Optional.of(newInfo.cover),
      lastInit = Optional.of(now)
    )

    val updatedManga = manga.copy(
      key = if (newInfo.key.isEmpty()) manga.key else newInfo.key,
      title = if (newInfo.title.isEmpty()) manga.title else newInfo.title,
      artist = newInfo.artist,
      author = newInfo.author,
      description = newInfo.description,
      genres = newInfo.genres,
      status = newInfo.status,
      cover = newInfo.cover,
      lastInit = now
    )

    withContext(Dispatchers.IO) {
      mangaRepository.savePartial(update)
      libraryCovers.find(manga.id).setLastModified(now)
    }

    return updatedManga
  }

  suspend fun await(manga: Manga, force: Boolean = false): Manga? {
    val source = catalogRepository.get(manga.sourceId)?.source ?: return null
    return await(source, manga, force)
  }

  private fun lastInitBelowMinInterval(manga: Manga): Boolean {
    return System.currentTimeMillis() - manga.lastInit < INIT_MIN_INTERVAL
  }

  private companion object {
    val INIT_MIN_INTERVAL = TimeUnit.DAYS.toMillis(30)
  }

}
