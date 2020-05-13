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
//import io.reactivex.Flowable
//import io.reactivex.Single
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.ArgumentMatchers.anyInt
//import org.mockito.ArgumentMatchers.anyLong
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.Mockito.verify
//import org.mockito.junit.MockitoJUnitRunner
//import tachiyomi.core.util.Optional
//import tachiyomi.domain.manga.model.Manga
//import tachiyomi.domain.manga.repository.MangaRepository
//import javax.inject.Inject
//
//@RunWith(MockitoJUnitRunner::class)
//class SubscribeMangaPageFromCatalogSourceTest {
//
//  @Rule @JvmField
//  val toothpickRule = ToothPickRule(this, "GetMangaPageFromCatalogSource")
//
//  @Mock lateinit var mangaRepository: MangaRepository
//  @Mock lateinit var source: CatalogSource
//
//  @Inject lateinit var getMangaFromSource: GetMangaPageFromCatalogSource
//
//  @Before
//  fun setup() {
//    toothpickRule.inject(this)
//  }
//
//  @Test
//  fun test_interact() {
//    // GIVEN
//    val sourceManga1 = MangaMeta("url1", "title1")
//    val sourceManga2 = MangaMeta("url2", "title2")
//    val sourceManga3 = MangaMeta("url3", "title3")
//    val sourceMangaPage = MangasPageMeta(listOf(sourceManga1, sourceManga2, sourceManga3), false)
//
//    val dbManga1 = Manga(1, 1, "url1", "title1")
//    val dbManga2 = Manga(2, 1, "url2", "title2")
//    val dbManga3 = Manga(3, 1, "url3", "title3")
//
//    `when`(source.id).thenReturn(1)
//    `when`(source.fetchMangaList(anyInt())).thenReturn(sourceMangaPage)
//
//    `when`(mangaRepository.subscribe(eq("url1"), anyLong()))
//      .thenReturn(Flowable.just(Optional.of(dbManga1)))
//
//    `when`(mangaRepository.subscribe(eq("url2"), anyLong()))
//      .thenReturn(Flowable.just(Optional.None))
//
//    `when`(mangaRepository.subscribe(eq("url3"), anyLong()))
//      .thenReturn(Flowable.just(Optional.None))
//
//    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga1), anyLong()))
//      .thenReturn(Single.just(dbManga1))
//
//    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga2), anyLong()))
//      .thenReturn(Single.just(dbManga2))
//
//    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga3), anyLong()))
//      .thenReturn(Single.just(dbManga3))
//
//    // WHEN
//    getMangaFromSource.interact(source, 1).subscribe()
//
//    // THEN
//    verify(mangaRepository, never()).saveAndReturnNewManga(eq(sourceManga1), anyLong())
//
//    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga2), anyLong())
//
//    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga3), anyLong())
//  }
//}
