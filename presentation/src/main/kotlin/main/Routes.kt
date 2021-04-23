/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.main

sealed class Route(val id: String) {
  object Library : Route("library")
  object LibraryManga : Route("library/manga")

  object Updates : Route("updates")

  object History : Route("history")

  object Browse : Route("browse")
  object BrowseCatalog : Route("browse/catalog")
  object BrowseCatalogManga : Route("browse/catalog/manga")

  object More : Route("more")
  object Categories : Route("categories")
  object DownloadQueue : Route("download_queue")
  object About : Route("about")

  object Settings : Route("settings")
  object SettingsGeneral : Route("settings/general")
  object SettingsAppearance : Route("settings/appearance")
  object SettingsLibrary : Route("settings/library")
  object SettingsReader : Route("settings/reader")
  object SettingsDownloads : Route("settings/downloads")
  object SettingsTracking : Route("settings/tracking")
  object SettingsBrowse : Route("settings/browse")
  object SettingsBackup : Route("settings/backup")
  object SettingsSecurity : Route("settings/security")
  object SettingsParentalControls : Route("settings/parental_controls")
  object SettingsAdvanced : Route("settings/advanced")
}
