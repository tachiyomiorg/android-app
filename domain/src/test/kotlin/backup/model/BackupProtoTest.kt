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
import io.kotest.matchers.equality.shouldBeEqualToUsingFields
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.reflect.full.declaredMemberProperties

private inline fun <reified T> encodeAndDecode(obj: T, withDefaults: Boolean = true): T {
  val proto = ProtoBuf { encodeDefaults = withDefaults }
  return proto.decodeFromByteArray(proto.encodeToByteArray(obj))
}

class BackupProtoTest : FunSpec({

  test("should fail when proto models are updated to check this test") {
    MangaProto::class.constructors.first().parameters shouldHaveSize 20
    ChapterProto::class.constructors.first().parameters shouldHaveSize 12
    CategoryProto::class.constructors.first().parameters shouldHaveSize 5
    TrackProto::class.constructors.first().parameters shouldHaveSize 13
  }

  test("should convert manga from/to proto") {
    val props = MangaProto::class.declaredMemberProperties.toTypedArray()
    listOf(
      MangaProto(
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
      ),
      MangaProto(
        sourceId = 1,
        key = "key",
        title = "title",
        genres = listOf("genre"),
        chapters = listOf(ChapterProto("key", "name")),
        categories = listOf(1),
        tracks = listOf(TrackProto(1, 1))
      )
    ).forEach { manga ->
      val decodedWithDefaults = encodeAndDecode(manga, true)
      val decodedWithoutDefaults = encodeAndDecode(manga, false)

      manga shouldBe decodedWithDefaults
      manga shouldBe decodedWithoutDefaults
      manga.shouldBeEqualToUsingFields(decodedWithDefaults, *props)
      manga.shouldBeEqualToUsingFields(decodedWithoutDefaults, *props)
    }
  }

  test("should convert chapter from/to proto") {
    val chapterProps = ChapterProto::class.declaredMemberProperties.toTypedArray()
    listOf(
      ChapterProto(
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
      ),
      ChapterProto("key", "name")
    ).forEach { chapter ->
      val decodedWithDefaults = encodeAndDecode(chapter, true)
      val decodedWithoutDefaults = encodeAndDecode(chapter, false)

      chapter shouldBe decodedWithDefaults
      chapter shouldBe decodedWithoutDefaults
      chapter.shouldBeEqualToUsingFields(decodedWithDefaults, *chapterProps)
      chapter.shouldBeEqualToUsingFields(decodedWithoutDefaults, *chapterProps)
    }
  }

  test("should convert category from/to proto") {
    val props = CategoryProto::class.declaredMemberProperties.toTypedArray()
    listOf(
      CategoryProto(name = "category", order = 1, updateInterval = 1),
      CategoryProto("category", 2)
    ).forEach { category ->
      val decodedWithDefaults = encodeAndDecode(category, true)
      val decodedWithoutDefaults = encodeAndDecode(category, false)

      category shouldBe decodedWithDefaults
      category shouldBe decodedWithoutDefaults
      category.shouldBeEqualToUsingFields(decodedWithDefaults, *props)
      category.shouldBeEqualToUsingFields(decodedWithoutDefaults, *props)
    }
  }

  test("should convert track from/to proto") {
    val props = TrackProto::class.declaredMemberProperties.toTypedArray()
    listOf(
      TrackProto(
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
      ),
      TrackProto(1, 1)
    ).forEach { track ->
      val decodedWithDefaults = encodeAndDecode(track, true)
      val decodedWithoutDefaults = encodeAndDecode(track, false)

      track shouldBe decodedWithDefaults
      track shouldBe decodedWithoutDefaults
      track.shouldBeEqualToUsingFields(decodedWithDefaults, *props)
      track.shouldBeEqualToUsingFields(decodedWithoutDefaults, *props)
    }
  }

  test("should convert backup from/to proto") {
    val props = Backup::class.declaredMemberProperties.toTypedArray()
    listOf(
      Backup(),
      Backup(
        library = listOf(MangaProto(1, "key", "title")),
        categories = listOf(CategoryProto("name", 1))
      )
    ).forEach { backup ->
      val decodedWithDefaults = encodeAndDecode(backup, true)
      val decodedWithoutDefaults = encodeAndDecode(backup, false)

      backup shouldBe decodedWithDefaults
      backup shouldBe decodedWithoutDefaults
      backup.shouldBeEqualToUsingFields(decodedWithDefaults, *props)
      backup.shouldBeEqualToUsingFields(decodedWithoutDefaults, *props)
    }
  }

})
