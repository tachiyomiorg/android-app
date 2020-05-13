///*
// * Copyright (C) 2018 The Tachiyomi Open Source Project
// *
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at http://mozilla.org/MPL/2.0/.
// */
//
//package tachiyomi.domain.manga.interactor
//
//import com.nhaarman.mockito_kotlin.eq
//import com.nhaarman.mockito_kotlin.never
//import com.nhaarman.mockito_kotlin.verify
//import io.reactivex.Flowable
//import io.reactivex.Single
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.ArgumentMatchers.anyLong
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.junit.MockitoJUnitRunner
//import tachiyomi.core.util.Optional
//import tachiyomi.domain.manga.model.Manga
//import tachiyomi.domain.manga.repository.MangaRepository
//import javax.inject.Inject
//
//@RunWith(MockitoJUnitRunner::class)
//class GetOrAddMangaFromSourceTest {
//
//  @Rule @JvmField
//  val toothpickRule = ToothPickRule(this, "GetOrAddMangaFromSource")
//
//  @Mock lateinit var mangaRepository: MangaRepository
//
//  @Inject lateinit var getOrAddMangaFromSource: GetOrAddMangaFromSource
//
//  private val sourceManga = MangaMeta("url", "title")
//  private val sourceId = 1L
//  private val dbManga = Manga(1, sourceId, sourceManga.key, sourceManga.title)
//
//  @Before
//  fun setup() {
//    toothpickRule.inject(this)
//  }
//
//  @Test
//  fun `creates manga when not found in repository`() {
//    `when`(mangaRepository.subscribe(sourceManga.key, sourceId))
//      .thenReturn(Flowable.just(Optional.None))
//
//    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga), anyLong()))
//      .thenReturn(Single.just(dbManga))
//
//    getOrAddMangaFromSource.interact(sourceManga, sourceId).blockingGet()
//
//    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga), eq(sourceId))
//  }
//
//  @Test
//  fun `returns manga when found in repository`() {
//    `when`(mangaRepository.subscribe(sourceManga.key, sourceId))
//      .thenReturn(Flowable.just(Optional.of(dbManga)))
//
//    getOrAddMangaFromSource.interact(sourceManga, sourceId).blockingGet()
//
//    verify(mangaRepository, never()).saveAndReturnNewManga(sourceManga, sourceId)
//  }
//
//}
