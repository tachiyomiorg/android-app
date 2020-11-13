/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.AlertDialog
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Label
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.ui.tooling.preview.Preview
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.core.components.BackIconButton
import tachiyomi.ui.core.components.Toolbar
import tachiyomi.ui.core.viewmodel.viewModel

@Composable
fun CategoriesScreen(navController: NavHostController) {
  val vm = viewModel<CategoriesViewModel>()

  Column {
    Toolbar(
      title = { Text("Categories") },
      navigationIcon = { BackIconButton(navController) }
    )
    Box {
      LazyColumnFor(items = vm.categories, Modifier.fillMaxSize()) { category ->
        CategoryRow(category)
      }
      ExtendedFloatingActionButton(
        text = { Text(text = "Add") },
        icon = { Icon(asset = Icons.Default.Add) },
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        onClick = { vm.showCreateDialog = true }
      )
      if (vm.showCreateDialog) {
        CreateCategoryDialog(
          onDismissRequest = { vm.showCreateDialog = false },
          onCreateCategory = { vm.createCategory(it) }
        )
      }
    }
  }
}

@Composable
private fun CategoryRow(category: Category) {
  Row(
    modifier = Modifier.clickable(onClick = {}),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      asset = Icons.Default.DragHandle,
      modifier = Modifier.size(56.dp),
      tint = AmbientContentColor.current.copy(alpha = ContentAlpha.medium)
    )
    Icon(
      asset = Icons.Outlined.Label,
      tint = MaterialTheme.colors.primary
    )
    Text(
      text = category.name,
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
    )
  }
}

@Composable
private fun CreateCategoryDialog(
  onDismissRequest: () -> Unit,
  onCreateCategory: (String) -> Unit
) {
  val (categoryName, setCategoryName) = remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text("Add category") },
    text = {
      OutlinedTextField(value = categoryName, onValueChange = setCategoryName)
    },
    buttons = {
      Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismissRequest) {
          Text("Cancel")
        }
        TextButton(
          onClick = {
            onCreateCategory(categoryName)
            onDismissRequest()
          }
        ) {
          Text("Create")
        }
      }
    }
  )
}

@Preview(showDecoration = true)
@Composable
private fun CategoryRowPreview() {
  val category = Category(1, "Category name", 0, 0)
  CategoryRow(category)
}
