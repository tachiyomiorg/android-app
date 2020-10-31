/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse

data class LanguageChoices(val choices: List<LanguageChoice>, val selected: LanguageChoice)

sealed class LanguageChoice {
  object All : LanguageChoice()
  data class One(val language: Language) : LanguageChoice()
  data class Others(val languages: List<Language>) : LanguageChoice()
}
