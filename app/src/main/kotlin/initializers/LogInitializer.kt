/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.app.initializers

import android.app.Application
import org.tinylog.configuration.Configuration
import tachiyomi.app.BuildConfig
import java.io.File
import javax.inject.Inject

class LogInitializer @Inject constructor(context: Application) {

  init {
    val configuration = if (BuildConfig.DEBUG) {
      mapOf(
        "writer" to "logcat",
        "writer.exception" to "unpack, strip: com.android.internal"
      )
    } else {
      mapOf(
        "writer" to "rolling file",
        "writer.level" to "debug",
        "writer.file" to File(context.noBackupFilesDir, "log_{date:yyyy-MM-dd}.txt").absolutePath,
        "writer.format" to "{date} [{thread}] {level}/{class-name}.{method}#{line}: {message}",
        "writer.exception" to "unpack",
        "writer.policies" to "daily",
        "writer.append" to "true",
        "writer.buffered" to "true",
        "writer.backups" to "3",
        "writingthread" to "true"
      )
    }
    Configuration.replace(configuration)
  }

}
