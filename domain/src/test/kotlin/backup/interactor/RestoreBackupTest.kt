/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.interactor

import io.kotest.core.spec.style.StringSpec
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import tachiyomi.core.db.Transactions
import tachiyomi.domain.backup.model.CategoryProto
import tachiyomi.domain.backup.model.ChapterProto
import tachiyomi.domain.backup.model.MangaProto
import tachiyomi.domain.backup.model.TrackProto
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.library.service.CategoryRepository
import tachiyomi.domain.library.service.MangaCategoryRepository
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.service.ChapterRepository
import tachiyomi.domain.manga.service.MangaRepository
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.model.TrackUpdate
import tachiyomi.domain.track.service.TrackRepository

class RestoreBackupTest : StringSpec({

  val mangaRepository = mockk<MangaRepository>(relaxed = true)
  val categoryRepository = mockk<CategoryRepository>(relaxed = true)
  val chapterRepository = mockk<ChapterRepository>(relaxed = true)
  val trackRepository = mockk<TrackRepository>(relaxed = true)
  val mangaCategoryRepository = mockk<MangaCategoryRepository>(relaxed = true)
  val transactions = mockk<Transactions>(relaxed = true)
  val interactor = RestoreBackup(mangaRepository, categoryRepository, chapterRepository,
    trackRepository, mangaCategoryRepository, transactions)
  afterTest { clearAllMocks() }

  // Set defaults for repositories
  beforeTest {
    coEvery { categoryRepository.findAll() } returns listOf()
    coEvery { categoryRepository.findCategoriesOfManga(any()) } returns listOf()
    coEvery { mangaRepository.findFavorites() } returns listOf()
    coEvery { chapterRepository.findForManga(any()) } returns listOf()
    coEvery { trackRepository.findAllForManga(any()) } returns listOf()
  }

  "restores non existent manga" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1"
    )
    coEvery { mangaRepository.find(any(), any()) } returns null

    interactor.restoreManga(backupManga)

    coVerify { mangaRepository.insert(backupManga.toDomain()) }
  }
  "restores backup manga when it's more recent" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 2
    )
    coEvery { mangaRepository.find(any(), any()) } returns Manga(
      id = 1,
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 1,
      favorite = true
    )

    interactor.restoreManga(backupManga)

    coVerify { mangaRepository.updatePartial(any()) }
  }
  "keeps db manga when it's more recent" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 1
    )
    coEvery { mangaRepository.find(any(), any()) } returns Manga(
      id = 1,
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 2,
      favorite = true
    )

    interactor.restoreManga(backupManga)

    coVerify(exactly = 0) {
      mangaRepository.updatePartial(any())
      mangaRepository.insert(any())
    }
  }
  "restores backup manga when not in library" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 2
    )
    coEvery { mangaRepository.find(any(), any()) } returns Manga(
      id = 1,
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastInit = 1,
      favorite = false
    )

    interactor.restoreManga(backupManga)

    coVerify { mangaRepository.updatePartial(any()) }
  }
  "keeps db chapters untouched when backup is empty" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      chapters = listOf()
    )
    coEvery { mangaRepository.find(any(), any()) } returns backupManga.toDomain()

    interactor.restoreChapters(backupManga)

    coVerify { chapterRepository wasNot Called }
  }
  "restores backup chapters when they're more recent (db chapter present)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastUpdate = 2,
      chapters = listOf(
        ChapterProto(
          key = "chkey1",
          name = "name 1"
        )
      )
    )
    coEvery { mangaRepository.find(any(), any()) } returns backupManga.toDomain().copy(
      lastUpdate = 1
    )
    coEvery { chapterRepository.findForManga(any()) } returns backupManga.chapters.map {
      it.toDomain(1)
    }

    interactor.restoreChapters(backupManga)

    coVerifyOrder {
      chapterRepository.delete(any())
      chapterRepository.insert(match { it.size == 1 })
    }
  }
  "restores backup chapters when they're more recent (db chapter missing)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastUpdate = 2,
      chapters = listOf(
        ChapterProto(
          key = "chkey1",
          name = "name 1"
        )
      )
    )
    coEvery { mangaRepository.find(any(), any()) } returns backupManga.toDomain().copy(
      lastUpdate = 1
    )
    coEvery { chapterRepository.findForManga(any()) } returns emptyList()

    interactor.restoreChapters(backupManga)

    coVerifyOrder {
      chapterRepository.delete(any())
      chapterRepository.insert(match { it.size == 1 })
    }
  }
  "keeps db chapters when they're more recent (backup chapter present)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastUpdate = 2,
      chapters = listOf(
        ChapterProto(
          key = "chkey1",
          name = "name 1"
        )
      )
    )
    coEvery { mangaRepository.find(any(), any()) } returns backupManga.toDomain()
    coEvery { chapterRepository.findForManga(any()) } returns backupManga.chapters.map {
      it.toDomain(1)
    }

    interactor.restoreChapters(backupManga)

    coVerify(exactly = 0) { chapterRepository.delete(any()) }
    coVerify { chapterRepository.updatePartial(match { it.size == 1 }) }
  }
  "keeps db chapters when they're more recent (backup chapter missing)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      lastUpdate = 2,
      chapters = listOf()
    )
    coEvery { mangaRepository.find(any(), any()) } returns backupManga.toDomain()
    coEvery { chapterRepository.findForManga(any()) } returns listOf(
      Chapter(key = "chkey1", name = "name 1")
    )

    interactor.restoreChapters(backupManga)

    coVerify(exactly = 0) {
      chapterRepository.delete(any())
      chapterRepository.updatePartial(emptyList())
    }
  }
  "keeps db categories untouched when backup is empty" {
    interactor.restoreCategories(emptyList())

    coVerify { categoryRepository wasNot Called }
  }
  "restores backup categories (no categories on db)" {
    val backupCategories = listOf(
      CategoryProto(
        name = "cat1",
        order = 0
      ),
      CategoryProto(
        name = "cat2",
        order = 1
      )
    )

    interactor.restoreCategories(backupCategories)

    coVerify { categoryRepository.insert(backupCategories.map { it.toDomain() }) }
  }
  "restores backup categories (with missing categories on db)" {
    val backupCategories = listOf(
      CategoryProto(name = "cat1", order = 0),
      CategoryProto(name = "cat2", order = 1)
    )
    coEvery { categoryRepository.findAll() } returns listOf(
      Category(id = 1, name = "cat1", order = 0),
      Category(id = 3, name = "cat3", order = 1)
    )

    interactor.restoreCategories(backupCategories)

    coVerify { categoryRepository.insert(listOf(Category(name = "cat2", order = 2))) }
  }
  "restores backup categories (with all categories on db)" {
    val backupCategories = listOf(
      CategoryProto(name = "cat1", order = 0),
      CategoryProto(name = "cat2", order = 1)
    )
    coEvery { categoryRepository.findAll() } returns listOf(
      Category(id = 1, name = "cat1", order = 0),
      Category(id = 2, name = "cat2", order = 1),
      Category(id = 3, name = "cat3", order = 2)
    )

    interactor.restoreCategories(backupCategories)

    coVerify(exactly = 0) { categoryRepository.insert(any<List<Category>>()) }
  }
  "keeps db categories of manga when backup is empty" {
    interactor.restoreCategoriesOfManga(1, emptyList())

    coVerify { mangaCategoryRepository wasNot Called }
  }
  "restores backup manga categories" {
    interactor.restoreCategoriesOfManga(1, listOf(2, 4))

    coVerify {
      val expected = listOf(MangaCategory(1, 2), MangaCategory(1, 4))
      mangaCategoryRepository.replaceAll(expected)
    }
  }
  "keeps db tracks if backup is empty" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      tracks = listOf()
    )

    interactor.restoreTracks(backupManga, 1)

    coVerify { trackRepository wasNot Called }
  }
  "restores backup tracks (no tracks on db)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      tracks = listOf(
        TrackProto(siteId = 1, entryId = 1, status = 1),
        TrackProto(siteId = 2, entryId = 1, status = 1)
      )
    )
    coEvery { trackRepository.findAllForManga(1) } returns listOf()

    interactor.restoreTracks(backupManga, 1)

    coVerify { trackRepository.insert(backupManga.tracks.map { it.toDomain(1) }) }
  }
  "restores backup tracks (with tracks on db)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      tracks = listOf(
        TrackProto(siteId = 1, entryId = 1, status = 1),
        TrackProto(siteId = 2, entryId = 1, status = 1)
      )
    )
    coEvery { trackRepository.findAllForManga(1) } returns listOf(
      Track(id = 1, mangaId = 1, siteId = 1, entryId = 1)
    )

    interactor.restoreTracks(backupManga, 1)

    coVerify { trackRepository.insert(match<List<Track>> { it.size == 1 }) }
    coVerify(exactly = 0) { trackRepository.updatePartial(any<List<TrackUpdate>>()) }
  }
  "restores backup tracks (with tracks on db, updates existing)" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1",
      tracks = listOf(
        TrackProto(siteId = 1, entryId = 1, status = 1, lastRead = 3f)
      )
    )
    coEvery { trackRepository.findAllForManga(1) } returns listOf(
      Track(id = 1, mangaId = 1, siteId = 1, entryId = 1)
    )

    interactor.restoreTracks(backupManga, 1)

    coVerify { trackRepository.updatePartial(match<List<TrackUpdate>> { it.size == 1 }) }
  }

})
