/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

class InsetsChangeHandlerFrameLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ChangeHandlerFrameLayout(context, attrs, defStyleAttr) {

  init {
    fitsSystemWindows = true

    // Look for replaced views and apply the insets again.
    setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
      override fun onChildViewAdded(parent: View, child: View) {
        child.fitsSystemWindows = true
        requestApplyInsets()
      }

      override fun onChildViewRemoved(parent: View, child: View) {
      }
    })
  }

  override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
    val childCount = childCount
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      child.dispatchApplyWindowInsets(insets)
    }
    return insets
  }

}
