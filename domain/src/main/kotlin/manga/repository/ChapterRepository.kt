/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate

interface ChapterRepository {

  fun subscribeForManga(mangaId: Long): Flow<List<Chapter>>

  fun subscribe(chapterId: Long): Flow<Chapter?>

  suspend fun findForManga(mangaId: Long): List<Chapter>

  suspend fun find(chapterId: Long): Chapter?

  suspend fun find(chapterKey: String, mangaId: Long): Chapter?

  suspend fun insert(chapters: List<Chapter>)

  suspend fun update(chapters: List<Chapter>)

  suspend fun updatePartial(updates: List<ChapterUpdate>)

  suspend fun updateOrder(chapters: List<Chapter>)

  suspend fun delete(chapters: List<Chapter>)

}
