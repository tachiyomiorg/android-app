/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import tachiyomi.domain.manga.model.Chapter

@Serializable
internal data class ChapterProto(
  @ProtoNumber(1) val key: String,
  @ProtoNumber(2) val name: String,
  @ProtoNumber(3) val scanlator: String = "",
  @ProtoNumber(4) val read: Boolean = false,
  @ProtoNumber(5) val bookmark: Boolean = false,
  @ProtoNumber(6) val progress: Int = 0,
  @ProtoNumber(7) val dateFetch: Long = 0,
  @ProtoNumber(8) val dateUpload: Long = 0,
  @ProtoNumber(9) val number: Float = 0f,
  @ProtoNumber(10) val sourceOrder: Int = 0
) {

  fun toDomain(mangaId: Long): Chapter {
    return Chapter(
      mangaId = mangaId,
      key = key,
      name = name,
      scanlator = scanlator,
      read = read,
      bookmark = bookmark,
      progress = progress,
      dateFetch = dateFetch,
      dateUpload = dateUpload,
      number = number,
      sourceOrder = sourceOrder
    )
  }

  companion object {
    fun fromDomain(chapter: Chapter): ChapterProto {
      return ChapterProto(
        key = chapter.key,
        name = chapter.name,
        scanlator = chapter.scanlator,
        read = chapter.read,
        bookmark = chapter.bookmark,
        progress = chapter.progress,
        dateFetch = chapter.dateFetch,
        dateUpload = chapter.dateUpload,
        number = chapter.number,
        sourceOrder = chapter.sourceOrder
      )
    }
  }

}
