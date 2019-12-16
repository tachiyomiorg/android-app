/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.service

import android.app.Application
import tachiyomi.domain.library.service.LibraryCovers
import java.io.File
import javax.inject.Inject

internal class LibraryCoversImpl @Inject constructor(
  context: Application
) : LibraryCovers {

  private val cacheDir = File(context.filesDir, "library_covers")

  init {
    cacheDir.mkdirs()
  }

  override fun find(mangaId: Long): File {
    return File(cacheDir, "$mangaId")
  }

  override fun findCustom(mangaId: Long): File {
    return File(cacheDir, "${mangaId}_custom")
  }

  override fun delete(mangaId: Long): Boolean {
    return find(mangaId).delete()
  }

  override fun deleteCustom(mangaId: Long): Boolean {
    return findCustom(mangaId).delete()
  }

}
