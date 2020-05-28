/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui

import androidx.compose.Ambient
import androidx.compose.Composable
import androidx.compose.staticAmbientOf
import androidx.ui.graphics.Color
import androidx.ui.material.Emphasis
import androidx.ui.material.EmphasisLevels
import androidx.ui.material.MaterialTheme

val TextEmphasisAmbient: Ambient<EmphasisLevels> = staticAmbientOf { TextEmphasisLevels }

private object TextEmphasisLevels : EmphasisLevels {

  private class HighEmphasis(private val isLight: Boolean) : Emphasis {
    override fun applyEmphasis(color: Color) = if (isLight) {
      color.copy(alpha = 0.87f)
    } else {
      color.copy(alpha = 1f)
    }
  }

  private class MediumEmphasis(private val isLight: Boolean) : Emphasis {
    override fun applyEmphasis(color: Color) = if (isLight) {
      color.copy(alpha = 0.54f)
    } else {
      color.copy(alpha = 0.70f)
    }
  }

  private class DisabledEmphasis(private val isLight: Boolean) : Emphasis {
    override fun applyEmphasis(color: Color) = if (isLight) {
      color.copy(alpha = 0.38f)
    } else {
      color.copy(alpha = 0.50f)
    }
  }

  @Composable
  override val high: Emphasis
    get() = HighEmphasis(MaterialTheme.colors.isLight)

  @Composable
  override val medium: Emphasis
    get() = MediumEmphasis(MaterialTheme.colors.isLight)

  @Composable
  override val disabled: Emphasis
    get() = DisabledEmphasis(MaterialTheme.colors.isLight)
}
