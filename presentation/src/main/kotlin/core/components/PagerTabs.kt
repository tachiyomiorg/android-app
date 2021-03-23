/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.TabPosition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

/**
 * This indicator syncs up the tab indicator with the [HorizontalPager] position.
 */
@OptIn(ExperimentalPagerApi::class)
fun Modifier.pagerTabIndicatorOffset(
  pagerState: PagerState,
  tabPositions: List<TabPosition>,
): Modifier = composed {
  val targetIndicatorOffset: Dp
  val indicatorWidth: Dp

  val currentTab = tabPositions[pagerState.currentPage]
  val nextTab = tabPositions.getOrNull(pagerState.currentPage + 1)
  if (nextTab != null) {
    // If we have a next tab, lerp between the size and offset
    targetIndicatorOffset = lerp(currentTab.left, nextTab.left, pagerState.currentPageOffset)
    indicatorWidth = lerp(currentTab.width, nextTab.width, pagerState.currentPageOffset)
  } else {
    // Otherwise we just use the current tab/page
    targetIndicatorOffset = currentTab.left
    indicatorWidth = currentTab.width
  }

  fillMaxWidth()
    .wrapContentSize(Alignment.BottomStart)
    .offset(x = targetIndicatorOffset)
    .width(indicatorWidth)
}
