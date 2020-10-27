/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.model

import tachiyomi.source.Source
import java.io.File

sealed class Catalog {
  abstract val name: String
  abstract val description: String
  abstract val sourceId: Long
}

sealed class CatalogLocal : Catalog() {
  abstract val source: Source
  override val sourceId get() = source.id
}

data class CatalogBundled(
  override val source: Source,
  override val description: String = "",
  override val name: String = source.name
) : CatalogLocal()

sealed class CatalogInstalled : CatalogLocal() {
  abstract val pkgName: String
  abstract val versionName: String
  abstract val versionCode: Int

  data class SystemWide(
    override val name: String,
    override val description: String,
    override val source: Source,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int
  ) : CatalogInstalled()

  data class Locally(
    override val name: String,
    override val description: String,
    override val source: Source,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val installDir: File
  ) : CatalogInstalled()
}

data class CatalogRemote(
  override val name: String,
  override val description: String = "",
  override val sourceId: Long,
  val pkgName: String,
  val versionName: String,
  val versionCode: Int,
  val lang: String,
  val pkgUrl: String,
  val iconUrl: String,
  val nsfw: Boolean
) : Catalog()
