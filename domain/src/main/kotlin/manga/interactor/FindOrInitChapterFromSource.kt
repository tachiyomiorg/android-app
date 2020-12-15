/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class FindOrInitChapterFromSource @Inject internal constructor(
  private val getChapters: GetChapters,
  private val syncChaptersFromSource: SyncChaptersFromSource
) {

  suspend fun await(chapterKey: String, manga: Manga): Chapter? {
    val chapter = getChapters.await(chapterKey, manga.id)
    return if (chapter != null) {
      chapter
    } else {
      syncChaptersFromSource.await(manga)
      getChapters.await(chapterKey, manga.id)
    }
  }

}
