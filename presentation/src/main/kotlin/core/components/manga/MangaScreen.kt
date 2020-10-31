/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components.manga

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.model.ChapterInfo
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.coil.MangaCover
import tachiyomi.ui.core.components.LoadingScreen

@Composable
fun MangaScreen(
  navController: NavController,
  manga: Manga?,
  chapters: List<ChapterInfo> = emptyList()
) {
  if (manga == null) {
    // TODO: loading UX
    LoadingScreen()
    return
  }

  ScrollableColumn {
    MangaInfoHeader(manga)
    MangaChapters(chapters)
  }
}

@Composable
private fun MangaInfoHeader(manga: Manga) {
  Row {
    CoilImage(
      model = MangaCover.from(manga),
      modifier = Modifier
        .fillMaxWidth(0.3f)
        .aspectRatio(3f / 4f)
    )
    Column {
      Text(manga.title)
      if (manga.author.isNotBlank()) {
        Text(manga.author)
      }
      if (manga.artist.isNotBlank()) {
        Text(manga.artist)
      }
      Text(manga.status.toString())
      Text(manga.sourceId.toString())
    }
  }
}

@Composable
private fun MangaChapters(chapters: List<ChapterInfo>) {
  chapters.forEach {
    Text("${it.number}: ${it.name}")
  }
}
