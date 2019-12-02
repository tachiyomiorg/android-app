/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.repository

import android.app.Application
import android.os.SystemClock
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.core.db.inTransaction
import tachiyomi.data.catalog.api.CatalogGithubApi
import tachiyomi.data.catalog.installer.AndroidCatalogInstaller
import tachiyomi.data.catalog.installer.AndroidCatalogLoader
import tachiyomi.data.catalog.sql.CatalogTable
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.repository.CatalogRemoteRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class CatalogRemoteRepositoryImpl @Inject constructor(
  private val context: Application,
  private val storio: StorIOSQLite,
  private val loader: AndroidCatalogLoader,
  private val installer: AndroidCatalogInstaller,
  private val api: CatalogGithubApi
) : CatalogRemoteRepository {

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

  override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
    return remoteCatalogsChannel.asFlow()
  }

  private fun initRemoteCatalogs() {
    GlobalScope.launch(Dispatchers.IO) {
      val catalogs = storio.get()
        .listOfObjects(CatalogRemote::class.java)
        .withQuery(Query.builder()
          .table(CatalogTable.TABLE)
          .orderBy("${CatalogTable.COL_LANG}, ${CatalogTable.COL_NAME}")
          .build())
        .prepare()
        .executeAsBlocking()

      remoteCatalogs = catalogs
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

    withContext(Dispatchers.IO) {
      val newCatalogs = api.findCatalogs()
      storio.inTransaction {
        storio.delete()
          .byQuery(DeleteQuery.builder().table(CatalogTable.TABLE).build())
          .prepare()
          .executeAsBlocking()

        storio.put()
          .objects(newCatalogs)
          .prepare()
          .executeAsBlocking()
      }
      remoteCatalogs = newCatalogs
    }
  }

}
