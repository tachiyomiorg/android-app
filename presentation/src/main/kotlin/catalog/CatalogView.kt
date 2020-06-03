/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.onDispose
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.gesture.longPressGestureFilter
import androidx.ui.core.tag
import androidx.ui.foundation.Box
import androidx.ui.foundation.HorizontalScroller
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.clickable
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.Column
import androidx.ui.layout.ConstraintLayout
import androidx.ui.layout.ConstraintSet
import androidx.ui.layout.Row
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.height
import androidx.ui.layout.padding
import androidx.ui.layout.size
import androidx.ui.layout.widthIn
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.GetApp
import androidx.ui.material.icons.filled.Settings
import androidx.ui.res.stringResource
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.text.withStyle
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import tachiyomi.core.di.AppScope
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogBundled
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.R
import tachiyomi.ui.coil.CoilImage
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CatalogScreen() {
  val presenter = remember { AppScope.getInstance<CatalogsPresenter>() }
  onDispose { presenter.destroy() }

  val state = presenter.state()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.label_catalogues)) }
    )
    VerticalScroller {
      Column {
        val currState = state.value

        val mediumTextEmphasis = EmphasisAmbient.current.medium
          .applyEmphasis(contentColor())

        if (currState.updatableCatalogs.isNotEmpty() || currState.localCatalogs.isNotEmpty()) {
          Text(
            "Installed",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
          )
        }
        if (currState.updatableCatalogs.isNotEmpty()) {
          Text(
            "Update available (${currState.updatableCatalogs.size})",
            style = MaterialTheme.typography.subtitle1,
            color = mediumTextEmphasis,
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
          )

          for (catalog in currState.updatableCatalogs) {
            CatalogItem(presenter, catalog, state, hasUpdate = true)
          }
        }
        if (currState.localCatalogs.isNotEmpty()) {
          if (currState.updatableCatalogs.isNotEmpty()) {
            Text(
              "Up to date",
              style = MaterialTheme.typography.subtitle1,
              color = mediumTextEmphasis,
              modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
            )
          }
          for (catalog in currState.localCatalogs) {
            CatalogItem(presenter, catalog, state)
          }
        }
        if (currState.remoteCatalogs.isNotEmpty()) {
          Text(
            "Available",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
          )

          HorizontalScroller {
            Row(modifier = Modifier.padding(8.dp)) {
              for (choice in currState.languageChoices) {
                LanguageChip(
                  choice = choice,
                  selectedChoice = currState.languageChoice,
                  onClick = { presenter.setLanguageChoice(choice) }
                )
              }
            }
          }

          for (catalog in currState.remoteCatalogs) {
            CatalogItem(presenter, catalog, state)
          }
        }
      }
    }
  }
}

@Composable
fun LanguageChip(choice: LanguageChoice, selectedChoice: LanguageChoice, onClick: () -> Unit) {
  Surface(
    color = if (selectedChoice == choice) {
      MaterialTheme.colors.primary
    } else {
      MaterialTheme.colors.onSurface.copy(alpha = 0.25f)
    },
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.widthIn(minWidth = 56.dp).height(40.dp).padding(4.dp)
      .clickable(onClick = onClick)
  ) {
    val text = when (choice) {
      LanguageChoice.All -> stringResource(R.string.lang_all)
      is LanguageChoice.One -> choice.language.toEmoji() ?: ""
      is LanguageChoice.Others -> stringResource(R.string.lang_others)
    }
    // TODO wait for EmojiCompat support
    Text(
      text,
      modifier = Modifier.wrapContentSize(Alignment.Center),
      color = if (selectedChoice == choice) {
        MaterialTheme.colors.onPrimary
      } else {
        Color.Black
      }
    )
  }
}

@Composable
fun CatalogItem(
  presenter: CatalogsPresenter,
  catalog: Catalog,
  state: State<ViewState>,
  hasUpdate: Boolean = false
) {
  ConstraintLayout(
    constraintSet = ConstraintSet {
      val pic = tag("pic")
      val title = tag("title")
      val description = tag("description")
      val icons = tag("icons")

      pic.apply {
        width = valueFixed(48.dp)
        height = valueFixed(48.dp)
        left constrainTo parent.left
        top constrainTo parent.top
        bottom constrainTo parent.bottom
      }
      title.apply {
        width = spread
        left constrainTo pic.right
        top constrainTo parent.top
        right constrainTo icons.left
        left.margin = 12.dp
      }
      description.apply {
        width = spread
        left constrainTo title.left
        top constrainTo title.bottom
        right constrainTo parent.right
      }
      icons.apply {
        height = valueFixed(48.dp)
        top constrainTo title.top
        right constrainTo parent.right
        bottom constrainTo title.bottom
      }
    },
    modifier = Modifier.fillMaxWidth().padding(12.dp, 12.dp, 8.dp, 12.dp)
  ) {
    val mediumTextEmphasis = EmphasisAmbient.current.medium
      .applyEmphasis(contentColor())
    val title = annotatedString {
      append("${catalog.name} ")

      val versionSpan = SpanStyle(fontSize = 12.sp, color = mediumTextEmphasis)
      if (catalog is CatalogInstalled) {
        withStyle(versionSpan) { append("v${catalog.versionCode}") }
      } else if (catalog is CatalogRemote) {
        withStyle(versionSpan) { append("v${catalog.versionCode}") }
      }
    }

    Box(modifier = Modifier.tag("pic")) {
      CatalogPic(catalog)
    }
    Text(title, modifier = Modifier.tag("title"), style = MaterialTheme.typography.subtitle1)
    Text(
      catalog.description,
      modifier = Modifier.tag("description"),
      style = MaterialTheme.typography.body2,
      color = mediumTextEmphasis
    )
    Row(
      modifier = Modifier.tag("icons"),
      verticalGravity = Alignment.CenterVertically
    ) {
      val rowModifier = Modifier.size(48.dp)

      if (catalog is CatalogInstalled) {
        val installStep = state.value.installingCatalogs[catalog.pkgName]
        when {
          installStep != null && !installStep.isFinished() -> {
            CircularProgressIndicator(modifier = rowModifier + Modifier.padding(4.dp))
          }
          hasUpdate -> {
            IconButton(onClick = { presenter.installCatalog(catalog) }) {
              Image(Icons.Filled.GetApp, colorFilter = ColorFilter.tint(mediumTextEmphasis))
            }
          }
        }
      } else if (catalog is CatalogRemote) {
        val installStep = state.value.installingCatalogs[catalog.pkgName]
        if (installStep != null && !installStep.isFinished()) {
          CircularProgressIndicator(modifier = rowModifier + Modifier.padding(4.dp))
        } else {
          IconButton(onClick = { presenter.installCatalog(catalog) }) {
            Image(Icons.Filled.GetApp, colorFilter = ColorFilter.tint(mediumTextEmphasis))
          }
        }
      }
      if (catalog !is CatalogBundled) {
        IconButton(onClick = { }, modifier = Modifier.longPressGestureFilter(onLongPress = {
          if (catalog is CatalogInstalled) {
            presenter.uninstallCatalog(catalog)
          }
        })) {
          Image(Icons.Filled.Settings, colorFilter = ColorFilter.tint(mediumTextEmphasis))
        }
      }
    }
  }
}

@Composable
fun CatalogPic(catalog: Catalog) {
  when (catalog) {
    is CatalogBundled -> {
      val letter = catalog.name.take(1)
      Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape, color = Colors.get(letter)
      ) {
        Text(
          text = letter,
          color = Color.White,
          modifier = Modifier.wrapContentSize(Alignment.Center),
          style = MaterialTheme.typography.h6
        )
      }
    }
    else -> {
      CoilImage(catalog)
    }
  }
}

object Colors {
  private val colors = arrayOf(
    Color(0xffe57373),
    Color(0xfff06292),
    Color(0xffba68c8),
    Color(0xff9575cd),
    Color(0xff7986cb),
    Color(0xff64b5f6),
    Color(0xff4fc3f7),
    Color(0xff4dd0e1),
    Color(0xff4db6ac),
    Color(0xff81c784),
    Color(0xffaed581),
    Color(0xffff8a65),
    Color(0xffd4e157),
    Color(0xffffd54f),
    Color(0xffffb74d),
    Color(0xffa1887f),
    Color(0xff90a4ae)
  )

  fun get(key: Any): Color {
    return colors[abs(key.hashCode()) % colors.size]
  }

  fun random(): Color {
    return colors[Random.nextInt(colors.size)]
  }
}
