/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

sealed class CatalogSubheader {

  data class UpdateAvailable(val updatable: Int) : CatalogSubheader()
  object UpToDate : CatalogSubheader()

}
