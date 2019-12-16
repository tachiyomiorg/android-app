/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import tachiyomi.core.di.AppScope
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryUpdater
import tachiyomi.domain.library.service.LibraryUpdaterNotification
import tachiyomi.ui.R
import javax.inject.Inject

class LibraryUpdaterNotificationImpl @Inject constructor(
  private val context: Application,
  private val notificationManager: NotificationManager
) : LibraryUpdaterNotification {

  private val channel = "library" // TODO inject value or retrieve from constants

  private val ongoingId = 1

  private val cancelIntent = PendingIntent.getBroadcast(context, 0,
    Intent(context, CancelReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT
  )

  private val notificationBuilder = NotificationCompat.Builder(context, channel)
    .setContentTitle(context.getString(R.string.app_name))
    .setSmallIcon(R.drawable.ic_refresh_white_24dp)
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    .addAction(0, context.getString(android.R.string.cancel), cancelIntent)

  private var _currentNotification: Notification? = null

  val currentNotification: Notification get() = _currentNotification ?: notificationBuilder.build()

  private val serviceChannel = BroadcastChannel<Boolean>(1)

  init {
    GlobalScope.launch(Dispatchers.Main) {
      serviceChannel.asFlow()
        .debounce(250)
        .distinctUntilChanged()
        .collect { start ->
          if (start) {
            ContextCompat.startForegroundService(context, getIntent(context))
          } else {
            context.stopService(getIntent(context))
          }
        }
    }
  }

  override fun showProgress(manga: LibraryManga, current: Int, total: Int) {
    val notification = notificationBuilder
      .setContentTitle(manga.title)
      .setProgress(total, current, false)
      .build()
    _currentNotification = notification
    notificationManager.notify(ongoingId, notification)
  }

  override fun showResult(updates: List<LibraryManga>) {

  }

  override fun start() {
    serviceChannel.offer(true)
  }

  override fun end() {
    serviceChannel.offer(false)
    notificationManager.cancel(ongoingId)
    _currentNotification = null
  }

  private fun getIntent(context: Context): Intent {
    return Intent(context, Service::class.java)
  }

  class Service : android.app.Service() {

    override fun onCreate() {
      super.onCreate()
      val notifications = AppScope.getInstance<LibraryUpdaterNotification>()
      notifications as LibraryUpdaterNotificationImpl
      val notification = notifications.currentNotification
      startForeground(notifications.ongoingId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
      return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      return START_NOT_STICKY
    }

  }

  class CancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
      val updater = AppScope.getInstance<LibraryUpdater>()
      updater.cancelFirst()
    }

  }

}
