/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import okhttp3.CacheControl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * An OkHttp interceptor for bypassing the Cloudflare challenge. It receives a [jsFactory] to
 * execute the JavaScript code required to solve the challenge.
 */
class CloudflareInterceptor(
  private val jsFactory: JSFactory
) : Interceptor {

  private val operationPattern = Regex(
    """setTimeout\(function\(\)\{\s+(var (?:\w,)+f.+?\r?\n[\s\S]+?a\.value =.+?)\r?\n""")

  private val passPattern = Regex("""name="pass" value="(.+?)"""")

  private val challengePattern = Regex("""name="jschl_vc" value="(\w+)"""")

  private val serverCheck = arrayOf("cloudflare-nginx", "cloudflare")

  /**
   * Intercepts a requests and checks if it should resolve the Cloudflare challenge.
   */
  @Synchronized
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    // Check if Cloudflare anti-bot is on
    if (response.code == 503 && response.header("Server") in serverCheck) {
      return chain.proceed(resolveChallenge(response))
    }

    return response
  }

  /**
   * Resolves the Cloudflare challenge that should have been provided by this [response].
   * A [CloudflareException] is thrown if there was any issue while resolving the challenge.
   */
  private fun resolveChallenge(response: Response): Request {
    jsFactory.create().use { jsClient ->
      val originalRequest = response.request
      val url = originalRequest.url
      val domain = url.host
      val content = response.body!!.string()

      // CloudFlare requires waiting 4 seconds before resolving the challenge
      Thread.sleep(4000)

      val operation = operationPattern.find(content)?.groups?.get(1)?.value
      val challenge = challengePattern.find(content)?.groups?.get(1)?.value
      val pass = passPattern.find(content)?.groups?.get(1)?.value

      if (operation == null || challenge == null || pass == null) {
        throw CloudflareException("Either operation, challenge or pass is null")
      }

      val result = try {
        val script = operation
          .replace(Regex("""a\.value = (.+ \+ t\.length).+"""), "$1")
          .replace(Regex("""\s{3,}[a-z](?: = |\.).+"""), "")
          .replace("t.length", "${domain.length}")
          .replace("\n", "")

        jsClient.evaluateAsDouble(script)
      } catch (e: Exception) {
        throw CloudflareException("Failed to resolve script with challenge", e)
      }

      val cloudflareUrl = "${url.scheme}://$domain/cdn-cgi/l/chk_jschl".toHttpUrl()
        .newBuilder()
        .addQueryParameter("jschl_vc", challenge)
        .addQueryParameter("pass", pass)
        .addQueryParameter("jschl_answer", "$result")
        .toString()

      val cloudflareHeaders = originalRequest.headers
        .newBuilder()
        .add("Referer", url.toString())
        .add("Accept", "text/html,application/xhtml+xml,application/xml")
        .add("Accept-Language", "en")
        .build()

      return GET(cloudflareUrl, cloudflareHeaders, cache = CacheControl.Builder().build())
    }
  }

}
