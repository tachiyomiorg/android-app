/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import tachiyomi.data.AppDatabase
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogRemoteRepository
import javax.inject.Inject

internal class CatalogRemoteRepositoryImpl @Inject constructor(
  db: AppDatabase
) : CatalogRemoteRepository {

  private val dao = db.catalogRemote

  private var remoteCatalogs = emptyList<CatalogRemote>()
    set(value) {
      field = value
      remoteCatalogsChannel.offer(value)
    }

  private val remoteCatalogsChannel = ConflatedBroadcastChannel(remoteCatalogs)

  private val initDeferred = CompletableDeferred<Unit>()

  init {
    initRemoteCatalogs()
  }

  override suspend fun getRemoteCatalogs(): List<CatalogRemote> {
    initDeferred.await()
    return remoteCatalogs
  }

  override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
    return remoteCatalogsChannel.asFlow()
  }

  private fun initRemoteCatalogs() {
    GlobalScope.launch(Dispatchers.IO) {
      remoteCatalogs = dao.findAll()
      initDeferred.complete(Unit)
    }
  }

  override suspend fun setRemoteCatalogs(catalogs: List<CatalogRemote>) {
    initDeferred.await()
    dao.replaceAll(catalogs)
    remoteCatalogs = catalogs
  }

}
