/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
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

class BackupManagerTest : StringSpec({

  val mangaRepository = mockk<MangaRepository>(relaxed = true)
  val categoryRepository = mockk<CategoryRepository>(relaxed = true)
  val chapterRepository = mockk<ChapterRepository>(relaxed = true)
  val trackRepository = mockk<TrackRepository>(relaxed = true)
  val mangaCategoryRepository = mockk<MangaCategoryRepository>(relaxed = true)
  val transactions = mockk<Transactions>(relaxed = true)
  val manager = BackupManager(mangaRepository, categoryRepository, chapterRepository,
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

  "dumps nothing" {
    val backup = manager.loadDump(manager.createDump())

    backup.library shouldHaveSize 0
    backup.categories shouldHaveSize 0
  }
  "dumps library" {
    coEvery { mangaRepository.findFavorites() } returns listOf(
      Manga(id = 1, sourceId = 0, key = "key1", title = "title1"),
      Manga(id = 2, sourceId = 1, key = "key2", title = "title2")
    )

    val result = manager.dumpLibrary()

    coVerify { mangaRepository.findFavorites() }
    result shouldHaveSize 2
  }
  "dumps chapters" {
    coEvery { chapterRepository.findForManga(any()) } returns listOf(
      Chapter(id = 1, mangaId = 4, key = "chapter1", name = "Chapter 1"),
      Chapter(id = 2, mangaId = 4, key = "chapter2", name = "Chapter 2"),
      Chapter(id = 3, mangaId = 4, key = "chapter3", name = "Chapter 3")
    )

    val result = manager.dumpChapters(4)

    coVerify { chapterRepository.findForManga(4) }
    result shouldHaveSize 3
  }
  "dumps categories of manga" {
    coEvery { categoryRepository.findCategoriesOfManga(any()) } returns listOf(
      Category(id = Category.ALL_ID, order = 0, updateInterval = 0),
      Category(id = Category.UNCATEGORIZED_ID, order = 1, updateInterval = 0),
      Category(id = 1, name = "cat1", order = 2, updateInterval = 0),
      Category(id = 2, name = "cat2", order = 3, updateInterval = 0)
    )

    val result = manager.dumpMangaCategories(3)

    coVerify { categoryRepository.findCategoriesOfManga(3) }
    result shouldHaveSize 2
  }
  "dumps categories" {
    coEvery { categoryRepository.findAll() } returns listOf(
      Category(id = Category.ALL_ID, order = 0, updateInterval = 0),
      Category(id = Category.UNCATEGORIZED_ID, order = 1, updateInterval = 0),
      Category(id = 1, name = "cat1", order = 2, updateInterval = 0),
      Category(id = 2, name = "cat2", order = 3, updateInterval = 0)
    )

    val result = manager.dumpCategories()

    coVerify { categoryRepository.findAll() }
    result shouldHaveSize 2
  }
  "dumps tracks" {
    coEvery { trackRepository.findAllForManga(1) } returns listOf(
      Track(id = 1, mangaId = 1, siteId = 1, entryId = 1),
      Track(id = 1, mangaId = 1, siteId = 1, entryId = 1)
    )

    val result = manager.dumpTracks(1)

    coVerify { trackRepository.findAllForManga(1) }
    result shouldHaveSize 2
  }

  "restores non existent manga" {
    val backupManga = MangaProto(
      sourceId = 1,
      key = "key1",
      title = "title 1"
    )
    coEvery { mangaRepository.find(any(), any()) } returns null

    manager.restoreManga(backupManga)

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

    manager.restoreManga(backupManga)

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

    manager.restoreManga(backupManga)

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

    manager.restoreManga(backupManga)

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

    manager.restoreChapters(backupManga)

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

    manager.restoreChapters(backupManga)

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

    manager.restoreChapters(backupManga)

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

    manager.restoreChapters(backupManga)

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

    manager.restoreChapters(backupManga)

    coVerify(exactly = 0) {
      chapterRepository.delete(any())
      chapterRepository.updatePartial(emptyList())
    }
  }
  "keeps db categories untouched when backup is empty" {
    manager.restoreCategories(emptyList())

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

    manager.restoreCategories(backupCategories)

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

    manager.restoreCategories(backupCategories)

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

    manager.restoreCategories(backupCategories)

    coVerify(exactly = 0) { categoryRepository.insert(any<List<Category>>()) }
  }
  "keeps db categories of manga when backup is empty" {
    manager.restoreCategoriesOfManga(1, emptyList())

    coVerify { mangaCategoryRepository wasNot Called }
  }
  "restores backup manga categories" {
    manager.restoreCategoriesOfManga(1, listOf(2, 4))

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

    manager.restoreTracks(backupManga, 1)

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

    manager.restoreTracks(backupManga, 1)

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

    manager.restoreTracks(backupManga, 1)

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

    manager.restoreTracks(backupManga, 1)

    coVerify { trackRepository.updatePartial(match<List<TrackUpdate>> { it.size == 1 }) }
  }

})
