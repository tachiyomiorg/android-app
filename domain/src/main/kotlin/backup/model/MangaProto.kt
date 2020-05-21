/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import tachiyomi.domain.manga.model.Manga

@Serializable
internal data class MangaProto(
  @ProtoId(1) val sourceId: Long,
  @ProtoId(2) val key: String,
  @ProtoId(3) val title: String,
  @ProtoId(4) val artist: String = "",
  @ProtoId(5) val author: String = "",
  @ProtoId(6) val description: String = "",
  @ProtoId(7) val genres: List<String> = emptyList(),
  @ProtoId(8) val status: Int = 0,
  @ProtoId(9) val cover: String = "",
  @ProtoId(10) val customCover: String = "",
  @ProtoId(11) val lastUpdate: Long = 0,
  @ProtoId(12) val lastInit: Long = 0,
  @ProtoId(13) val dateAdded: Long = 0,
  @ProtoId(14) val viewer: Int = 0,
  @ProtoId(15) val flags: Int = 0,
  @ProtoId(16) val chapters: List<ChapterProto> = emptyList(),
  @ProtoId(17) val categories: List<Int> = emptyList(),
  @ProtoId(18) val tracks: List<TrackProto> = emptyList()
) {

  fun toDomain(): Manga {
    return Manga(
      sourceId = sourceId,
      key = key,
      title = title,
      artist = artist,
      author = author,
      description = description,
      genres = genres,
      status = status,
      cover = cover,
      customCover = customCover,
      favorite = true, // If present in backup this is a favorite
      lastUpdate = lastUpdate,
      lastInit = lastInit,
      dateAdded = dateAdded,
      viewer = viewer,
      flags = flags
    )
  }

  companion object {
    fun fromDomain(
      manga: Manga,
      chapters: List<ChapterProto> = emptyList(),
      categories: List<Int> = emptyList(),
      tracks: List<TrackProto> = emptyList()
    ): MangaProto {
      return MangaProto(
        sourceId = manga.sourceId,
        key = manga.key,
        title = manga.title,
        artist = manga.artist,
        author = manga.author,
        description = manga.description,
        genres = manga.genres,
        status = manga.status,
        cover = manga.cover,
        customCover = manga.customCover,
        lastUpdate = manga.lastUpdate,
        lastInit = manga.lastInit,
        dateAdded = manga.dateAdded,
        viewer = manga.viewer,
        flags = manga.flags,
        chapters = chapters,
        categories = categories,
        tracks = tracks
      )
    }
  }

}
