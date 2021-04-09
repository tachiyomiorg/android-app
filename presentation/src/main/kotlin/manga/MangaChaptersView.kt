/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import tachiyomi.domain.manga.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)

@Composable
fun ChapterHeader(
  chapters: List<Chapter>,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(start = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text("${chapters.size} chapters", modifier = Modifier.weight(1f))
    IconButton(onClick = { /*TODO*/ }) {
      Icon(Icons.Default.FilterList, null)
    }
  }
}

@Composable
fun ChapterRow(
  chapter: Chapter,
  isDownloaded: Boolean,
  onClick: () -> Unit,
  onDownloadClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .height(64.dp)
      .clickable(onClick = onClick)
      .padding(start = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        chapter.name,
        style = MaterialTheme.typography.body2,
        color = LocalContentColor.current.copy(
          alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
        )
      )
      val subtitleStr = buildAnnotatedString {
        if (chapter.dateUpload > 0) {
          append(dateFormat.format(Date(chapter.dateUpload)))
        }
        if (!chapter.read && chapter.progress > 0) {
          if (length > 0) append(" • ")
          append(
            AnnotatedString(
              "Page " + (chapter.progress + 1).toString(),
              SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled))
            )
          )
        }
        if (chapter.scanlator.isNotBlank()) {
          if (length > 0) append(" • ")
          append(chapter.scanlator)
        }
      }
      Text(
        subtitleStr,
        style = MaterialTheme.typography.caption,
        color = LocalContentColor.current.copy(
          alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
        )
      )
    }
    if (isDownloaded) {
      DownloadedIconButton(onDeleteClick)
    } else {
      DownloadIconButton(onDownloadClick)
    }
  }
}

@Composable
private fun DownloadIconButton(onClick: () -> Unit) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.fillMaxHeight()
  ) {
    Surface(
      shape = CircleShape,
      border = BorderStroke(2.dp, LocalContentColor.current.copy(alpha = ContentAlpha.disabled)),
    ) {
      Icon(
        Icons.Default.ArrowDownward,
        null,
        Modifier
          .size(22.dp)
          .padding(2.dp),
        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
      )
    }
  }
}

@Composable
private fun DownloadedIconButton(onClick: () -> Unit) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.fillMaxHeight()
  ) {
    Surface(shape = CircleShape, color = LocalContentColor.current) {
      Icon(
        Icons.Default.Check,
        null,
        Modifier
          .size(22.dp)
          .padding(2.dp),
        MaterialTheme.colors.surface
      )
    }
  }
}
