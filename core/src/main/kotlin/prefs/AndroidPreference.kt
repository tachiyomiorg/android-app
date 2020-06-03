/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import android.content.SharedPreferences
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * An implementation of [Preference] backed by Android's [SharedPreferences].
 */
internal class AndroidPreference<T>(
  private val preferences: SharedPreferences,
  private val key: String,
  private val defaultValue: T,
  private val adapter: Adapter<T>,
  private val keyChanges: BroadcastChannel<String>
) : Preference<T> {

  interface Adapter<T> {
    fun get(key: String, preferences: SharedPreferences): T

    fun set(key: String, value: T, editor: SharedPreferences.Editor)
  }

  /**
   * Returns the key of this preference.
   */
  override fun key(): String {
    return key
  }

  /**
   * Returns the current value of this preference.
   */
  override fun get(): T {
    return if (!preferences.contains(key)) {
      defaultValue
    } else {
      adapter.get(key, preferences)
    }
  }

  /**
   * Sets a new [value] for this preference.
   */
  override fun set(value: T) {
    val editor = preferences.edit()
    adapter.set(key, value, editor)
    editor.apply()
  }

  /**
   * Returns whether there's an existing entry for this preference.
   */
  override fun isSet(): Boolean {
    return preferences.contains(key)
  }

  /**
   * Deletes the entry of this preference.
   */
  override fun delete() {
    preferences.edit().remove(key).apply()
  }

  /**
   * Returns the default value of this preference
   */
  override fun defaultValue(): T {
    return defaultValue
  }

  /**
   * Returns an observer of the changes made to this preference. The current value of the preference
   * must be returned when subscribed. Callers may decide to skip this initial value with the skip
   * operator.
   */
  override fun changes(emitOnStart: Boolean): Flow<T> {
    return flow {
      val subscription = keyChanges.openSubscription()
      if (emitOnStart) {
        emit(key)
      }
      subscription.consumeEach { value ->
        emit(value)
      }
    }
      .filter { it == key }
      .map { get() }
  }

}
