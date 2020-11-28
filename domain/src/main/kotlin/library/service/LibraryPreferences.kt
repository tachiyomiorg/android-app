/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.deserialize
import tachiyomi.domain.library.model.deserializeList
import tachiyomi.domain.library.model.serialize

class LibraryPreferences(private val preferenceStore: PreferenceStore) {

  fun sorting(): Preference<LibrarySort> {
    return preferenceStore.getObject(
      key = "sorting",
      defaultValue = LibrarySort(LibrarySort.Type.Title, true),
      serializer = { it.serialize() },
      deserializer = { LibrarySort.deserialize(it) }
    )
  }

  fun filters(includeAll: Boolean = false): Preference<List<LibraryFilter>> {
    return preferenceStore.getObject(
      key = "filters",
      defaultValue = emptyList(),
      serializer = { it.serialize() },
      deserializer = { LibraryFilter.deserializeList(it, includeAll) }
    )
  }

  fun lastUsedCategory(): Preference<Long> {
    return preferenceStore.getLong("last_used_category", Category.ALL_ID)
  }

  fun defaultCategory(): Preference<Long> {
    return preferenceStore.getLong("default_category", Category.UNCATEGORIZED_ID)
  }

  fun showAllCategory(): Preference<Boolean> {
    return preferenceStore.getBoolean("show_all_category", true)
  }

}
