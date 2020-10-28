/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * An implementation of [Preference] backed by Android's [SharedPreferences].
 */
internal class AndroidPreference<T>(
  private val preferences: SharedPreferences,
  private val key: String,
  private val defaultValue: T,
  private val adapter: Adapter<T>,
  private val keyChanges: SharedFlow<String>
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
   * Returns a cold [Flow] of this preference to receive updates when its value changes.
   */
  override fun changes(): Flow<T> {
    return keyChanges
      .filter { it == key }
      .map { get() }
  }

  /**
   * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
   * current value and receive preference updates.
   */
  override fun stateIn(scope: CoroutineScope): StateFlow<T> {
    return keyChanges
      .filter { it == key }
      .map { get() }
      .stateIn(scope, SharingStarted.Eagerly, get())
  }

}
