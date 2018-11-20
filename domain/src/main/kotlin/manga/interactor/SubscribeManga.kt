/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import tachiyomi.core.rx.RxOptional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

class SubscribeManga @Inject constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(mangaId: Long): Flowable<RxOptional<Manga>> {
    return mangaRepository.subscribeManga(mangaId)

  }

}