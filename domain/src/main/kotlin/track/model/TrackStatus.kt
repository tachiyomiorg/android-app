/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.track.model

enum class TrackStatus(val value: Int) {
  Reading(1),
  Completed(2),
  OnHold(3),
  Dropped(4),
  Planned(5),
  Repeating(6);

  companion object {
    fun from(value: Int): TrackStatus {
      return checkNotNull(values.find { it.value == value }, {
        "The provided value for TrackStatus doesn't exist"
      })
    }

    private val values = values()
  }
}
