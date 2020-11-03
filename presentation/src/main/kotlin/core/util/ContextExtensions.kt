/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.util

import android.content.Context
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/**
 * Display a toast in this context.
 *
 * @param text the text to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, text.orEmpty(), duration).show()
}

/**
 * Opens a URL in a custom tab.
 */
fun Context.openInBrowser(url: String) {
  try {
    val intent = CustomTabsIntent.Builder()
      .build()
    intent.launchUrl(this, url.toUri())
  } catch (e: Exception) {
    toast(e.message)
  }
}
