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
import io.kotest.matchers.shouldBe
import tachiyomi.domain.backup.model.CategoryProto
import tachiyomi.domain.backup.model.ChapterProto
import tachiyomi.domain.backup.model.MangaProto
import tachiyomi.domain.backup.model.TrackProto
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.model.TrackStatus
import tachiyomi.source.model.MangaInfo

class BackupMappersTest : StringSpec({

  "should fail when domain models are updated to maybe add new backup fields" {
    Manga::class.constructors.first().parameters shouldHaveSize 17
    Chapter::class.constructors.first().parameters shouldHaveSize 12
    Category::class.constructors.first().parameters shouldHaveSize 4
    Track::class.constructors.first().parameters shouldHaveSize 13
  }

  "should be the same manga after mapping" {
    val manga = Manga(
      id = 0, // Must be 0 because the mapper doesn't save this
      sourceId = 3,
      key = "test key",
      title = "test title",
      artist = "test artist",
      author = "test author",
      description = "test description",
      genres = listOf("test genre1", "test genre2"),
      status = MangaInfo.ONGOING,
      cover = "test cover",
      customCover = "test customCover",
      favorite = true,
      lastUpdate = 4,
      lastInit = 5,
      dateAdded = 6,
      viewer = 7,
      flags = 8
    )

    val restoredManga = MangaProto.fromDomain(manga).toDomain()

    restoredManga shouldBe manga
  }
  "should be the same chapter after mapping" {
    val chapter = Chapter(
      id = 0, // Must be 0 because the mapper doesn't save this
      mangaId = 4,
      key = "test key",
      name = "test name",
      read = true,
      bookmark = true,
      progress = 5,
      dateUpload = 6,
      dateFetch = 7,
      sourceOrder = 8,
      number = 5.5f,
      scanlator = "test scanlator"
    )

    val restoredChapter = ChapterProto.fromDomain(chapter).toDomain(4)

    restoredChapter shouldBe chapter
  }
  "should be the same category after mapping" {
    val category = Category(
      id = 0, // Must be 0 because the mapper doesn't save this
      name = "test name",
      order = 2,
      updateInterval = 3
    )

    val restoredCategory = CategoryProto.fromDomain(category).toDomain()

    restoredCategory shouldBe category
  }
  "should be the same track after mapping" {
    val track = Track(
      id = 0, // Must be 0 because the mapper doesn't save this
      mangaId = 3,
      siteId = 2,
      entryId = 3,
      mediaId = 4,
      mediaUrl = "test url",
      title = "test title",
      lastRead = 5.5f,
      totalChapters = 6,
      score = 7f,
      status = TrackStatus.Completed,
      startReadTime = 4,
      endReadTime = 5
    )

    val restoredTrack = TrackProto.fromDomain(track).toDomain(3)

    restoredTrack shouldBe track
  }

})
