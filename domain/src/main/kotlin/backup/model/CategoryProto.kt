/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.backup.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import tachiyomi.domain.library.model.Category

@Serializable
internal data class CategoryProto(
  @ProtoId(1) val name: String,
  @ProtoId(2) val order: Int,
  @ProtoId(3) val updateInterval: Int = 0
) {

  fun toDomain(): Category {
    return Category(
      name = name,
      order = order,
      updateInterval = updateInterval
    )
  }

  companion object {
    fun fromDomain(category: Category): CategoryProto {
      return CategoryProto(
        name = category.name,
        order = category.order,
        updateInterval = category.updateInterval
      )
    }
  }

}
