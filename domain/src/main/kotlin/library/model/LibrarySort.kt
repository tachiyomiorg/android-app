/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

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
    val default = LibrarySort(Type.Title, true)
  }
}

fun LibrarySort.serialize(): String {
  val type = type.name
  val order = if (isAscending) "a" else "d"
  return "$type,$order"
}

fun LibrarySort.Companion.deserialize(serialized: String): LibrarySort {
  if (serialized.isEmpty()) return default

  return try {
    val values = serialized.split(",")
    val type = enumValueOf<LibrarySort.Type>(values[0])
    val ascending = values[1] == "a"
    LibrarySort(type, ascending)
  } catch (e: Exception) {
    default
  }
}
