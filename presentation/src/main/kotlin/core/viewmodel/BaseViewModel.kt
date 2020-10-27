/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.viewmodel

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseViewModel {

  protected val scope = MainScope()

  fun destroy() {
    scope.cancel()
    onDestroy()
  }

  open fun onDestroy() {
  }

}
