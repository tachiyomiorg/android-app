/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import tachiyomi.data.manga.db.BaseDao
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.model.CategoryWithCount
import tachiyomi.domain.library.model.MangaCategory

@Dao
abstract class CategoryDao : BaseDao<Category> {

  @Query("SELECT * FROM category ORDER BY `order`")
  abstract fun subscribeAll(): Flow<List<Category>>

  @RawQuery(observedEntities = [Category::class, MangaCategory::class])
  protected abstract fun subscribeWithCount(query: SimpleSQLiteQuery): Flow<List<CategoryWithCount>>

  fun subscribeWithCount(): Flow<List<CategoryWithCount>> {
    // language=RoomSql
    val query = """SELECT category.*, COUNT(mangaCategory.mangaId) as mangaCount
FROM category
LEFT JOIN mangaCategory
ON category.id = mangaCategory.categoryId
WHERE category.id > 0
GROUP BY category.id
UNION ALL
SELECT *, (
  SELECT COUNT()
  FROM library
)
FROM category
WHERE category.id = ${Category.ALL_ID}
UNION ALL
SELECT *, (
  SELECT COUNT(library.id)
  FROM library
  WHERE NOT EXISTS (
    SELECT mangaCategory.mangaId
    FROM mangaCategory
    WHERE library.id = mangaCategory.mangaId
  )
)
FROM category
WHERE category.id = ${Category.UNCATEGORIZED_ID}
ORDER BY category.`order`"""

    return subscribeWithCount(SimpleSQLiteQuery(query))
  }

  @Query("SELECT * FROM category ORDER BY `order`")
  abstract suspend fun findAll(): List<Category>

  @Query("SELECT * FROM category WHERE id = :id")
  abstract suspend fun find(id: Long): Category?

  @Query("""SELECT category.* FROM category JOIN mangaCategory 
    ON category.id = mangaCategory.categoryId WHERE mangaCategory.mangaId = :mangaId""")
  abstract fun subscribeCategoriesOfManga(mangaId: Long): Flow<List<Category>>

  @Query("""SELECT category.* FROM category JOIN mangaCategory 
    ON category.id = mangaCategory.categoryId WHERE mangaCategory.mangaId = :mangaId""")
  abstract suspend fun findCategoriesOfManga(mangaId: Long): List<Category>

  @Query("""UPDATE category SET
    name = coalesce(:name, name),
    `order` = coalesce(:order, `order`),
    updateInterval = coalesce(:updateInterval, updateInterval)
    WHERE id = :id""")
  abstract suspend fun updatePartial(
    id: Long,
    name: String? = null,
    order: Int? = null,
    updateInterval: Int? = null
  )

  suspend fun updatePartial(update: CategoryUpdate) {
    updatePartial(
      id = update.id,
      name = update.name,
      order = update.order,
      updateInterval = update.updateInterval
    )
  }

  @Transaction
  open suspend fun updatePartial(updates: Collection<CategoryUpdate>) {
    for (update in updates) {
      updatePartial(update)
    }
  }

  @Query("DELETE FROM category WHERE id = :id")
  abstract suspend fun delete(id: Long)

  @Transaction
  open suspend fun delete(ids: Collection<Long>) {
    for (id in ids) {
      delete(id)
    }
  }

}
