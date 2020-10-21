/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

sealed class LibraryUpdaterEvent {
  data class Progress(
    val updated: Int,
    val total: Int,
    val updating: List<LibraryManga>
  ) : LibraryUpdaterEvent()

  // TODO list of updated
  data class Completed(
    val total: Int
  ) : LibraryUpdaterEvent()
}
