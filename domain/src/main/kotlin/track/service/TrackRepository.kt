/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.service

import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.model.TrackUpdate

interface TrackRepository {

  suspend fun findForManga(mangaId: Long, siteId: Int): Track?

  suspend fun findAllForManga(mangaId: Long): List<Track>

  suspend fun save(track: Track)

  suspend fun savePartial(update: TrackUpdate)

}
