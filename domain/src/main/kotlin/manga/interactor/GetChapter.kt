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
import tachiyomi.domain.manga.repository.ChapterRepository
import javax.inject.Inject

class GetChapter @Inject constructor(
  private val repository: ChapterRepository
) {

  suspend fun await(id: Long) = withContext(Dispatchers.IO) {
    repository.find(id)
  }

  suspend fun await(key: String, mangaId: Long) = withContext(Dispatchers.IO) {
    repository.find(key, mangaId)
  }

}
