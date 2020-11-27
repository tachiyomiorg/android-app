/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

import tachiyomi.domain.library.model.LibraryFilter.Completed
import tachiyomi.domain.library.model.LibraryFilter.Downloaded
import tachiyomi.domain.library.model.LibraryFilter.Unread
import tachiyomi.domain.library.model.LibraryFilterValue.Excluded
import tachiyomi.domain.library.model.LibraryFilterValue.Included
import tachiyomi.domain.library.model.LibraryFilterValue.Missing

enum class LibraryFilter {
  Downloaded,
  Unread,
  Completed;

  companion object {
    val values = values()
  }
}

enum class LibraryFilterValue {
  Included,
  Excluded,
  Missing;
}

data class LibraryFilterState(val filter: LibraryFilter, val value: LibraryFilterValue) {
  companion object
}

fun LibraryFilterState.serialize(): String? {
  val value = when (value) {
    Included -> "i"
    Excluded -> "e"
    Missing -> return null // Missing filters are not saved
  }
  val filter = when (filter) {
    Downloaded -> "Downloaded"
    Unread -> "Unread"
    Completed -> "Completed"
  }
  return "$filter:$value"
}

fun LibraryFilterState.Companion.deserialize(serialized: String): LibraryFilterState? {
  val parts = serialized.split(":")
  val filter = when (parts[0]) {
    "Downloaded" -> Downloaded
    "Unread" -> Unread
    "Completed" -> Completed
    else -> return null
  }
  val state = when (parts[1]) {
    "i" -> Included
    "e" -> Excluded
    else -> return null
  }
  return LibraryFilterState(filter, state)
}

fun List<LibraryFilterState>.serialize(): String {
  return mapNotNull { it.serialize() }.joinToString(";")
}

fun LibraryFilterState.Companion.deserializeList(
  serialized: String,
  includeAll: Boolean
): List<LibraryFilterState> {
  val savedFilters = serialized.split(";").mapNotNull { LibraryFilterState.deserialize(it) }
  return if (!includeAll) {
    savedFilters
  } else {
    LibraryFilter.values.map { filter ->
      savedFilters.find { it.filter == filter } ?: LibraryFilterState(filter, Missing)
    }
  }
}
