/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import androidx.core.os.LocaleListCompat
import tachiyomi.domain.catalog.model.CatalogLocal

class UserLanguagesComparator : Comparator<Language> {

  private val userLanguages = mutableMapOf<String, Int>()

  init {
    val userLocales = LocaleListCompat.getDefault()
    val size = userLocales.size()
    for (i in 0 until size) {
      userLanguages[userLocales[i].language] = size - userLanguages.size
    }
  }

  override fun compare(langOne: Language, langTwo: Language): Int {
    val langOnePosition = userLanguages[langOne.code] ?: 0
    val langTwoPosition = userLanguages[langTwo.code] ?: 0

    return langTwoPosition.compareTo(langOnePosition)
  }

}

class InstalledLanguagesComparator(
  localCatalogs: List<CatalogLocal>
) : Comparator<Language> {

  private val preferredLanguages = localCatalogs
    .groupBy { it.source.lang }
    .mapValues { it.value.size }

  override fun compare(langOne: Language, langTwo: Language): Int {
    val langOnePosition = preferredLanguages[langOne.code] ?: 0
    val langTwoPosition = preferredLanguages[langTwo.code] ?: 0

    return langTwoPosition.compareTo(langOnePosition)
  }

}
