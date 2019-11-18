/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import tachiyomi.core.db.Transaction
import tachiyomi.core.util.Optional
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.library.repository.MangaCategoryRepository
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject
import javax.inject.Provider

class ChangeMangaFavorite @Inject constructor(
  private val mangaRepository: MangaRepository,
  private val mangaCategoryRepository: MangaCategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val libraryCovers: LibraryCovers,
  private val transactions: Provider<Transaction>,
  private val setCategoriesForMangas: SetCategoriesForMangas
) {

  suspend fun await(manga: Manga): Result = withContext(NonCancellable) f@{
    val now = System.currentTimeMillis()
    val nowFavorite = !manga.favorite
    val update = if (nowFavorite) {
      MangaUpdate(
        id = manga.id,
        favorite = Optional.of(true),
        dateAdded = Optional.of(now)
      )
    } else {
      MangaUpdate(id = manga.id, favorite = Optional.of(false))
    }

    val mangaIds = listOf(manga.id)

    try {
      withContext(Dispatchers.IO) {
        transactions.get().withAction {
          mangaRepository.savePartial(update)

          if (nowFavorite) {
            val defaultCategory = libraryPreferences.defaultCategory().get()
            val result = setCategoriesForMangas.execute(listOf(defaultCategory), mangaIds)
            if (result is SetCategoriesForMangas.Result.InternalError) {
              throw result.error
            }
          } else {
            mangaCategoryRepository.deleteForMangas(mangaIds)
          }
        }
      }
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }

    if (!nowFavorite) {
      try {
        libraryCovers.delete(manga.id)
      } catch (e: Exception) {
        // Ignore this exception
      }
    }

    Result.Success
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
