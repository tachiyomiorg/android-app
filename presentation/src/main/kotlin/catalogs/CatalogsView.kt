/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.ConstraintSet
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tachiyomi.core.di.AppScope
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogBundled
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.R
import tachiyomi.ui.core.coil.CoilImage
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CatalogsScreen(navController: NavController) {
  val presenter = remember { AppScope.getInstance<CatalogsPresenter>() }
  onDispose { presenter.destroy() }

  val state = presenter.state()

  Column {
    TopAppBar(
      title = { Text(stringResource(R.string.label_catalogs)) }
    )
    ScrollableColumn {
      val currState = state.value

      val mediumTextEmphasis = AmbientEmphasisLevels.current.medium
        .applyEmphasis(AmbientContentColor.current)

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

        ScrollableRow(modifier = Modifier.padding(8.dp)) {
          for (choice in currState.languageChoices) {
            LanguageChip(
              choice = choice,
              selectedChoice = currState.languageChoice,
              onClick = { presenter.setLanguageChoice(choice) }
            )
          }
        }

        for (catalog in currState.remoteCatalogs) {
          CatalogItem(presenter, catalog, state)
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
    modifier = Modifier.widthIn(min = 56.dp).height(40.dp).padding(4.dp)
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
      val pic = createRefFor("pic")
      val title = createRefFor("title")
      val description = createRefFor("description")
      val icons = createRefFor("icons")

      constrain(pic) {
        width = Dimension.value(48.dp)
        height = Dimension.value(48.dp)
        start.linkTo(parent.start)
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
      }
      constrain(title) {
        linkTo(start = pic.end, startMargin = 12.dp, end = icons.start, bias = 0.0f)
        top.linkTo(parent.top)
      }
      constrain(description) {
        linkTo(start = title.start, end = parent.end, bias = 0.0f)
        top.linkTo(title.bottom)
      }
      constrain(icons) {
        height = Dimension.value(48.dp)
        top.linkTo(title.top)
        end.linkTo(parent.end)
        bottom.linkTo(title.bottom)
      }
    },
    modifier = Modifier.fillMaxWidth().padding(12.dp, 12.dp, 8.dp, 12.dp)
  ) {
    val mediumTextEmphasis = AmbientEmphasisLevels.current.medium
      .applyEmphasis(AmbientContentColor.current)
    val title = annotatedString {
      append("${catalog.name} ")

      val versionSpan = SpanStyle(fontSize = 12.sp, color = mediumTextEmphasis)
      if (catalog is CatalogInstalled) {
        withStyle(versionSpan) { append("v${catalog.versionCode}") }
      } else if (catalog is CatalogRemote) {
        withStyle(versionSpan) { append("v${catalog.versionCode}") }
      }
    }

    Box(modifier = Modifier.layoutId("pic")) {
      CatalogPic(catalog)
    }
    Text(title, modifier = Modifier.layoutId("title"), style = MaterialTheme.typography.subtitle1)
    Text(
      catalog.description,
      modifier = Modifier.layoutId("description"),
      style = MaterialTheme.typography.body2,
      color = mediumTextEmphasis
    )
    Row(
      modifier = Modifier.layoutId("icons"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val rowModifier = Modifier.size(48.dp)

      if (catalog is CatalogInstalled) {
        val installStep = state.value.installingCatalogs[catalog.pkgName]
        when {
          installStep != null && !installStep.isFinished() -> {
            CircularProgressIndicator(modifier = rowModifier.then(Modifier.padding(4.dp)))
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
          CircularProgressIndicator(modifier = rowModifier.then(Modifier.padding(4.dp)))
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
