/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.library_controller.*
import kotlinx.coroutines.flow.asFlow
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.ui.R
import tachiyomi.ui.category.CategoryController
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.controller.withHorizontalTransition
import tachiyomi.ui.glide.GlideController
import tachiyomi.ui.glide.GlideProvider
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.manga.MangaController
import tachiyomi.ui.util.itemClicks
import tachiyomi.ui.util.scanWithPrevious
import tachiyomi.ui.util.visibleIf
import tachiyomi.ui.widget.CustomViewTabLayout

class LibraryController : MvpController<LibraryPresenter>(),
  HomeChildController.FAB,
  HomeChildController.Drawer,
  GlideController,
  LibraryAdapter.Listener,
  LibraryNavigationView.Listener,
  LibrarySheetAdapter.Listener,
  LibraryChangeCategoriesDialog.Listener {

  private var adapter: LibraryCategoryAdapter? = null
  private var sheetAdapter: LibrarySheetAdapter? = null

  private var sheet: BottomSheetDialog? = null
  private var navView: LibraryNavigationView? = null

  override val glideProvider = GlideProvider.from(this)

  private var actionMode: ActionMode? = null
  private var actionModeCallback: ActionModeCallback? = null

  override fun getPresenterClass() = LibraryPresenter::class.java

  override fun getModule() = LibraryModule(this)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.library_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    library_swipe_refresh.setOnRefreshListener { onSwipeRefresh() }

    adapter = LibraryCategoryAdapter(glideProvider.get(), this)
    library_recycler.layoutManager = GridLayoutManager(view.context, 2)
    library_recycler.adapter = adapter
    library_tabs.setOnSettingsClickListener(::onCategorySettingsClick)
    library_tabs.addOnTabSelectedListener(object : CustomViewTabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: CustomViewTabLayout.Tab) {
        presenter.setSelectedCategory(tab.position)
      }

      override fun onTabUnselected(tab: CustomViewTabLayout.Tab) {}

      override fun onTabReselected(tab: CustomViewTabLayout.Tab) {}

    })

    library_toolbar.inflateMenu(R.menu.library_menu)
    library_toolbar.itemClicks().collectWithView { item ->
      when (item.itemId) {
        R.id.quick_categories_item -> onQuickCategoriesClick()
      }
    }

    sheetAdapter = LibrarySheetAdapter(this)

    presenter.state.asFlow()
      .scanWithPrevious()
      .collectWithView { (state, prevState) -> render(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    actionMode?.finish()
    sheet?.dismiss()
    sheet = null
    adapter = null
    sheetAdapter = null
    super.onDestroyView(view)
  }

  override fun createFAB(container: ViewGroup): FloatingActionButton {
    val inflater = LayoutInflater.from(container.context)
    val fab = inflater.inflate(R.layout.library_fab, container, false)
    fab.setOnClickListener { onFabClick() }
    return fab as FloatingActionButton
  }

  override fun createNavView(container: ViewGroup): View {
    val inflater = LayoutInflater.from(container.context)
    val view = inflater.inflate(R.layout.library_nav_view, container, false)
    navView = view as LibraryNavigationView
    return view
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.showQuickCategories != prevState?.showQuickCategories) {
      renderQuickCategories(state.showQuickCategories)
    }
    if (state.categories !== prevState?.categories ||
      state.showQuickCategories != prevState.showQuickCategories ||
      state.selectedCategory != prevState.selectedCategory
    ) {
      renderCategories(state)
    }
    if (state.library !== prevState?.library || state.selectedManga !== prevState.selectedManga) {
      renderLibrary(state.library, state.selectedManga)
    }
    if (state.selectedManga !== prevState?.selectedManga) {
      renderSelectedManga(state.selectedManga)
    }
    if (state.sheetVisible != prevState?.sheetVisible) {
      renderSheetVisibility(state.sheetVisible)
    }
    if (state.sheetVisible != prevState?.sheetVisible ||
      state.categories !== prevState.categories ||
      state.selectedCategory != prevState.selectedCategory ||
      state.filters !== prevState.filters ||
      state.sorting != prevState.sorting
    ) {
      renderSheetItems(state)
    }
    renderUpdatingCategory(state.showUpdatingCategory) // Changes internally checked
  }

  private fun renderQuickCategories(showQuickCategories: Boolean) {
    library_toolbar.menu.findItem(R.id.quick_categories_item)?.isChecked = showQuickCategories
    library_tabs.visibleIf { showQuickCategories }
  }

  private fun renderCategories(state: ViewState) {
    if (state.showQuickCategories) {
      library_tabs.setCategories(state.categories, state.selectedCategory?.id)
    }
    navView?.render(state.categories)
  }

  private fun renderLibrary(library: List<LibraryManga>, selectedManga: Set<Long>) {
    adapter?.submitManga(library, selectedManga)
  }

  private fun renderSelectedManga(selectedManga: Set<Long>) {
    if (selectedManga.isEmpty()) {
      actionMode?.finish()
      return
    }

    if (actionMode == null) {
      val callback = ActionModeCallback()
      actionModeCallback = callback
      actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(callback)
    }

    actionModeCallback?.render(selectedManga, actionMode)
  }

  private fun renderSheetVisibility(isVisible: Boolean) {
    if (isVisible) {
      if (sheet == null) {
        val activity = activity ?: return
        val adapter = sheetAdapter ?: return
        sheet = LibrarySheet.show(activity, adapter).apply {
          setOnDismissListener {
            if (!activity.isChangingConfigurations) {
              presenter.hideSheet()
            }
          }
        }
      }
    } else {
      sheet?.dismiss()
      sheet = null
    }
  }

  private fun renderSheetItems(state: ViewState) {
    if (state.sheetVisible) {
      sheetAdapter?.render(state.categories, state.selectedCategory, state.filters, state.sorting)
    }
  }

  private fun renderUpdatingCategory(showUpdatingCategory: Boolean) {
    library_swipe_refresh.isRefreshing = showUpdatingCategory
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  private fun onFabClick() {
    presenter.showSheet()
  }

  override fun onMangaClick(manga: LibraryManga) {
    if (actionMode == null) {
      findRootRouter().pushController(MangaController(manga.id).withHorizontalTransition())
    } else {
      onMangaLongClick(manga)
    }
  }

  override fun onMangaLongClick(manga: LibraryManga) {
    presenter.toggleMangaSelection(manga)
  }

  override fun onCategoryClick(category: Category) {
    presenter.setSelectedCategory(category)
  }

  private fun onSwipeRefresh() {
    presenter.updateSelectedCategory()
  }

  override fun onCategorySettingsClick() {
    presenter.hideSheet()
    router.pushController(CategoryController().withHorizontalTransition())
  }

  override fun onSortClick(sort: LibrarySort) {
    presenter.setSelectedSort(sort)
  }

  private fun onQuickCategoriesClick() {
    presenter.toggleQuickCategories()
  }

  private fun showSetCategoriesDialog(selectedManga: Set<Long>) {
    val categories = presenter.getCategories().takeUnless { it.isEmpty() } ?: return
    val preselected = emptyArray<Int>() // TODO
    LibraryChangeCategoriesDialog(this, selectedManga, categories, preselected).showDialog(router)
  }

  override fun updateCategoriesForMangas(
    categoryIds: Collection<Long>,
    mangaIds: Collection<Long>
  ) {
    presenter.setCategoriesForMangas(categoryIds, mangaIds)
  }

  private inner class ActionModeCallback : ActionMode.Callback {

    private var selectedManga: Set<Long> = emptySet()

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.menuInflater.inflate(R.menu.library_selection_menu, menu)
      return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.title = resources?.getString(R.string.label_selected, selectedManga.size)
      return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_set_categories -> {
          showSetCategoriesDialog(selectedManga)
        }
      }
      return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
      presenter.unselectMangas()
      actionModeCallback = null
      actionMode = null
    }

    fun render(selectedManga: Set<Long>, mode: ActionMode?) {
      this.selectedManga = selectedManga
      mode?.invalidate()
    }

  }

}
