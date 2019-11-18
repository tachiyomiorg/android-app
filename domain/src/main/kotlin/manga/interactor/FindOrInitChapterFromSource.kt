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
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class FindOrInitChapterFromSource @Inject constructor(
  private val getChapter: GetChapter,
  private val syncChaptersFromSource: SyncChaptersFromSource
) {

  suspend fun await(chapterKey: String, manga: Manga): Chapter? {
    return withContext(Dispatchers.IO) {
      val chapter = getChapter.await(chapterKey, manga.id)
      if (chapter != null) {
        chapter
      } else {
        syncChaptersFromSource.await(manga)
        getChapter.await(chapterKey, manga.id)
      }
    }
  }

}
