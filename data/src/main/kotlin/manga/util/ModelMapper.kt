/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.util

import tachiyomi.domain.manga.model.Genres
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.model.MangaInfo

internal fun MangaInfo.asDbManga(sourceId: Long): Manga {
  return Manga(
    id = -1,
    sourceId = sourceId,
    key = key,
    title = title,
    artist = artist,
    author = author,
    description = description,
    genres = Genres(),
    status = status,
    cover = cover,
    favorite = false,
    lastUpdate = 0,
    lastInit = 0,
    viewer = 0,
    flags = 0
  )
}
