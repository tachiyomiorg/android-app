/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient

/**
 * This singleton contains the HTTP clients available to the application. It receives a [cache] for
 * temporarily storing requests, a [cookieManager] for cookies which are also persisted across
 * application restarts and a [jsFactory] in case any client needs to execute JavaScript code.
 */
class Http(cache: Cache, cookieManager: CookieManager, jsFactory: JSFactory) {

  /**
   * This is the client that will be used by default for every request.
   */
  val defaultClient = OkHttpClient.Builder()
    .cookieJar(cookieManager)
    .cache(cache)
    .build()

  /**
   * This client should be used for sites that need to bypass Cloudflare.
   */
  val cloudflareClient = defaultClient.newBuilder()
    .addInterceptor(CloudflareInterceptor(jsFactory))
    .build()

  /**
   * Cache control used to not store the response in the cache.
   */
  val noStore = CacheControl.Builder().noStore().build()

}
