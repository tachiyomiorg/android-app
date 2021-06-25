/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryWithCount
import javax.inject.Inject

class GetUserCategories @Inject internal constructor(
  private val getCategoriesWithCount: GetCategoriesWithCount
) {

  fun subscribe(withAllCategory: Boolean): Flow<List<CategoryWithCount>> {
    return getCategoriesWithCount.subscribe()
      .map { categories ->
        categories.mapNotNull { categoryAndCount ->
          val (category, count) = categoryAndCount
          when (category.id) {
            // All category only shown when requested
            Category.ALL_ID -> if (withAllCategory) categoryAndCount else null

            // Uncategorized category only shown if there are entries and user categories exist
            Category.UNCATEGORIZED_ID -> {
              if (count > 0 &&
                (!withAllCategory || categories.any { !it.category.isSystemCategory })
              ) {
                categoryAndCount
              } else {
                null
              }
            }

            // User created category, always show
            else -> categoryAndCount
          }
        }
      }
      .distinctUntilChanged()
  }

}
