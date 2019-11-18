/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.library_nav_category_item.*
import tachiyomi.domain.library.model.Category
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.adapter.ItemCallback
import tachiyomi.ui.category.getVisibleName
import tachiyomi.ui.theme.IconTheme
import tachiyomi.ui.util.inflate

class LibraryNavigationView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : NavigationView(context, attrs) {

  private val recycler = RecyclerView(context)

  private val adapter = Adapter()

  private val iconTheme = IconTheme(context)

  init {
    recycler.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    recycler.layoutManager = LinearLayoutManager(context)
    recycler.adapter = adapter
    addView(recycler)
  }

  fun render(categories: List<Category>) {
    adapter.submitList(categories)
  }

  interface Listener {
    fun onCategoryClick(category: Category)
  }

  inner class Adapter : BaseListAdapter<Category, CategoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
      return CategoryHolder(parent, iconTheme)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
      val category = getItem(position)
      holder.bind(category)
    }

    override fun getDiffCallback(
      oldList: List<Category>,
      newList: List<Category>
    ): DiffUtil.Callback {
      return DiffCallback(oldList, newList)
    }

  }

  class CategoryHolder(
    parent: ViewGroup,
    iconTheme: IconTheme
  ) : BaseViewHolder(parent.inflate(R.layout.library_nav_category_item)) {

    init {
      iconTheme.apply(library_nav_category_edit)
    }

    fun bind(category: Category) {
      library_nav_category_text.text = category.getVisibleName(itemView.context)
    }
  }

  private class DiffCallback(
    oldList: List<Category>,
    newList: List<Category>
  ) : ItemCallback<Category>(oldList, newList) {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem == newItem
    }

  }

}
