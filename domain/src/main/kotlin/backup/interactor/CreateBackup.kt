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
import kotlinx.serialization.dump
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.buffer
import okio.gzip
import okio.sink
import tachiyomi.core.db.Transactions
import tachiyomi.domain.backup.model.Backup
import tachiyomi.domain.backup.model.CategoryProto
import tachiyomi.domain.backup.model.ChapterProto
import tachiyomi.domain.backup.model.MangaProto
import tachiyomi.domain.backup.model.TrackProto
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.manga.service.ChapterRepository
import tachiyomi.domain.manga.service.MangaRepository
import tachiyomi.domain.track.service.TrackRepository
import java.io.File
import javax.inject.Inject

class CreateBackup @Inject internal constructor(
  private val mangaRepository: MangaRepository,
  private val categoryRepository: CategoryRepository,
  private val chapterRepository: ChapterRepository,
  private val trackRepository: TrackRepository,
  private val transactions: Transactions
) {

  suspend fun saveTo(file: File) {
    withContext(Dispatchers.IO) {
      file.sink().gzip().buffer().use { it.write(createDump()) }
    }
  }

  internal suspend fun createDump(): ByteArray {
    val backup = transactions.run {
      Backup(
        library = dumpLibrary(),
        categories = dumpCategories()
      )
    }

    return ProtoBuf { encodeDefaults = false }.encodeToByteArray(backup)
  }

  internal suspend fun dumpLibrary(): List<MangaProto> {
    return mangaRepository.findFavorites()
      .map { manga ->
        val chapters = dumpChapters(manga.id)
        val mangaCategories = dumpMangaCategories(manga.id)
        val tracks = dumpTracks(manga.id)

        MangaProto.fromDomain(manga, chapters, mangaCategories, tracks)
      }
  }

  internal suspend fun dumpChapters(mangaId: Long): List<ChapterProto> {
    return chapterRepository.findForManga(mangaId).map { chapter ->
      ChapterProto.fromDomain(chapter)
    }
  }

  internal suspend fun dumpMangaCategories(mangaId: Long): List<Int> {
    return categoryRepository.findCategoriesOfManga(mangaId)
      .filter { !it.isSystemCategory }
      .map { it.order }
  }

  internal suspend fun dumpTracks(mangaId: Long): List<TrackProto> {
    return trackRepository.findAllForManga(mangaId).map { TrackProto.fromDomain(it) }
  }

  internal suspend fun dumpCategories(): List<CategoryProto> {
    return categoryRepository.findAll()
      .filter { !it.isSystemCategory }
      .map { cat -> CategoryProto.fromDomain(cat) }
  }

}
