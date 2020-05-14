/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.service

import java.io.File

class LibraryCovers(private val dir: File) {

  init {
    dir.mkdirs()
  }

  fun find(mangaId: Long): File {
    return File(dir, "$mangaId.0")
  }

  fun delete(mangaId: Long): Boolean {
    return find(mangaId).delete()
  }

  fun invalidate(mangaId: Long) {
    find(mangaId).setLastModified(0)
  }

}
