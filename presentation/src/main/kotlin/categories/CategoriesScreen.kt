/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.categories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Label
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.categories.CategoriesViewModel.Dialog
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CategoriesScreen(navController: NavHostController) {
  val vm = viewModel<CategoriesViewModel>()

  Scaffold(
    topBar = {
      Toolbar(
        title = { Text("Categories") },
        navigationIcon = { BackIconButton(navController) }
      )
    }
  ) {
    Box {
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(vm.categories) { i, category ->
          CategoryRow(
            category = category,
            moveUpEnabled = i != 0,
            moveDownEnabled = i != vm.categories.lastIndex,
            onMoveUp = { vm.moveUp(category) },
            onMoveDown = { vm.moveDown(category) },
            onRename = { vm.showRenameDialog(category) },
            onDelete = { vm.showDeleteDialog(category) },
          )
        }
      }
      ExtendedFloatingActionButton(
        text = { Text(text = "Add") },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        onClick = { vm.showCreateDialog() }
      )
    }
  }

  when (val dialog = vm.dialog) {
    Dialog.Create -> {
      CreateCategoryDialog(
        onDismissRequest = { vm.dismissDialog() },
        onCreate = { vm.createCategory(it) }
      )
    }
    is Dialog.Rename -> {
      val category = dialog.category
      RenameCategoryDialog(
        category = dialog.category,
        onDismissRequest = { vm.dismissDialog() },
        onRename = { vm.renameCategory(category, it) }
      )
    }
    is Dialog.Delete -> {
      val category = dialog.category
      DeleteCategoryDialog(
        category = category,
        onDismissRequest = { vm.dismissDialog() },
        onDelete = { vm.deleteCategory(category) }
      )
    }
  }
}

@Composable
private fun CategoryRow(
  category: Category,
  moveUpEnabled: Boolean = true,
  moveDownEnabled: Boolean = true,
  onMoveUp: () -> Unit = {},
  onMoveDown: () -> Unit = {},
  onRename: () -> Unit = {},
  onDelete: () -> Unit = {},
) {
  Card(Modifier.padding(8.dp)) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Label,
          modifier = Modifier.padding(16.dp),
          tint = MaterialTheme.colors.primary,
          contentDescription = null,
        )
        Text(
          text = category.name,
          modifier = Modifier.weight(1f).padding(end = 16.dp)
        )
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          val enabledColor = LocalContentColor.current
          val disabledColor = enabledColor.copy(ContentAlpha.disabled)
          IconButton(
            onClick = onMoveUp,
            enabled = moveUpEnabled
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDropUp,
              tint = if (moveUpEnabled) enabledColor else disabledColor,
              contentDescription = null
            )
          }
          IconButton(
            onClick = onMoveDown,
            enabled = moveDownEnabled
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              tint = if (moveDownEnabled) enabledColor else disabledColor,
              contentDescription = null
            )
          }
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = onRename) {
            Icon(imageVector = Icons.Default.Edit,
              contentDescription = null)
          }
          IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete,
              contentDescription = null)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CategoryRowPreview() {
  val category = Category(1, "Category name", 0, 0)
  CategoryRow(category, false)
}
