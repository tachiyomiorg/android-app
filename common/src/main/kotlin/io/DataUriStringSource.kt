/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.io

import okio.Buffer
import okio.Source
import okio.Timeout
import tachiyomi.core.util.decodeBase64

class DataUriStringSource(private val data: String) : Source {

  private val timeout = Timeout()

  private val headers = data.substringBefore(",")

  private var pos = headers.length + 1

  private val decoder: (String) -> ByteArray = if ("base64" in headers) {
    { it.decodeBase64() }
  } else {
    { it.toByteArray() }
  }

  override fun read(sink: Buffer, byteCount: Long): Long {
    if (pos >= data.length) return -1

    val charsToRead = minOf(data.length - pos, byteCount.toInt())
    val nextChars = data.substring(pos, pos + charsToRead)

    pos += charsToRead

    val decoded = decoder(nextChars)
    sink.write(decoded)

    return decoded.size.toLong()
  }

  override fun timeout(): Timeout {
    return timeout
  }

  override fun close() {
  }

}
