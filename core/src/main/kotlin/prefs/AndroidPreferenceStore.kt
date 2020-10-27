/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

/**
 * An implementation of a [PreferenceStore] backed by Android's [SharedPreferences]. All the
 * preferences are read and written from the given [preferences] instance.
 *
 * Note: there should be only one instance of this class per shared preferences file.
 */
class AndroidPreferenceStore(private val preferences: SharedPreferences) : PreferenceStore {

  private val keyChanges = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
      offer(key)
    }
    preferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose {
      preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
  }.shareIn(GlobalScope, SharingStarted.WhileSubscribed(1000))

  /**
   * Returns an [String] preference for this [key].
   */
  override fun getString(key: String, defaultValue: String): Preference<String> {
    return AndroidPreference(preferences, key, defaultValue, StringAdapter, keyChanges)
  }

  /**
   * Returns a [Long] preference for this [key].
   */
  override fun getLong(key: String, defaultValue: Long): Preference<Long> {
    return AndroidPreference(preferences, key, defaultValue, LongAdapter, keyChanges)
  }

  /**
   * Returns an [Int] preference for this [key].
   */
  override fun getInt(key: String, defaultValue: Int): Preference<Int> {
    return AndroidPreference(preferences, key, defaultValue, IntAdapter, keyChanges)
  }

  /**
   * Returns a [Float] preference for this [key].
   */
  override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
    return AndroidPreference(preferences, key, defaultValue, FloatAdapter, keyChanges)
  }

  /**
   * Returns a [Boolean] preference for this [key].
   */
  override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
    return AndroidPreference(preferences, key, defaultValue, BooleanAdapter, keyChanges)
  }

  /**
   * Returns a [Set<String>] preference for this [key].
   */
  override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
    return AndroidPreference(preferences, key, defaultValue, StringSetAdapter, keyChanges)
  }

  /**
   * Returns preference of type [T] for this [key]. The [serializer] and [deserializer] function
   * must be provided.
   */
  override fun <T> getObject(
    key: String,
    defaultValue: T,
    serializer: (T) -> String,
    deserializer: (String) -> T
  ): Preference<T> {
    val adapter = ObjectAdapter(serializer, deserializer)
    return AndroidPreference(preferences, key, defaultValue, adapter, keyChanges)
  }

}
