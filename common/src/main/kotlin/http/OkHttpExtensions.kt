/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun OkHttpClient.get(url: String, headers: Headers? = null, cache: CacheControl? = null): Call {
  return newCall(Request.Builder()
    .get()
    .url(url)
    .apply {
      if (headers != null) headers(headers)
      if (cache != null) cacheControl(cache)
    }
    .build())
}

fun OkHttpClient.post(url: String, body: RequestBody, headers: Headers? = null): Call {
  return newCall(Request.Builder()
    .post(body)
    .url(url)
    .apply { if (headers != null) headers(headers) }
    .build())
}

suspend fun Call.awaitSuccess(): Response {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          continuation.resume(response)
        } else {
          continuation.resumeWithException(Exception("HTTP error ${response.code}"))
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })
  }
}

suspend fun Call.awaitResponse(): Response {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })
  }
}

suspend fun Call.awaitBody(): String {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          val body = response.body
          if (body == null) {
            continuation.resumeWithException(IllegalStateException("Response received null body"))
          } else {
            // TODO check if OkHttp's [cancel] propagates properly to this job
            GlobalScope.launch(Dispatchers.IO) {
              try {
                continuation.resume(body.string())
              } catch (e: Exception) {
                continuation.resumeWithException(e)
              }
            }
          }
        } else {
          continuation.resumeWithException(Exception("HTTP error ${response.code}"))
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })
  }
}

suspend fun Response.awaitBody(): String {
  return withContext(Dispatchers.IO) {
    use {
      val body = checkNotNull(body) { "Response received null body" }
      body.string()
    }
  }
}

suspend fun Response.saveTo(file: File) {
  val body = checkNotNull(body) { "Response received null body" }
  return body.saveTo(file)
}

suspend fun ResponseBody.saveTo(file: File) {
  withContext(Dispatchers.IO) {
    use {
      val source = source()
      file.sink().buffer().use { it.writeAll(source) }
    }
  }
}

/**
 * Returns a new call for this [request] that allows listening for the progress of the response
 * through a [listener].
 */
fun OkHttpClient.newCallWithProgress(request: Request, listener: ProgressListener): Call {
  val progressClient = newBuilder()
    .cache(null)
    .addNetworkInterceptor(object : Interceptor {
      override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
          .body(ProgressResponseBody(originalResponse.body!!, listener))
          .build()
      }
    })
    .build()

  return progressClient.newCall(request)
}
