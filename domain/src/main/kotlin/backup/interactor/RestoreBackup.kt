/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.load
import kotlinx.serialization.protobuf.ProtoBuf
import okio.buffer
import okio.gzip
import okio.source
import tachiyomi.core.db.Transactions
import tachiyomi.domain.backup.model.Backup
import tachiyomi.domain.backup.model.CategoryProto
import tachiyomi.domain.backup.model.MangaProto
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.library.service.MangaCategoryRepository
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.service.ChapterRepository
import tachiyomi.domain.manga.service.MangaRepository
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.model.TrackUpdate
import tachiyomi.domain.track.service.TrackRepository
import java.io.File
import javax.inject.Inject

class RestoreBackup @Inject internal constructor(
  private val mangaRepository: MangaRepository,
  private val categoryRepository: CategoryRepository,
  private val chapterRepository: ChapterRepository,
  private val trackRepository: TrackRepository,
  private val mangaCategoryRepository: MangaCategoryRepository,
  private val transactions: Transactions
) {

  suspend fun restoreFrom(file: File) {
    withContext(Dispatchers.IO) {
      val bytes = file.source().gzip().buffer().use { it.readByteArray() }
      val backup = loadDump(bytes)

      transactions.run {
        restoreCategories(backup.categories)
        val backupCategoriesWithId = getCategoryIdsByBackupId(backup.categories)
        for (manga in backup.library) {
          val mangaId = restoreManga(manga)
          restoreChapters(manga)
          restoreCategoriesOfManga(mangaId,
            manga.categories.mapNotNull(backupCategoriesWithId::get))
          restoreTracks(manga, mangaId)
        }
      }
    }
  }

  private fun loadDump(data: ByteArray): Backup {
    return ProtoBuf.decodeFromByteArray(data)
  }

  internal suspend fun restoreManga(manga: MangaProto): Long {
    val dbManga = mangaRepository.find(manga.key, manga.sourceId)
    if (dbManga == null) {
      val newManga = manga.toDomain()
      return mangaRepository.insert(newManga)
    }
    if (manga.lastInit > dbManga.lastInit || !dbManga.favorite) {
      val update = MangaUpdate(
        dbManga.id,
        title = manga.title,
        artist = manga.artist,
        author = manga.author,
        description = manga.description,
        genres = manga.genres,
        status = manga.status,
        cover = manga.cover,
        customCover = manga.customCover,
        favorite = true,
        lastUpdate = manga.lastUpdate,
        lastInit = manga.lastInit,
        dateAdded = manga.dateAdded,
        viewer = manga.viewer,
        flags = manga.flags
      )
      mangaRepository.updatePartial(update)
    }
    return dbManga.id
  }

  internal suspend fun restoreChapters(manga: MangaProto) {
    if (manga.chapters.isEmpty()) return

    val dbManga = checkNotNull(mangaRepository.find(manga.key, manga.sourceId))
    val dbChapters = chapterRepository.findForManga(dbManga.id)

    // Keep the backup chapters
    if (manga.lastUpdate > dbManga.lastUpdate) {
      chapterRepository.delete(dbChapters)
      val dbChaptersMap = dbChapters.associateBy { it.key }

      val chaptersToAdd = mutableListOf<Chapter>()
      for (backupChapter in manga.chapters) {
        val dbChapter = dbChaptersMap[backupChapter.key]
        val newChapter = if (dbChapter != null) {
          backupChapter.toDomain(dbManga.id).copy(
            read = backupChapter.read || dbChapter.read,
            bookmark = backupChapter.bookmark || dbChapter.bookmark,
            progress = maxOf(backupChapter.progress, dbChapter.progress)
          )
        } else {
          backupChapter.toDomain(dbManga.id)
        }
        chaptersToAdd.add(newChapter)
      }
      chapterRepository.insert(chaptersToAdd)
    }
    // Keep the database chapters
    else {
      val backupChaptersMap = manga.chapters.associateBy { it.key }

      val chaptersToUpdate = mutableListOf<ChapterUpdate>()
      for (dbChapter in dbChapters) {
        val backupChapter = backupChaptersMap[dbChapter.key]
        if (backupChapter != null) {
          val update = ChapterUpdate(
            dbChapter.id,
            read = dbChapter.read || backupChapter.read,
            bookmark = dbChapter.bookmark || backupChapter.bookmark,
            progress = maxOf(backupChapter.progress, dbChapter.progress)
          )
          chaptersToUpdate.add(update)
        }
      }
      if (chaptersToUpdate.isNotEmpty()) {
        chapterRepository.updatePartial(chaptersToUpdate)
      }
    }
  }

  internal suspend fun restoreCategories(categories: List<CategoryProto>) {
    if (categories.isEmpty()) return

    val dbCategories = categoryRepository.findAll()
    val dbCategoryNames = dbCategories.map { it.name }
    val categoriesToAdd = categories
      .filter { category -> dbCategoryNames.none { category.name.equals(it, true) } }
      .mapIndexed { index, category ->
        category.toDomain().copy(
          order = dbCategories.size + index
        )
      }

    if (categoriesToAdd.isNotEmpty()) {
      categoryRepository.insert(categoriesToAdd)
    }
  }

  internal suspend fun restoreCategoriesOfManga(mangaId: Long, categoryIds: List<Long>) {
    if (categoryIds.isEmpty()) return

    val mangaCategories = categoryIds.map { categoryId ->
      MangaCategory(mangaId, categoryId)
    }

    if (mangaCategories.isNotEmpty()) {
      mangaCategoryRepository.replaceAll(mangaCategories)
    }
  }

  internal suspend fun restoreTracks(manga: MangaProto, mangaId: Long) {
    if (manga.tracks.isEmpty()) return

    val dbTracks = trackRepository.findAllForManga(mangaId)
    val tracksToAdd = mutableListOf<Track>()
    val tracksToUpdate = mutableListOf<TrackUpdate>()

    for (track in manga.tracks) {
      val dbTrack = dbTracks.find { it.siteId == track.siteId }

      if (dbTrack == null) {
        tracksToAdd.add(track.toDomain(mangaId))
      } else {
        if (track.lastRead > dbTrack.lastRead || track.totalChapters > dbTrack.totalChapters) {
          val update = TrackUpdate(
            dbTrack.id,
            lastRead = maxOf(dbTrack.lastRead, track.lastRead),
            totalChapters = maxOf(dbTrack.totalChapters, track.totalChapters)
          )
          tracksToUpdate.add(update)
        }
      }
    }

    if (tracksToAdd.isNotEmpty()) {
      trackRepository.insert(tracksToAdd)
    }
    if (tracksToUpdate.isNotEmpty()) {
      trackRepository.updatePartial(tracksToUpdate)
    }
  }

  private suspend fun getCategoryIdsByBackupId(categories: List<CategoryProto>): Map<Int, Long> {
    val dbCategories = categoryRepository.findAll()
    return categories.associate { backupCategory ->
      val dbId = dbCategories.first { it.name.equals(backupCategory.name, true) }.id
      backupCategory.order to dbId
    }
  }

}
