/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.library.service.MangaCategoryRepository
import javax.inject.Inject

class SetCategoriesForMangas @Inject internal constructor(
  private val mangaCategoryRepository: MangaCategoryRepository
) {

  suspend fun await(categoryIds: Collection<Long>, mangaIds: Collection<Long>): Result {
    val newMangaCategories = getNewMangaCategories(categoryIds, mangaIds)

    return try {
      mangaCategoryRepository.replaceAll(newMangaCategories)
      Result.Success
    } catch (e: Exception) {
      Result.InternalError(e)
    }
  }

  private fun getNewMangaCategories(
    categoryIds: Collection<Long>,
    mangaIds: Collection<Long>
  ): List<MangaCategory> {
    val newMangaCategories = mutableListOf<MangaCategory>()
    for (categoryId in categoryIds) {
      // System categories don't need entries
      if (categoryId > 0) {
        for (mangaId in mangaIds) {
          val mangaCategory = MangaCategory(mangaId, categoryId)
          newMangaCategories.add(mangaCategory)
        }
      }
    }
    return newMangaCategories
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
