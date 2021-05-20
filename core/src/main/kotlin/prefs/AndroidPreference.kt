/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO(inorichi): consider using the async API if possible
/**
 * An implementation of [Preference] backed by Androidx's [DataStore].
 *
 * Read operations are blocking, but writes are performed in the given [scope], which should be
 * an IO thread.
 */
internal class AndroidPreference<T>(
  private val store: DataStore<Preferences>,
  private val scope: CoroutineScope,
  private val key: Key<T>,
  private val defaultValue: T
) : Preference<T> {

  /**
   * Returns the key of this preference.
   */
  override fun key(): String {
    return key.name
  }

  /**
   * Returns the current value of this preference.
   */
  override fun get(): T {
    return runBlocking {
      store.data.first()[key] ?: defaultValue
    }
  }

  /**
   * Sets a new [value] for this preference.
   */
  override fun set(value: T) {
    scope.launch {
      store.edit { it[key] = value }
    }
  }

  /**
   * Returns whether there's an existing entry for this preference.
   */
  override fun isSet(): Boolean {
    return runBlocking {
      store.data.first().contains(key)
    }
  }

  /**
   * Deletes the entry of this preference.
   */
  override fun delete() {
    scope.launch {
      store.edit { it.remove(key) }
    }
  }

  /**
   * Returns the default value of this preference.
   */
  override fun defaultValue(): T {
    return defaultValue
  }

  /**
   * Returns a cold [Flow] of this preference to receive updates when its value changes.
   */
  override fun changes(): Flow<T> {
    return store.data
      .drop(1)
      .map { it[key] ?: defaultValue }
      .distinctUntilChanged()
  }

  /**
   * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
   * current value and receive preference updates.
   */
  override fun stateIn(scope: CoroutineScope): StateFlow<T> {
    return changes().stateIn(scope, SharingStarted.Eagerly, get())
  }

}

// TODO(inorichi): create a common wrapper for these two classes if possible.
class AndroidPreferenceObject<T>(
  private val store: DataStore<Preferences>,
  private val scope: CoroutineScope,
  private val key: Key<String>,
  private val defaultValue: T,
  private val serializer: (T) -> String,
  private val deserializer: (String) -> T
) : Preference<T> {
  override fun key(): String {
    return key.name
  }

  override fun get(): T {
    return runBlocking {
      store.data.first()[key]?.let { deserializer(it) } ?: defaultValue
    }
  }

  override fun set(value: T) {
    scope.launch {
      store.edit { it[key] = serializer(value) }
    }
  }

  override fun isSet(): Boolean {
    return runBlocking {
      store.data.first().contains(key)
    }
  }

  override fun delete() {
    scope.launch {
      store.edit { it.remove(key) }
    }
  }

  override fun defaultValue(): T {
    return defaultValue
  }

  override fun changes(): Flow<T> {
    return store.data
      .drop(1)
      .map { preferences -> preferences[key]?.let { deserializer(it) } ?: defaultValue }
      .distinctUntilChanged()
  }

  override fun stateIn(scope: CoroutineScope): StateFlow<T> {
    return changes().stateIn(scope, SharingStarted.Eagerly, get())
  }
}
