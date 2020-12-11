/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing

data class LibraryFilter(val type: Type, val value: Value) {

  enum class Type {
    Unread,
    Completed,
    Downloaded;
  }

  enum class Value {
    Included,
    Excluded,
    Missing;
  }

  companion object {
    val types = Type.values()

    fun getDefault(includeAll: Boolean): List<LibraryFilter> {
      return if (includeAll) {
        types.map { LibraryFilter(it, Missing) }
      } else {
        emptyList()
      }
    }
  }
}

private fun LibraryFilter.serialize(): String? {
  val value = when (value) {
    Included -> "i"
    Excluded -> "e"
    Missing -> return null // Missing filters are not saved
  }
  val type = type.name
  return "$type,$value"
}

private fun LibraryFilter.Companion.deserialize(serialized: String): LibraryFilter? {
  return try {
    val parts = serialized.split(",")
    val type = enumValueOf<LibraryFilter.Type>(parts[0])
    val state = when (parts[1]) {
      "i" -> Included
      "e" -> Excluded
      else -> return null
    }
    LibraryFilter(type, state)
  } catch (e: Exception) {
    null
  }
}

fun List<LibraryFilter>.serialize(): String {
  return mapNotNull { it.serialize() }.joinToString(";")
}

fun LibraryFilter.Companion.deserializeList(
  serialized: String,
  includeAll: Boolean
): List<LibraryFilter> {
  val savedFilters = serialized.split(";").mapNotNull { LibraryFilter.deserialize(it) }
  return if (!includeAll) {
    savedFilters
  } else {
    types.map { type ->
      savedFilters.find { it.type == type } ?: LibraryFilter(type, Missing)
    }
  }
}
