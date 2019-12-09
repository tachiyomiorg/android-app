/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.DeepLinkSource
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class FindOrInitMangaFromChapterKey @Inject constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource,
  private val mangaInitializer: MangaInitializer
) {

  suspend fun await(chapterKey: String, source: DeepLinkSource): Manga {
    val mangaKey = source.findMangaKey(chapterKey)
    return if (mangaKey != null) {
      val mangaInfo = MangaInfo(key = mangaKey, title = "")
      val manga = getOrAddMangaFromSource.await(mangaInfo, source.id)
      mangaInitializer.await(source, manga)
      manga
    } else {
      throw Exception("Manga key not found")
    }
  }

}
