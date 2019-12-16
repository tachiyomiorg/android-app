/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.model.CategoryWithCount

interface CategoryRepository {

  fun subscribeAll(): Flow<List<Category>>

  fun subscribeWithCount(): Flow<List<CategoryWithCount>>

  fun subscribeCategoriesOfManga(mangaId: Long): Flow<List<Category>>

  suspend fun findAll(): List<Category>

  suspend fun find(categoryId: Long): Category?

  suspend fun findCategoriesOfManga(mangaId: Long): List<Category>

  suspend fun insert(category: Category)

  suspend fun updatePartial(update: CategoryUpdate)

  suspend fun updatePartial(updates: Collection<CategoryUpdate>)

  suspend fun delete(categoryId: Long)

  suspend fun delete(categoryIds: Collection<Long>)

}
