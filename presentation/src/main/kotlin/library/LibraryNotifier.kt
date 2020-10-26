/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tachiyomi.domain.library.model.LibraryUpdaterEvent
import tachiyomi.domain.library.service.LibraryUpdater
import tachiyomi.ui.R
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryNotifier @Inject constructor(
  private val application: Application,
  private val updater: LibraryUpdater,
  private val notifManager: NotificationManager
) {

  private val initialized = AtomicBoolean()

  fun init() {
    if (!initialized.compareAndSet(false, true)) return

    var started = false
    updater.running
      .onEach { running ->
        val intent = Intent(application, Service::class.java)
        if (running && !started) {
          started = true
          serviceStartedDeferred = CompletableDeferred()
          ContextCompat.startForegroundService(application, intent)
        } else if (!running && started) {
          started = false
          serviceStartedDeferred?.await()
          application.stopService(intent)
        }
      }
      .launchIn(GlobalScope)

    updater.events
      .onEach { event ->
        when (event) {
          is LibraryUpdaterEvent.Progress -> {
            val updatingText = event.updating.joinToString("\n") { it.title }
            val notification = NotificationCompat.Builder(application, "library")
              .setContentTitle("Updating library... (${event.updated}/${event.total})")
              .setStyle(NotificationCompat.BigTextStyle().bigText(updatingText))
              .setProgress(event.total, event.updated, false)
              .setSmallIcon(R.drawable.ic_launcher_background)
              .also { lastNotification = it }

            notifManager.notify(101, notification.build())
          }
          is LibraryUpdaterEvent.Completed -> {
            lastNotification = null
//            notifManager.notify(102, NotificationCompat.Builder(application, "library")
//              .setContentTitle("Updated ${event.total}")
//              .setSmallIcon(R.drawable.ic_launcher_background)
//              .build())
          }
        }
      }
      .launchIn(GlobalScope)
  }

  private companion object {
    var lastNotification: NotificationCompat.Builder? = null
    var serviceStartedDeferred: CompletableDeferred<Unit>? = null
  }

  class Service : android.app.Service() {

    override fun onCreate() {
      super.onCreate()
      val notif = lastNotification ?: NotificationCompat.Builder(this, "library")
        .setContentTitle("Updater starting...")
        .setSmallIcon(R.drawable.ic_launcher_background)

      startForeground(101, notif.build())
      serviceStartedDeferred?.complete(Unit)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
      return null
    }

  }

}
