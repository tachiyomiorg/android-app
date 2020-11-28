/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

import tachiyomi.domain.library.model.LibrarySort.Type.LastRead
import tachiyomi.domain.library.model.LibrarySort.Type.LastUpdated
import tachiyomi.domain.library.model.LibrarySort.Type.Source
import tachiyomi.domain.library.model.LibrarySort.Type.Title
import tachiyomi.domain.library.model.LibrarySort.Type.TotalChapters
import tachiyomi.domain.library.model.LibrarySort.Type.Unread

data class LibrarySort(val type: Type, val isAscending: Boolean) {

  enum class Type {
    Title,
    LastRead,
    LastUpdated,
    Unread,
    TotalChapters,
    Source;
  }

  companion object {
    val types = Type.values()
  }
}

fun LibrarySort.serialize(): String {
  val type = when (type) {
    Title -> "Title"
    LastRead -> "LastRead"
    LastUpdated -> "LastUpdated"
    Unread -> "Unread"
    TotalChapters -> "TotalChapters"
    Source -> "Source"
  }
  val order = if (isAscending) "a" else "d"
  return "$type,$order"
}

fun LibrarySort.Companion.deserialize(serialized: String): LibrarySort {
  if (serialized.isEmpty()) return LibrarySort(Title, true)

  val values = serialized.split(",")
  val type = values[0]
  val ascending = values[1] == "a"

  return when (type) {
    "LastRead" -> LibrarySort(LastRead, ascending)
    "LastUpdated" -> LibrarySort(LastUpdated, ascending)
    "Unread" -> LibrarySort(Unread, ascending)
    "TotalChapters" -> LibrarySort(TotalChapters, ascending)
    "Source" -> LibrarySort(Source, ascending)
    else -> LibrarySort(Title, ascending)
  }
}
