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
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.util.ContentLengthInputStream
import okhttp3.Call
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

internal class MangaCoverUrlFetcher(
  private val callFn: () -> Call,
  private val destFile: File? = null
) : DataFetcher<InputStream>, okhttp3.Callback {

  private var stream: InputStream? = null
  private var responseBody: ResponseBody? = null
  private var callback: DataFetcher.DataCallback<in InputStream>? = null

  @Volatile
  private var call: Call? = null

  override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
    this.callback = callback

    call = callFn()
    call?.enqueue(this)
  }

  override fun onFailure(call: Call, e: IOException) {
    callback?.onLoadFailed(e)
  }

  override fun onResponse(call: Call, response: Response) {
    val callback = callback ?: return
    responseBody = response.body
    val responseBody = responseBody ?: return
    if (response.isSuccessful) {
      try {
        val contentLength = responseBody.contentLength()
        if (destFile != null) {
          if (shouldCopyToFile(response, contentLength)) {
            val netStream =
              ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength)
            copyToFile(netStream)
          }
          stream = destFile.inputStream()
        } else {
          stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength)
        }
      } catch (e: Exception) {
        callback.onLoadFailed(e)
        return
      }
      callback.onDataReady(stream)
    } else {
      callback.onLoadFailed(HttpException(response.message, response.code))
    }
  }

  /**
   * Cover should be copied if it doesn't exist yet, server didn't reply with not modified, and
   * content-length is either unspecified by server or different from the saved cover.
   */
  private fun shouldCopyToFile(response: Response, contentLength: Long): Boolean {
    return (destFile != null && (!destFile.exists()
      || response.networkResponse?.code != HttpURLConnection.HTTP_NOT_MODIFIED))
      && (contentLength == -1L || destFile.length() != contentLength)
  }

  private fun copyToFile(netStream: InputStream) {
    val file = destFile ?: return
    file.parentFile.mkdirs()
    netStream.use {
      file.outputStream().use { output ->
        netStream.copyTo(output)
      }
    }
  }

  override fun cleanup() {
    try {
      stream?.close()
    } catch (e: IOException) {
      // Ignored
    }

    responseBody?.close()
    callback = null
  }

  override fun cancel() {
    call?.cancel()
  }

  override fun getDataClass(): Class<InputStream> {
    return InputStream::class.java
  }

  override fun getDataSource(): DataSource {
    return DataSource.LOCAL
  }
}
