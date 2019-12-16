/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.flow
import tachiyomi.domain.catalog.interactor.GetLocalCatalog
import tachiyomi.domain.catalog.service.CatalogPreferences
import tachiyomi.domain.library.interactor.ChangeMangaFavorite
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.ListMangaPageFromCatalogSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SearchMangaPageFromCatalogSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.EmptySideEffect
import tachiyomi.ui.presenter.FlowSwitchSideEffect
import tachiyomi.ui.presenter.SingleSideEffect
import tachiyomi.ui.presenter.SingleSwitchSideEffect
import javax.inject.Inject

/**
 * Presenter of the catalog that interacts with the backend through use cases and also handles
 * intentions from the [CatalogBrowseController]. Any kind of update produces a [Change] which is
 * then reduced to a new [CatalogBrowseViewState]. Updates from the controller usually emit a new
 * [Action], that may also generate one or more [Change] and a new view state.
 *
 * [params] contains the required parameters of this presenter.
 * [listMangaPageFromCatalogSource] is an use case that retrieves a list of manga from a catalog
 * given a listing.
 * [searchMangaPageFromCatalogSource] is an use case that retrieves a list of manga from a catalog
 * given a list of filters.
 * [mangaInitializer] is an use case that initializes the given manga if needed.
 * [catalogPreferences] contains preferences used by the catalog.
 */
class CatalogBrowsePresenter @Inject constructor(
  private val params: CatalogBrowseParams,
  private val getLocalCatalog: GetLocalCatalog,
  private val listMangaPageFromCatalogSource: ListMangaPageFromCatalogSource,
  private val searchMangaPageFromCatalogSource: SearchMangaPageFromCatalogSource,
  private val mangaInitializer: MangaInitializer,
  private val getManga: GetManga,
  private val changeMangaFavorite: ChangeMangaFavorite,
  private val catalogPreferences: CatalogPreferences
) : BasePresenter() {

  /**
   * Last used listing preference.
   */
  private val lastListingPreference = catalogPreferences.lastListingUsed(params.sourceId)

  /**
   * Grid mode preference.
   */
  private val gridPreference = catalogPreferences.gridMode()

  private val initialState = getInitialViewState()

  /**
   * Behavior subject containing the last emitted view state.
   */
  val state = ConflatedBroadcastChannel(initialState)

  private val store = scope.createStore(
    name = "Catalog browse presenter",
    initialState = initialState,
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.offer(it) }
    store.dispatch(Action.LoadMore) // Always load the initial page
  }

  private fun getInitialViewState(): ViewState {
    // Find the requested source or early return an initial state with a not found error.
    val catalog = getLocalCatalog.get(params.sourceId)
    val source = catalog?.source as? CatalogSource
      ?: return ViewState(error = Exception("Source not found"))

    // Get the listings of the source and the initial listing and query mode to set.
    val listings = source.getListings()
    val initialListing = listings.getOrNull(lastListingPreference.get()) ?: listings.firstOrNull()
    val initialQueryMode = QueryMode.List(initialListing)

    return ViewState(
      source = source,
      queryMode = initialQueryMode,
      listings = listings,
      filters = getWrappedFilters(source),
      isGridMode = gridPreference.get()
    )
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += SingleSwitchSideEffect("Set listing mode") f@{ stateFn, action ->
      if (action !is Action.SetSearchMode.Listing) return@f null
      val type = stateFn().listings.getOrNull(action.index) ?: return@f null

      suspend {
        // Save this listing as the last used.
        lastListingPreference.set(action.index)

        // Emit an update to listing mode.
        val queryMode = QueryMode.List(type)
        Action.QueryModeUpdated(queryMode)
      }
    }

    sideEffects += SingleSwitchSideEffect("Set filters mode") f@{ _, action ->
      if (action !is Action.SetSearchMode.Filters) return@f null

      // Get the filters to apply, update their inner value and ignore the ones with the default
      // value.
      val filters = action.filters
        .asSequence()
        .onEach { it.updateInnerValue() }
        .map { it.filter }
        .filter { !it.isDefaultValue() }
        .toList()

      // Do nothing if there are no filters to apply.
      if (filters.isEmpty()) return@f null

      suspend {
        // Emit an update to search/filter mode.
        val queryMode = QueryMode.Filter(filters)
        Action.QueryModeUpdated(queryMode)
      }
    }

    sideEffects += EmptySideEffect("Swap display mode") f@{ stateFn, action ->
      if (action is Action.SwapDisplayMode) {
        suspend { gridPreference.set(stateFn().isGridMode) }
      } else {
        null
      }
    }

    sideEffects += FlowSwitchSideEffect("Load pages") f@{ stateFn, action ->
      if (!(action is Action.LoadMore || action is Action.QueryModeUpdated)) return@f null
      val state = stateFn()
      val source = state.source
      val queryMode = state.queryMode
      if (source == null || queryMode == null || state.isLoading || !state.hasMorePages)
        return@f null

      suspend {
        val nextPage = state.currentPage + 1

        flow {
          emit(Action.Loading(true, state.currentPage))

          try {
            val mangasPage = when (queryMode) {
              is QueryMode.Filter ->
                searchMangaPageFromCatalogSource.await(source, queryMode.filters, nextPage)
              is QueryMode.List ->
                listMangaPageFromCatalogSource.await(source, queryMode.listing, nextPage)
            }

            emit(Action.PageReceived(mangasPage))

            for (manga in mangasPage.mangas) {
              val initializedManga = mangaInitializer.await(source, manga) ?: continue
              emit(Action.MangaInitialized(initializedManga))
            }
          } catch (e: Exception) {
            emit(Action.LoadingError(e))
          }
        }
      }
    }

    sideEffects += SingleSideEffect("Toggle favorite") f@{ stateFn, action ->
      if (action !is Action.ToggleFavorite) return@f null

      suspend {
        changeMangaFavorite.await(action.manga)
        val updatedManga = getManga.await(action.manga.id)
        updatedManga?.let { Action.MangaInitialized(it) }
      }
    }

    return sideEffects
  }

  /**
   * Returns the filters of the given [source] wrapped in a container that allows to keep the values
   * of the filters in sync with the UI without changing their internal value.
   */
  private fun getWrappedFilters(source: CatalogSource): List<FilterWrapper<*>> {
    val filters = mutableListOf<FilterWrapper<*>>()
    source.getFilters().forEach { filter ->
      filters.add(FilterWrapper.from(filter))
      if (filter is Filter.Group) {
        val childFilters = filter.filters.map { FilterWrapper.from(it) }
        filters.addAll(childFilters)
      }
    }
    return filters
  }

  /**
   * Emits an action to request a display mode change.
   */
  fun swapDisplayMode() {
    store.dispatch(Action.SwapDisplayMode)
  }

  /**
   * Emits an action to request the next page of the catalog.
   */
  fun loadMore() {
    store.dispatch(Action.LoadMore)
  }

  /**
   * Emits an action to query the catalog with the listing at the given [index].
   */
  fun setListing(index: Int) {
    store.dispatch(Action.SetSearchMode.Listing(index))
  }

  /**
   * Emits an action to query the catalog with the given [filters].
   */
  fun setFilters(filters: List<FilterWrapper<*>>) {
    store.dispatch(Action.SetSearchMode.Filters(filters))
  }

  /**
   * Emits an action to change the favorite status of the given [manga].
   */
  fun toggleFavorite(manga: Manga) {
    store.dispatch(Action.ToggleFavorite(manga))
  }

}
