/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.interactor.GetCatalogsByType
import tachiyomi.domain.catalog.interactor.InstallCatalog
import tachiyomi.domain.catalog.interactor.SyncRemoteCatalogs
import tachiyomi.domain.catalog.interactor.UninstallCatalog
import tachiyomi.domain.catalog.interactor.UpdateCatalog
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CatalogsViewModel @Inject constructor(
  private val getCatalogsByType: GetCatalogsByType,
  private val installCatalog: InstallCatalog,
  private val uninstallCatalog: UninstallCatalog,
  private val updateCatalog: UpdateCatalog,
  private val syncRemoteCatalogs: SyncRemoteCatalogs
) : BaseViewModel() {

  var localCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    private set
  var updatableCatalogs by mutableStateOf(emptyList<CatalogInstalled>())
    private set
  var remoteCatalogs by mutableStateOf(emptyList<CatalogRemote>())
    private set
  var languageChoices by mutableStateOf(emptyList<LanguageChoice>())
    private set
  var selectedLanguage by mutableStateOf<LanguageChoice>(LanguageChoice.All)
    private set
  var installSteps by mutableStateOf(emptyMap<String, InstallStep>())
    private set
  var refreshingCatalogs by mutableStateOf(false)
    private set

  private var unfilteredRemoteCatalogs = emptyList<CatalogRemote>()

  init {
    getCatalogs()
  }

  private fun getCatalogs() {
    scope.launch {
      getCatalogsByType.subscribe(excludeRemoteInstalled = true)
        .collect { (upToDate, updatable, remote) ->
          localCatalogs = upToDate
          updatableCatalogs = updatable
          unfilteredRemoteCatalogs = remote
          remoteCatalogs = getRemoteCatalogsForLanguageChoice(remote, selectedLanguage)
          languageChoices = getLanguageChoices(remote, upToDate + updatable)
        }
    }
  }

  fun installCatalog(catalog: Catalog) {
    scope.launch {
      val isUpdate = catalog in updatableCatalogs
      val (pkgName, flow) = if (isUpdate) {
        catalog as CatalogInstalled
        catalog.pkgName to updateCatalog.await(catalog)
      } else {
        catalog as CatalogRemote
        catalog.pkgName to installCatalog.await(catalog)
      }
      flow.collect { step ->
        installSteps = if (step != InstallStep.Completed) {
          installSteps + (pkgName to step)
        } else {
          installSteps - pkgName
        }
      }
    }
  }

  fun uninstallCatalog(catalog: Catalog) {
    scope.launch {
      uninstallCatalog.await(catalog as CatalogInstalled)
    }
  }

  fun setLanguageChoice(choice: LanguageChoice) {
    selectedLanguage = choice
    remoteCatalogs = getRemoteCatalogsForLanguageChoice(unfilteredRemoteCatalogs, selectedLanguage)
  }

  fun refreshCatalogs() {
    scope.launch {
      refreshingCatalogs = true
      syncRemoteCatalogs.await(true)
      refreshingCatalogs = false
    }
  }

  private fun getLanguageChoices(
    remote: List<CatalogRemote>,
    local: List<CatalogLocal>
  ): List<LanguageChoice> {
    val knownLanguages = mutableListOf<LanguageChoice.One>()
    val unknownLanguages = mutableListOf<Language>()

    val languageComparators = UserLanguagesComparator()
      .then(InstalledLanguagesComparator(local))
      .thenBy { it.code }

    remote.asSequence()
      .map { Language(it.lang) }
      .distinct()
      .sortedWith(languageComparators)
      .forEach { code ->
        if (code.toEmoji() != null) {
          knownLanguages.add(LanguageChoice.One(code))
        } else {
          unknownLanguages.add(code)
        }
      }

    val languages = mutableListOf<LanguageChoice>()
    languages.add(LanguageChoice.All)
    languages.addAll(knownLanguages)
    if (unknownLanguages.isNotEmpty()) {
      languages.add(LanguageChoice.Others(unknownLanguages))
    }

    return languages
  }

  private fun getRemoteCatalogsForLanguageChoice(
    catalogs: List<CatalogRemote>,
    choice: LanguageChoice
  ): List<CatalogRemote> {
    return when (choice) {
      is LanguageChoice.All -> catalogs
      is LanguageChoice.One -> catalogs.filter { choice.language.code == it.lang }
      is LanguageChoice.Others -> {
        val codes = choice.languages.map { it.code }
        catalogs.filter { it.lang in codes }
      }
    }
  }

}
