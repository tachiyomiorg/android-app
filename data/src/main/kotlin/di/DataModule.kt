/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import tachiyomi.core.db.Transaction
import tachiyomi.data.AppDatabase
import tachiyomi.data.catalog.installer.AndroidCatalogInstallationReceiver
import tachiyomi.data.catalog.installer.AndroidCatalogInstaller
import tachiyomi.data.catalog.installer.AndroidCatalogLoader
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.data.catalog.prefs.CatalogPreferencesProvider
import tachiyomi.data.catalog.repository.CatalogRemoteRepositoryImpl
import tachiyomi.data.library.prefs.LibraryPreferencesProvider
import tachiyomi.data.library.repository.CategoryRepositoryImpl
import tachiyomi.data.library.repository.LibraryCoversImpl
import tachiyomi.data.library.repository.LibraryRepositoryImpl
import tachiyomi.data.library.repository.MangaCategoryRepositoryImpl
import tachiyomi.data.library.updater.LibraryUpdateSchedulerImpl
import tachiyomi.data.manga.repository.ChapterRepositoryImpl
import tachiyomi.data.manga.repository.MangaRepositoryImpl
import tachiyomi.data.sync.api.SyncDeviceAndroid
import tachiyomi.data.sync.prefs.SyncPreferencesProvider
import tachiyomi.domain.catalog.repository.CatalogInstallationReceiver
import tachiyomi.domain.catalog.repository.CatalogInstaller
import tachiyomi.domain.catalog.repository.CatalogLoader
import tachiyomi.domain.catalog.repository.CatalogRemoteRepository
import tachiyomi.domain.catalog.repository.CatalogStore
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.library.repository.MangaCategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdateScheduler
import tachiyomi.domain.manga.repository.ChapterRepository
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.sync.api.SyncDevice
import tachiyomi.domain.sync.prefs.SyncPreferences
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

val DataModule = module {

  bind<AppDatabase>().toProvider(RoomDatabaseProvider::class).providesSingleton()
  bind<Transaction>().toClass<RoomTransaction>()

  bind<MangaRepository>().toClass<MangaRepositoryImpl>().singleton()

  bind<ChapterRepository>().toClass<ChapterRepositoryImpl>().singleton()

  bind<CategoryRepository>().toClass<CategoryRepositoryImpl>().singleton()
  bind<MangaCategoryRepository>().toClass<MangaCategoryRepositoryImpl>().singleton()

  bind<LibraryRepository>().toClass<LibraryRepositoryImpl>().singleton()
  bind<LibraryPreferences>().toProvider(LibraryPreferencesProvider::class).providesSingleton()
  bind<LibraryCovers>().toClass<LibraryCoversImpl>().singleton()
  bind<LibraryUpdateScheduler>().toClass<LibraryUpdateSchedulerImpl>().singleton()

  bind<SyncPreferences>().toProvider(SyncPreferencesProvider::class).providesSingleton()
  bind<SyncDevice>().toClass<SyncDeviceAndroid>().singleton()

  bind<CatalogRemoteRepository>().toClass<CatalogRemoteRepositoryImpl>().singleton()
  bind<CatalogPreferences>().toProvider(CatalogPreferencesProvider::class).providesSingleton()
  bind<CatalogInstaller>().toClass<AndroidCatalogInstaller>().singleton()
  bind<CatalogStore>().singleton()
  bind<CatalogInstallationReceiver>().toClass<AndroidCatalogInstallationReceiver>()
  bind<CatalogLoader>().toClass<AndroidCatalogLoader>()

}
