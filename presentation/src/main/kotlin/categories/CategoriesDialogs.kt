/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.domain.library.model.Category

@Composable
fun CreateCategoryDialog(
  onDismissRequest: () -> Unit,
  onCreate: (String) -> Unit
) {
  val (categoryName, setCategoryName) = remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text("Create category") },
    text = {
      OutlinedTextField(value = categoryName, onValueChange = setCategoryName)
    },
    buttons = {
      ButtonsRow {
        TextButton(onClick = onDismissRequest) {
          Text("Cancel")
        }
        TextButton(onClick = {
          onCreate(categoryName)
          onDismissRequest()
        }) {
          Text("Create")
        }
      }
    }
  )
}

@Composable
fun RenameCategoryDialog(
  category: Category,
  onDismissRequest: () -> Unit,
  onRename: (String) -> Unit
) {
  val (categoryName, setCategoryName) = remember { mutableStateOf(category.name) }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text("Rename category") },
    text = {
      OutlinedTextField(value = categoryName, onValueChange = setCategoryName)
    },
    buttons = {
      ButtonsRow {
        TextButton(onClick = onDismissRequest) {
          Text("Cancel")
        }
        TextButton(onClick = {
          onRename(categoryName)
          onDismissRequest()
        }) {
          Text("Rename")
        }
      }
    }
  )
}

@Composable
fun DeleteCategoryDialog(
  category: Category,
  onDismissRequest: () -> Unit,
  onDelete: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text("Delete category") },
    text = {
      Text("Do you wish to delete the category ${category.name}?")
    },
    buttons = {
      ButtonsRow {
        TextButton(onClick = onDismissRequest) {
          Text("No")
        }
        TextButton(onClick = {
          onDelete()
          onDismissRequest()
        }) {
          Text("Yes")
        }
      }
    }
  )
}

@Composable
private fun ButtonsRow(buttons: @Composable RowScope.() -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(8.dp),
    horizontalArrangement = Arrangement.End,
    children = buttons
  )
}
