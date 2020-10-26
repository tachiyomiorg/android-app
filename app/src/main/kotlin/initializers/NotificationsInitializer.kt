/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.app.initializers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import tachiyomi.ui.library.LibraryNotifier
import javax.inject.Inject

class NotificationsInitializer @Inject constructor(
  notificationManager: NotificationManager,
  libraryNotifier: LibraryNotifier
) {

  init {
    // TODO either inject channels here or write initialization in the data & core packages
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val libraryChannel =
        NotificationChannel("library", "Library", NotificationManager.IMPORTANCE_LOW)
      notificationManager.createNotificationChannel(libraryChannel)
    }

    libraryNotifier.init()
  }

}
