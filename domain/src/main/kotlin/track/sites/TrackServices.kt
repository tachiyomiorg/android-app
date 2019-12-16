/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.sites

import tachiyomi.core.http.Http
import tachiyomi.domain.track.service.TrackPreferences
import tachiyomi.domain.track.sites.myanimelist.MyAnimeList
import javax.inject.Inject

class TrackServices @Inject constructor(
  private val http: Http,
  private val trackPreferences: TrackPreferences
) {

  val myAnimeList = MyAnimeList(http, trackPreferences)

  fun get(id: Int): TrackSite? {
    return when (id) {
      MYANIMELIST -> myAnimeList
      ANILIST -> null
      KITSU -> null
      SHIKIMORI -> null
      else -> null
    }
  }

  companion object {
    const val MYANIMELIST = 1
    const val ANILIST = 2
    const val KITSU = 3
    const val SHIKIMORI = 4
  }

}
