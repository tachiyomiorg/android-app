/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tachiyomi.ui.core.components.flow

import androidx.compose.ui.unit.Constraints

enum class LayoutOrientation {
  Horizontal,
  Vertical
}

data class OrientationIndependentConstraints(
  val mainAxisMin: Int,
  val mainAxisMax: Int,
  val crossAxisMin: Int,
  val crossAxisMax: Int
) {
  constructor(c: Constraints, orientation: LayoutOrientation) : this(
    if (orientation === LayoutOrientation.Horizontal) c.minWidth else c.minHeight,
    if (orientation === LayoutOrientation.Horizontal) c.maxWidth else c.maxHeight,
    if (orientation === LayoutOrientation.Horizontal) c.minHeight else c.minWidth,
    if (orientation === LayoutOrientation.Horizontal) c.maxHeight else c.maxWidth
  )

  // Creates a new instance with the same main axis constraints and maximum tight cross axis.
  fun stretchCrossAxis() = OrientationIndependentConstraints(
    mainAxisMin,
    mainAxisMax,
    if (crossAxisMax != Constraints.Infinity) crossAxisMax else crossAxisMin,
    crossAxisMax
  )

  // Given an orientation, resolves the current instance to traditional constraints.
  fun toBoxConstraints(orientation: LayoutOrientation) =
    if (orientation === LayoutOrientation.Horizontal) {
      Constraints(mainAxisMin, mainAxisMax, crossAxisMin, crossAxisMax)
    } else {
      Constraints(crossAxisMin, crossAxisMax, mainAxisMin, mainAxisMax)
    }

  // Given an orientation, resolves the max width constraint this instance represents.
  fun maxWidth(orientation: LayoutOrientation) =
    if (orientation === LayoutOrientation.Horizontal) {
      mainAxisMax
    } else {
      crossAxisMax
    }

  // Given an orientation, resolves the max height constraint this instance represents.
  fun maxHeight(orientation: LayoutOrientation) =
    if (orientation === LayoutOrientation.Horizontal) {
      crossAxisMax
    } else {
      mainAxisMax
    }
}