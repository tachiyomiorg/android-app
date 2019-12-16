/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.interactor

import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.model.TrackSearchResult
import tachiyomi.domain.track.service.TrackRepository
import tachiyomi.domain.track.sites.TrackSite
import javax.inject.Inject

class BindTrack @Inject constructor(
  private val trackRepository: TrackRepository
) {

  suspend fun await(site: TrackSite, info: TrackSearchResult, manga: Manga): Result {
    var entryId = site.getEntryId(info.mediaId)
    if (entryId == null) {
      entryId = site.add(info.mediaId)
    }
    val state = site.getState(entryId) ?: return Result.NotFound

    val track = Track(
      id = -1,
      mangaId = manga.id,
      siteId = site.id,
      entryId = entryId,
      mediaId = info.mediaId,
      mediaUrl = info.mediaUrl,
      title = info.title,
      lastChapterRead = state.lastChapterRead,
      totalChapters = state.totalChapters,
      score = state.score,
      status = state.status
    )

    trackRepository.save(track)

    return Result.Success
  }

  sealed class Result {
    object Success : Result()
    object NotFound : Result()
    data class Error(val exception: Throwable)
  }

}
