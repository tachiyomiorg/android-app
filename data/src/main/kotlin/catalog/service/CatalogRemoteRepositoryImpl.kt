/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.service

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import tachiyomi.data.AppDatabase
import tachiyomi.data.catalog.api.CatalogGithubApi
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.service.CatalogRemoteRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class CatalogRemoteRepositoryImpl @Inject constructor(
  db: AppDatabase,
  private val api: CatalogGithubApi
) : CatalogRemoteRepository {

  private val dao = db.catalogRemote

  var remoteCatalogs = emptyList<CatalogRemote>()
    private set(value) {
      field = value
      remoteCatalogsChannel.offer(value)
    }

  private val remoteCatalogsChannel = ConflatedBroadcastChannel(remoteCatalogs)

  private var lastTimeApiChecked: Long? = null

  private var minTimeApiCheck = TimeUnit.MINUTES.toMillis(5)

  init {
    initRemoteCatalogs()
  }

  override suspend fun getRemoteCatalogs(): List<CatalogRemote> {
    return remoteCatalogs
  }

  override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
    return remoteCatalogsChannel.asFlow()
  }

  private fun initRemoteCatalogs() {
    GlobalScope.launch(Dispatchers.IO) {
      remoteCatalogs = dao.findAll()
      refreshRemoteCatalogs(false)
    }
  }

  override suspend fun refreshRemoteCatalogs(forceRefresh: Boolean) {
    val lastCheck = lastTimeApiChecked
    if (!forceRefresh && lastCheck != null &&
      lastCheck - SystemClock.elapsedRealtime() < minTimeApiCheck
    ) {
      return
    }
    lastTimeApiChecked = SystemClock.elapsedRealtime()

    val newCatalogs = api.findCatalogs()
    dao.replaceAll(newCatalogs)
    remoteCatalogs = newCatalogs
  }

}
