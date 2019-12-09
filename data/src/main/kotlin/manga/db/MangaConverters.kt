/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.db

import androidx.room.TypeConverter

object MangaConverters {

  @JvmStatic
  @TypeConverter
  fun toDb(genres: List<String>?): String? {
    return genres?.joinToString(";")
  }

  @JvmStatic
  @TypeConverter
  fun fromDb(genres: String): List<String> {
    return genres.split(";")
  }

}
