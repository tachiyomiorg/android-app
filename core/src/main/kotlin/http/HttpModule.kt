/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

/**
 * A [toothpick.Toothpick] module to register the HTTP dependencies available to the application.
 */
val HttpModule = module {

  bind<Http>().toProvider(HttpProvider::class).providesSingleton()
  bind<JSFactory>().toClass<DuktapeJSFactory>().singleton()

}
