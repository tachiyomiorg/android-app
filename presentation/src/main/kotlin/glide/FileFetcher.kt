/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class FileFetcher(private val file: File) : DataFetcher<InputStream> {

  /**
   * Stream of this [file].
   */
  private var stream: InputStream? = null

  /**
   * Fetch data from which a resource can be decoded.
   *
   * This will always be called on background thread so it is safe to perform long running tasks
   * here. Any third party libraries called must be thread safe (or move the work to another thread)
   * since this method will be called from a thread in a
   * [java.util.concurrent.ExecutorService]
   * that may have more than one background thread.
   *
   * You **MUST** use the [DataCallback] once the request is complete.
   *
   * You are free to move the fetch work to another thread and call the callback from there.
   *
   * This method will only be called when the corresponding resource is not in the cache.
   *
   * Note - this method will be run on a background thread so blocking I/O is safe.
   *
   * @param priority The priority with which the request should be completed.
   * @param callback The callback to use when the request is complete
   * @see .cleanup
   */
  override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
    try {
      stream = file.inputStream()
    } catch (e: FileNotFoundException) {
      callback.onLoadFailed(e)
      return
    }

    callback.onDataReady(stream)
  }

  /**
   * Cleanup or recycle any resources used by this data fetcher. This method will be called in a
   * finally block after the data provided by [.loadData] has been decoded by the
   * [com.bumptech.glide.load.ResourceDecoder].
   *
   * Note - this method will be run on a background thread so blocking I/O is safe.
   */
  override fun cleanup() {
    try {
      stream?.close()
    } catch (e: IOException) {
      // Ignored.
    }
  }

  /**
   * Returns the class of the data this fetcher will attempt to obtain.
   */
  override fun getDataClass(): Class<InputStream> {
    return InputStream::class.java
  }

  /**
   * Returns the [com.bumptech.glide.load.DataSource] this fetcher will return data from.
   */
  override fun getDataSource(): DataSource {
    return DataSource.LOCAL
  }

  /**
   * A method that will be called when a load is no longer relevant and has been cancelled. This
   * method does not need to guarantee that any in process loads do not finish. It also may be
   * called before a load starts or after it finishes.
   *
   * The best way to use this method is to cancel any loads that have not yet started, but allow
   * those that are in process to finish since its we typically will want to display the same
   * resource in a different view in the near future.
   *
   * Note - this method will be run on the main thread so it should not perform blocking
   * operations and should finish quickly.
   */
  override fun cancel() {
    // Do nothing.
  }

}
