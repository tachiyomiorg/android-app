/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.service

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate

interface MangaRepository {

  fun subscribe(mangaId: Long): Flow<Manga?>

  fun subscribe(key: String, sourceId: Long): Flow<Manga?>

  suspend fun findFavorites(): List<Manga>

  suspend fun find(mangaId: Long): Manga?

  suspend fun find(key: String, sourceId: Long): Manga?

  suspend fun insert(manga: Manga): Long

  suspend fun updatePartial(update: MangaUpdate)

  suspend fun deleteNonFavorite()

}
