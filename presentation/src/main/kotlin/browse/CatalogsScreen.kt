/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogBundled
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.R
import tachiyomi.ui.Route
import tachiyomi.ui.core.coil.CoilImage
import tachiyomi.ui.core.components.ScrollableColumn
import tachiyomi.ui.core.components.ScrollableRow
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.theme.RandomColors
import tachiyomi.ui.core.viewmodel.viewModel
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CatalogsScreen(navController: NavController) {
  val vm = viewModel<CatalogsViewModel>()

  val onClick: (Catalog) -> Unit = {
    navController.navigate("${Route.BrowseCatalog.id}/${it.sourceId}")
  }

  Scaffold(
    topBar = {
      Toolbar(
        title = { Text(stringResource(R.string.browse_label)) }
      )
    }
  ) {
    ScrollableColumn {
      if (vm.updatableCatalogs.isNotEmpty() || vm.localCatalogs.isNotEmpty()) {
        Text(
          "Installed",
          style = MaterialTheme.typography.h6,
          modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
        )
      }
      if (vm.updatableCatalogs.isNotEmpty()) {
        Text(
          "Update available (${vm.updatableCatalogs.size})",
          style = MaterialTheme.typography.subtitle1,
          color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
          modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
        )

        for (catalog in vm.updatableCatalogs) {
          CatalogItem(
            catalog = catalog,
            showInstallButton = true,
            installStep = vm.installSteps[catalog.pkgName],
            onClick = { onClick(catalog) },
            onInstall = { vm.installCatalog(catalog) },
            onUninstall = { vm.uninstallCatalog(catalog) }
          )
        }
      }
      if (vm.localCatalogs.isNotEmpty()) {
        if (vm.updatableCatalogs.isNotEmpty()) {
          Text(
            "Up to date",
            style = MaterialTheme.typography.subtitle1,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
          )
        }
        for (catalog in vm.localCatalogs) {
          CatalogItem(
            catalog = catalog,
            showInstallButton = false,
            installStep = null,
            onClick = { onClick(catalog) },
            onInstall = { vm.installCatalog(catalog) },
            onUninstall = { vm.uninstallCatalog(catalog) }
          )
        }
      }
      if (vm.remoteCatalogs.isNotEmpty()) {
        Text(
          "Available",
          style = MaterialTheme.typography.h6,
          modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
        )

        ScrollableRow(modifier = Modifier.padding(8.dp)) {
          for (choice in vm.languageChoices) {
            LanguageChip(
              choice = choice,
              isSelected = choice == vm.selectedLanguage,
              onClick = { vm.setLanguageChoice(choice) }
            )
          }
        }

        for (catalog in vm.remoteCatalogs) {
          CatalogItem(
            catalog = catalog,
            showInstallButton = true,
            installStep = vm.installSteps[catalog.pkgName],
            onClick = { onClick(catalog) },
            onInstall = { vm.installCatalog(catalog) },
            onUninstall = { vm.uninstallCatalog(catalog) }
          )
        }
      }
    }
  }
}

@Composable
fun LanguageChip(choice: LanguageChoice, isSelected: Boolean, onClick: () -> Unit) {
  Surface(
    color = if (isSelected) {
      MaterialTheme.colors.primary
    } else {
      MaterialTheme.colors.onSurface.copy(alpha = 0.25f)
    },
    modifier = Modifier.widthIn(min = 56.dp).requiredHeight(40.dp).padding(4.dp)
      .clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
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
      color = if (isSelected) {
        MaterialTheme.colors.onPrimary
      } else {
        Color.Black
      }
    )
  }
}

@Composable
fun CatalogItem(
  catalog: Catalog,
  showInstallButton: Boolean = false,
  installStep: InstallStep? = null,
  onClick: (Catalog) -> Any,
  onInstall: (Catalog) -> Unit,
  onUninstall: (Catalog) -> Unit
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
    modifier = Modifier
      .clickable(
        enabled = catalog is CatalogLocal,
        onClick = { onClick(catalog) }
      )
      .fillMaxWidth()
      .padding(12.dp, 12.dp, 8.dp, 12.dp)
  ) {
    val mediumColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
    val title = buildAnnotatedString {
      append("${catalog.name} ")
      val versionSpan = SpanStyle(fontSize = 12.sp, color = mediumColor)
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
      color = mediumColor
    )
    Row(
      modifier = Modifier.layoutId("icons"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val rowModifier = Modifier.size(48.dp)

      // Show either progress indicator or install button
      if (installStep != null && !installStep.isFinished()) {
        CircularProgressIndicator(modifier = rowModifier.then(Modifier.padding(4.dp)))
      } else if (showInstallButton) {
        IconButton(onClick = { onInstall(catalog) }) {
          Image(Icons.Filled.GetApp, colorFilter = ColorFilter.tint(mediumColor),
            contentDescription = null)
        }
      }
      if (catalog !is CatalogBundled) {
        val longPressMod = Modifier.pointerInput(Unit) {
          detectTapGestures(onLongPress = {
            (catalog as? CatalogInstalled)?.let(onUninstall)
          })
        }
        IconButton(onClick = { }, modifier = rowModifier) {
          Image(Icons.Filled.Settings, colorFilter = ColorFilter.tint(mediumColor),
            modifier = longPressMod, contentDescription = null)
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
        shape = CircleShape, color = RandomColors.get(letter)
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

@Preview
@Composable
private fun CatalogItemPreview() {
  CatalogItem(
    catalog = CatalogRemote(
      name = "My Catalog",
      sourceId = 0L,
      pkgName = "my.catalog",
      versionName = "1.0.0",
      versionCode = 1,
      lang = "en",
      pkgUrl = "",
      iconUrl = "",
      nsfw = false,
    ),
    onClick = {},
    onInstall = {},
    onUninstall = {},
  )
}