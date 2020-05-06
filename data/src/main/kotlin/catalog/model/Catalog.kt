/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:Suppress("PackageDirectoryMismatch")

package tachiyomi.domain.catalog.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

sealed class Catalog {
  abstract val name: String
  abstract val description: String
}

@Entity
data class CatalogRemote(
  override val name: String,
  override val description: String = "",
  @PrimaryKey @ColumnInfo(name = "id") val sourceId: Long,
  val pkgName: String,
  val versionName: String,
  val versionCode: Int,
  val lang: String,
  @ColumnInfo(name = "apkUrl") val pkgUrl: String, // TODO remove explicit columninfo later
  val iconUrl: String,
  val nsfw: Boolean
) : Catalog()
