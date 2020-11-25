/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import tachiyomi.data.AppDatabase
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.model.CategoryWithCount
import tachiyomi.domain.library.service.CategoryRepository
import javax.inject.Inject

internal class CategoryRepositoryImpl @Inject constructor(
  db: AppDatabase
) : CategoryRepository {

  private val dao = db.category

  private val sharedCategories = dao.subscribeAll()
    .shareIn(GlobalScope, SharingStarted.Lazily, 1)

  override fun subscribeAll(): Flow<List<Category>> {
    return sharedCategories
  }

  override fun subscribeWithCount(): Flow<List<CategoryWithCount>> {
    return dao.subscribeWithCount()
  }

  override fun subscribeCategoriesOfManga(mangaId: Long): Flow<List<Category>> {
    return dao.subscribeCategoriesOfManga(mangaId)
  }

  override suspend fun findAll(): List<Category> {
    return sharedCategories.first()
  }

  override suspend fun find(categoryId: Long): Category? {
    return findAll().find { it.id == categoryId }
  }

  override suspend fun findCategoriesOfManga(mangaId: Long): List<Category> {
    return dao.findCategoriesOfManga(mangaId)
  }

  override suspend fun insert(category: Category) {
    dao.insert(category)
  }

  override suspend fun insert(categories: List<Category>) {
    dao.insert(categories)
  }

  override suspend fun updatePartial(update: CategoryUpdate) {
    dao.updatePartial(update)
  }

  override suspend fun updatePartial(updates: Collection<CategoryUpdate>) {
    dao.updatePartial(updates)
  }

  override suspend fun delete(categoryId: Long) {
    dao.delete(categoryId)
  }

  override suspend fun delete(categoryIds: Collection<Long>) {
    dao.delete(categoryIds)
  }

}
