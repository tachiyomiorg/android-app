/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import tachiyomi.core.db.Transaction
import tachiyomi.domain.catalog.service.CatalogStore
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.MangaBase
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.service.ChapterRepository
import tachiyomi.domain.manga.service.MangaRepository
import tachiyomi.domain.manga.util.ChapterRecognition
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class SyncChaptersFromSource @Inject constructor(
  private val chapterRepository: ChapterRepository,
  private val mangaRepository: MangaRepository,
  private val catalogStore: CatalogStore,
  private val transaction: Transaction
) {

  data class Diff(
    val added: List<Chapter> = emptyList(),
    val deleted: List<Chapter> = emptyList(),
    val updated: List<Chapter> = emptyList()
  )

  suspend fun await(manga: MangaBase): Result {
    val mangaInfo = MangaInfo(manga.key, manga.title)
    val catalog = catalogStore.get(manga.sourceId)
    val source = catalog?.source ?: return Result.SourceNotFound(manga.sourceId)

    // Chapters from source.
    val rawSourceChapters = source.fetchChapterList(mangaInfo)
    if (rawSourceChapters.isEmpty()) {
      return Result.NoChaptersFound
    }

    // Chapters from db.
    val dbChapters = chapterRepository.findForManga(manga.id)

    // Set the date fetch for new items in reverse order to allow another sorting method.
    // Sources MUST return the chapters from most to less recent, which is common.
    var endDateFetch = System.currentTimeMillis() + dbChapters.size

    val sourceChapters = rawSourceChapters.mapIndexed { i, meta ->
      Chapter(
        id = 0,
        mangaId = manga.id,
        key = meta.key,
        name = meta.name,
        dateUpload = meta.dateUpload,
        dateFetch = endDateFetch--,
        scanlator = meta.scanlator,
        number = meta.number.takeIf { it >= 0f } ?: ChapterRecognition.parse(meta, manga.title,
          source),
        sourceOrder = i
      )
    }

    // Chapters from the db not in the source.
    val toDelete = dbChapters.filterNot { dbChapter ->
      sourceChapters.any { it.key == dbChapter.key }
    }
    val toDeleteReadNumbers = toDelete.asSequence()
      .mapNotNull { chapter ->
        if (chapter.isRecognizedNumber && chapter.read) chapter.number else null
      }
      .toSet()

    // Chapters from the source not in db.
    val toAdd = mutableListOf<Chapter>()

    // Chapters whose metadata have changed.
    val toUpdate = mutableListOf<Chapter>()

    for (sourceChapter in sourceChapters) {
      val dbChapter = dbChapters.find { it.key == sourceChapter.key }

      // Add the chapter if not in db already, or update if the metadata changed.
      if (dbChapter == null) {
        // Try to mark already read chapters as read when the source deletes them
        toAdd += if (sourceChapter.number in toDeleteReadNumbers) {
          sourceChapter.copy(read = true)
        } else {
          sourceChapter
        }
      } else {
        //this forces metadata update for the main viewable things in the chapter list
        if (metaChanged(dbChapter, sourceChapter)) {
          toUpdate += dbChapter.copy(
            scanlator = sourceChapter.scanlator,
            name = sourceChapter.name,
            dateUpload = sourceChapter.dateUpload,
            number = sourceChapter.number
          )
        }
      }
    }

    // Return if there's nothing to add, delete or change, avoiding unnecessary db transactions.
    if (toAdd.isEmpty() && toDelete.isEmpty() && toUpdate.isEmpty()) {
      return Result.Success(Diff())
    }

    val diff = Diff(toAdd, toDelete, toUpdate)

    // To avoid notifying chapters that changed the key, we emit downstream a modified list that
    // ignores thoses chapters
    val toDeleteNumbers = toDelete.asSequence()
      .mapNotNull { chapter -> if (chapter.isRecognizedNumber) chapter.number else null }
      .toSet()

    val chaptersToNotify = toAdd.toList() - toAdd.filter { it.number in toDeleteNumbers }
    val notifyDiff = Diff(chaptersToNotify, toDelete, toUpdate)

    transaction.withAction {
      if (diff.deleted.isNotEmpty()) {
        chapterRepository.delete(diff.deleted)
      }
      if (diff.added.isNotEmpty()) {
        chapterRepository.insert(diff.added)
      }
      if (diff.updated.isNotEmpty()) {
        chapterRepository.update(diff.updated)
      }
      chapterRepository.updateOrder(sourceChapters)

      // Set this manga as updated since chapters were changed
      val mangaUpdate = MangaUpdate(manga.id, lastUpdate = System.currentTimeMillis())
      mangaRepository.updatePartial(mangaUpdate)
    }

    return Result.Success(notifyDiff)
  }

  // Checks if the chapter in db needs update
  private fun metaChanged(dbChapter: Chapter, sourceChapter: Chapter): Boolean {
    return dbChapter.scanlator != sourceChapter.scanlator ||
      dbChapter.name != sourceChapter.name ||
      dbChapter.dateUpload != sourceChapter.dateUpload ||
      dbChapter.number != sourceChapter.number
  }

  sealed class Result {
    data class Success(val diff: Diff) : Result()
    data class SourceNotFound(val sourceId: Long) : Result()
    object NoChaptersFound : Result()
  }

}
