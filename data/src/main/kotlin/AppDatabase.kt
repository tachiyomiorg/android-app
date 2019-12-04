/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import tachiyomi.data.catalog.db.CatalogRemoteDao
import tachiyomi.data.library.db.CategoryDao
import tachiyomi.data.library.db.LibraryDao
import tachiyomi.data.library.db.MangaCategoryDao
import tachiyomi.data.manga.db.ChapterDao
import tachiyomi.data.manga.db.MangaDao
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import androidx.room.Database as DB

@DB(
  entities = [Manga::class, Chapter::class, Category::class, MangaCategory::class,
    CatalogRemote::class],
  views = [LibraryManga::class],
  version = 1
)
abstract class AppDatabase : RoomDatabase() {

  abstract val manga: MangaDao

  abstract val chapter: ChapterDao

  abstract val library: LibraryDao

  abstract val category: CategoryDao

  abstract val mangaCategory: MangaCategoryDao

  abstract val catalogRemote: CatalogRemoteDao

  companion object {
    fun build(context: Context): AppDatabase {
      return Room.databaseBuilder(context, AppDatabase::class.java, "tachiyomi")
        .openHelperFactory(RequerySQLiteOpenHelperFactory())
        .addCallback(object : RoomDatabase.Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("""INSERT INTO category VALUES (${Category.ALL_ID}, "", 0, 0)""")
            db.execSQL("""INSERT INTO category VALUES (${Category.UNCATEGORIZED_ID}, "", 0, 0)""")
            db.execSQL("""CREATE TRIGGER system_categories_deletion_trigger
              BEFORE DELETE
              ON category
              BEGIN
              SELECT CASE
              WHEN OLD.id <= 0
              THEN RAISE(ABORT, 'System category cant be deleted')
              END;
              END;""")
            db.execSQL("CREATE INDEX favorite_manga_idx ON manga(favorite) WHERE favorite = 1")
            db.execSQL("""CREATE INDEX manga_chapters_unread_index ON chapter(mangaId, read) 
              WHERE read = 0""")
          }
        })
        // TODO test transactions with IO dispatchers
        .setQueryExecutor(Dispatchers.IO.asExecutor())
        // TODO non destructive migrations
        .fallbackToDestructiveMigration()
        .build()
    }
  }

}
