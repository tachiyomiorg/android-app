/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.app.initializers

import javax.inject.Inject

/**
 * List of components to initialize when the app is created. Every initializer must have a
 * constructor with the @Inject annotation and it's created on the main thread, so any expensive
 * initialization should be moved to a computation thread. Note that the components are initialized
 * sequentially in the order of the arguments.
 *
 * Reminder: Timber and RxJava should be the first ones initialized because the other dependencies
 * might use logging or the schedulers.
 */
@Suppress("UNUSED_PARAMETER")
class AppInitializers @Inject constructor(
  timberInitializer: TimberInitializer,
  catalogStoreInitializer: CatalogStoreInitializer,
  channelsInitializer: NotificationChannelsInitializer
)
