/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class GetCommonCategories @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  suspend fun await(mangas: List<Manga>): List<Category> {
    val commonCategories = sortedSetOf<Category>(compareBy { it.id })
    mangas.forEachIndexed { index, manga ->
      val categories = categoryRepository.findCategoriesOfManga(manga.id)
      if (index == 0) {
        commonCategories.addAll(categories)
      } else {
        commonCategories.retainAll(categories)
      }
    }
    return commonCategories.toList()
  }

}
