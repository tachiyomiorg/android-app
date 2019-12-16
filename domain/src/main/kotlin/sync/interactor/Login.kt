/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.sync.interactor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tachiyomi.domain.sync.api.LoginResult
import tachiyomi.domain.sync.api.SyncAPI
import tachiyomi.domain.sync.service.SyncPreferences
import javax.inject.Inject

class Login @Inject constructor(
  private val syncAPI: SyncAPI,
  private val syncPreferences: SyncPreferences
) {

  suspend fun await(unsafeAddress: String, username: String, password: String): Result {
    val address = try {
      val parsed = unsafeAddress.toHttpUrlOrNull()
      if (parsed != null) {
        HttpUrl.Builder()
          .scheme(parsed.scheme)
          .host(parsed.host)
          .port(parsed.port)
          .toString()
      } else {
        val host = unsafeAddress.substringBefore(":")
        val port = unsafeAddress.substringAfter(":", "").toIntOrNull() ?: 443
        HttpUrl.Builder()
          .scheme("https")
          .host(host)
          .port(port)
          .toString()
      }
    } catch (e: Exception) {
      return Result.InvalidAddress
    }

    return when (val result = syncAPI.login(address, username, password)) {
      is LoginResult.Token -> {
        syncPreferences.address().set(address)
        syncPreferences.token().set(result.token)
        Result.Success
      }
      LoginResult.InvalidCredentials -> Result.InvalidCredentials
      is LoginResult.Error -> Result.Error(result.error)
    }
  }

  sealed class Result {
    object Success : Result()
    object InvalidAddress : Result()
    object InvalidCredentials : Result()
    data class Error(val error: Throwable) : Result()
  }

}
