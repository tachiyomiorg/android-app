/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.coil

import coil.map.Mapper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import tachiyomi.domain.catalog.model.CatalogRemote

class CatalogRemoteMapper : Mapper<CatalogRemote, HttpUrl> {

  override fun map(data: CatalogRemote): HttpUrl {
    return data.iconUrl.toHttpUrl()
  }

}
