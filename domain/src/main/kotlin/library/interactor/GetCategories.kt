/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.service.CategoryRepository
import javax.inject.Inject

class GetCategories @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun subscribe(): Flow<List<Category>> {
    return categoryRepository.subscribeAll()
  }

  suspend fun await(): List<Category> {
    return categoryRepository.findAll()
  }

}
