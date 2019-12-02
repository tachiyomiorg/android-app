/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException

/**
 * A custom response body that allows listening for the progress of the original [responseBody]
 * through a [progressListener].
 */
class ProgressResponseBody(
  private val responseBody: ResponseBody,
  private val progressListener: ProgressListener
) : ResponseBody() {

  private val bufferedSource: BufferedSource by lazy {
    source(responseBody.source()).buffer()
  }

  override fun contentType(): MediaType {
    return responseBody.contentType()!!
  }

  override fun contentLength(): Long {
    return responseBody.contentLength()
  }

  override fun source(): BufferedSource {
    return bufferedSource
  }

  private fun source(source: Source): Source {
    return object : ForwardingSource(source) {
      var totalBytesRead = 0L

      @Throws(IOException::class)
      override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        // read() returns the number of bytes read, or -1 if this source is exhausted.
        totalBytesRead += if (bytesRead != -1L) bytesRead else 0
        progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
        return bytesRead
      }
    }
  }
}
