/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils

private val base64 = Base64()

fun String.decodeBase64() = base64.decode(this)

fun String.md5() = DigestUtils.md5Hex(this)
