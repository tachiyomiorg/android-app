/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.activity

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import tachiyomi.core.prefs.Preference
import tachiyomi.ui.core.prefs.PreferenceMutableState

open class BaseActivity : ComponentActivity() {

  fun <T> Preference<T>.asState() = PreferenceMutableState(this, lifecycleScope)

}
