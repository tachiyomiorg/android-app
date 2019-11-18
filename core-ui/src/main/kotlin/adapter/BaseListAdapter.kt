/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.adapter

import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * Base adapter for RecyclerViews using [DiffUtil]. This class integrates
 * AndroidX's [AsyncListDiffer] with minor modifications to reuse our threads and allow to do
 * some extra work before latching a new list.
 */
abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder> :
  RecyclerView.Adapter<VH>(), CoroutineScope {

  /**
   * Adapter's update callback where diff results are dispatched.
   */
  @Suppress("LeakingThis")
  private val updateCallback = AdapterListUpdateCallback(this)

  /**
   * List of items contained in the adapter. Note this list isn't updated until the diff result
   * is available.
   */
  private var list: List<T>? = null
    set(value) {
      field = value
      onListUpdated()
    }

  /**
   * Non-null, unmodifiable version of [list].
   */
  var currentList: List<T> = emptyList()
    private set

  /**
   * Max generation of currently scheduled runnable.
   */
  private var maxScheduledGeneration = 0

  /**
   * Job where the coroutines of this adLifecycleObserverapter are executed. Used for cancellation.
   */
  private val job = SupervisorJob()

  /**
   * The coroutine context for this adapter.
   */
  override val coroutineContext = Dispatchers.Main + job

  /**
   * Returns the item at the given [position] or null if it's out of bounds.
   */
  fun getItemOrNull(position: Int): T? {
    return currentList.getOrNull(position)
  }

  /**
   * Returns the item at the given [position] or throws an [IndexOutOfBoundsException].
   */
  protected fun getItem(position: Int): T {
    return currentList[position]
  }

  /**
   * Returns the number of items in the adapter.
   */
  override fun getItemCount(): Int {
    return currentList.size
  }

  /**
   * Called by RecyclerView when it stops observing this adapter.
   */
  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    job.cancelChildren()
  }

  /**
   * Called when the internal list of the adapter is updated.
   */
  open fun onListUpdated() {
  }

  /**
   * Submits a new list on this adapter. The diff will be calculated on a background thread, and
   * results will be delivered through [latchList] on the main thread.
   */
  fun submitList(newList: List<T>?, forceSubmit: Boolean = false) {
    // incrementing generation means any currently-running diffs are discarded when they finish
    val runGeneration = ++maxScheduledGeneration

    if (newList === list && !forceSubmit) {
      // nothing to do (Note - still had to inc generation, since may have ongoing work)
      return
    }

    // fast simple remove all
    if (newList == null) {
      val countRemoved = list!!.size
      list = null
      currentList = emptyList()
      // notify last, after list is updated
      updateCallback.onRemoved(0, countRemoved)
      return
    }

    // fast simple first insert
    if (list == null) {
      list = newList
      currentList = Collections.unmodifiableList(newList)
      // notify last, after list is updated
      updateCallback.onInserted(0, newList.size)
      return
    }

    val oldList = list!!

    launch(start = CoroutineStart.UNDISPATCHED) {
      val result = withContext(Dispatchers.Default) {
        DiffUtil.calculateDiff(getDiffCallback(oldList, newList))
      }

      if (maxScheduledGeneration == runGeneration) {
        latchList(newList, result)
      }
    }
  }

  /**
   * Returns the callback to be used for a diff between [oldList] and [newList].
   */
  abstract fun getDiffCallback(oldList: List<T>, newList: List<T>): DiffUtil.Callback

  /**
   * Called from [submitList] when the diff results are ready. It sets the given list as primary
   * and dispatches the diff results to the adapter.
   */
  private fun latchList(newList: List<T>, diffResult: DiffUtil.DiffResult) {
    list = newList
    // notify last, after list is updated
    currentList = Collections.unmodifiableList(newList)
    diffResult.dispatchUpdatesTo(updateCallback)
  }

}
