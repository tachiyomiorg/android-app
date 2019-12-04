/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.db

import androidx.room.TypeConverter
import tachiyomi.domain.manga.model.Genres

class MangaConverters {
  @TypeConverter
  fun toDb(genres: Genres?): String {
    return genres?.values?.joinToString(separator = ";") ?: ""
  }

  @TypeConverter
  fun fromDb(string: String): Genres {
    return Genres(*string.split(";").toTypedArray())
  }

}
