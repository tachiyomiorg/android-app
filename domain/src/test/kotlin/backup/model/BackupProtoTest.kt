/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

private inline fun <reified T> encodeAndDecode(obj: T): T {
  return ProtoBuf.decodeFromByteArray(ProtoBuf.encodeToByteArray(obj))
}

// We have to also test every field individually to pass test coverage
@Suppress("NO_REFLECTION_IN_CLASS_PATH")
class BackupProtoTest : FunSpec({

  test("should fail when proto models are updated to check this test") {
    MangaProto::class.constructors.first().parameters shouldHaveSize 20
    ChapterProto::class.constructors.first().parameters shouldHaveSize 12
    CategoryProto::class.constructors.first().parameters shouldHaveSize 5
    TrackProto::class.constructors.first().parameters shouldHaveSize 13
  }

  test("should convert manga from/to proto") {
    val manga = MangaProto(
      sourceId = 1,
      key = "key",
      title = "title",
      artist = "artist",
      author = "author",
      description = "description",
      genres = listOf(),
      status = 1,
      cover = "cover",
      customCover = "customCover",
      lastUpdate = 1,
      lastInit = 1,
      dateAdded = 1,
      viewer = 1,
      flags = 1,
      chapters = emptyList(),
      categories = emptyList(),
      tracks = emptyList()
    )
    val decodedManga = encodeAndDecode(manga)

    manga shouldBe decodedManga
    manga.sourceId shouldBe decodedManga.sourceId
    manga.key shouldBe decodedManga.key
    manga.title shouldBe decodedManga.title
    manga.artist shouldBe decodedManga.artist
    manga.author shouldBe decodedManga.author
    manga.description shouldBe decodedManga.description
    manga.genres shouldBe decodedManga.genres
    manga.status shouldBe decodedManga.status
    manga.cover shouldBe decodedManga.cover
    manga.customCover shouldBe decodedManga.customCover
    manga.lastUpdate shouldBe decodedManga.lastUpdate
    manga.lastInit shouldBe decodedManga.lastInit
    manga.dateAdded shouldBe decodedManga.dateAdded
    manga.viewer shouldBe decodedManga.viewer
    manga.flags shouldBe decodedManga.flags
    manga.chapters shouldBe decodedManga.chapters
    manga.categories shouldBe decodedManga.categories
    manga.tracks shouldBe decodedManga.tracks
  }

  test("should convert chapter from/to proto") {
    val chapter = ChapterProto(
      key = "key",
      name = "name",
      scanlator = "scanlator",
      read = true,
      bookmark = true,
      progress = 1,
      dateFetch = 1,
      dateUpload = 1,
      number = 1f,
      sourceOrder = 1
    )
    val decodedChapter = encodeAndDecode(chapter)

    chapter shouldBe decodedChapter
    chapter.key shouldBe decodedChapter.key
    chapter.name shouldBe decodedChapter.name
    chapter.scanlator shouldBe decodedChapter.scanlator
    chapter.read shouldBe decodedChapter.read
    chapter.bookmark shouldBe decodedChapter.bookmark
    chapter.progress shouldBe decodedChapter.progress
    chapter.dateFetch shouldBe decodedChapter.dateFetch
    chapter.dateUpload shouldBe decodedChapter.dateUpload
    chapter.number shouldBe decodedChapter.number
    chapter.sourceOrder shouldBe decodedChapter.sourceOrder
  }

  test("should convert category from/to proto") {
    val category = CategoryProto(name = "category", order = 1, updateInterval = 1)
    val decodedCategory = encodeAndDecode(category)

    category shouldBe decodedCategory
    category.name shouldBe decodedCategory.name
    category.order shouldBe decodedCategory.order
    category.updateInterval shouldBe decodedCategory.updateInterval
  }

  test("should convert track from/to proto") {
    val track = TrackProto(
      siteId = 1,
      entryId = 1,
      mediaId = 1,
      mediaUrl = "url",
      title = "title",
      lastRead = 1f,
      totalChapters = 1,
      score = 1f,
      status = 1,
      startReadTime = 1,
      endReadTime = 1
    )
    val decodedTrack = encodeAndDecode(track)

    track shouldBe decodedTrack
    track.siteId shouldBe decodedTrack.siteId
    track.entryId shouldBe decodedTrack.entryId
    track.mediaId shouldBe decodedTrack.mediaId
    track.mediaUrl shouldBe decodedTrack.mediaUrl
    track.title shouldBe decodedTrack.title
    track.lastRead shouldBe decodedTrack.lastRead
    track.totalChapters shouldBe decodedTrack.totalChapters
    track.score shouldBe decodedTrack.score
    track.status shouldBe decodedTrack.status
    track.startReadTime shouldBe decodedTrack.startReadTime
    track.endReadTime shouldBe decodedTrack.endReadTime
  }

  test("should convert backup from/to proto") {
    val backup = Backup(library = emptyList(), categories = emptyList())
    val decodedBackup = encodeAndDecode(backup)

    backup shouldBe decodedBackup
    backup.library shouldBe decodedBackup.library
    backup.categories shouldBe decodedBackup.categories
  }

})
