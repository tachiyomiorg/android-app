/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate

@Dao
@TypeConverters(MangaConverters::class)
abstract class MangaDao : BaseDao<Manga> {

  @Query("SELECT * FROM manga WHERE id = :id")
  abstract fun subscribe(id: Long): Flow<Manga?>

  @Query("SELECT * FROM manga WHERE `key` = :key AND sourceId = :sourceId")
  abstract fun subscribe(key: String, sourceId: Long): Flow<Manga?>

  @Query("SELECT * FROM manga WHERE id = :id")
  abstract suspend fun find(id: Long): Manga?

  @Query("SELECT * FROM manga WHERE `key` = :key AND sourceId = :sourceId")
  abstract suspend fun find(key: String, sourceId: Long): Manga?

  @Query("""UPDATE manga SET
title = coalesce(:title, title),
sourceId = coalesce(:sourceId, sourceId),
`key` = coalesce(:key, `key`),
artist = coalesce(:artist, artist),
author = coalesce(:author, author),
description = coalesce(:description, description),
genres = coalesce(:genres, genres),
status = coalesce(:status, status),
cover = coalesce(:cover, cover),
favorite = coalesce(:favorite, favorite),
lastUpdate = coalesce(:lastUpdate, lastUpdate),
lastInit = coalesce(:lastInit, lastInit),
dateAdded = coalesce(:dateAdded, dateAdded),
viewer = coalesce(:viewer, viewer),
flags = coalesce(:flags, flags)
WHERE id = :id"""
  )
  abstract suspend fun update(
    id: Long,
    sourceId: Long? = null,
    key: String? = null,
    title: String? = null,
    artist: String? = null,
    author: String? = null,
    description: String? = null,
    genres: String? = null,
    status: Int? = null,
    cover: String? = null,
    favorite: Boolean? = null,
    lastUpdate: Long? = null,
    lastInit: Long? = null,
    dateAdded: Long? = null,
    viewer: Int? = null,
    flags: Int? = null
  )

  suspend fun update(update: MangaUpdate) {
    update(
      id = update.id,
      sourceId = update.sourceId,
      key = update.key,
      title = update.title,
      artist = update.artist,
      author = update.author,
      description = update.description,
      genres = MangaConverters.toDb(update.genres),
      status = update.status,
      cover = update.cover,
      favorite = update.favorite,
      lastUpdate = update.lastUpdate,
      lastInit = update.lastInit,
      dateAdded = update.dateAdded,
      viewer = update.viewer,
      flags = update.flags
    )
  }

  @Query("DELETE FROM manga WHERE favorite = 0")
  abstract fun deleteNonFavorite()

}
