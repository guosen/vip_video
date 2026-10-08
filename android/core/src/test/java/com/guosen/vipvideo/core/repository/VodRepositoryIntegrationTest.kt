package com.guosen.vipvideo.core.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VodRepositoryIntegrationTest {
    private val repository = VodRepository()

    @Test
    fun latestUpdatesReturnsItems() = runBlocking {
        val (items, _) = repository.latestUpdates(page = 1)
        assertFalse(items.isEmpty())
    }

    @Test
    fun detailHasPlayUrl() = runBlocking {
        val (items, _) = repository.latestUpdates(page = 1)
        val id = items.first().idResolved
        val detail = repository.detail(id)
        assertNotNull(detail)
        val episodes = repository.episodes(id)
        assertTrue(episodes.isNotEmpty())
        assertTrue(episodes.first().url.startsWith("http"))
    }

    @Test
    fun searchSuggestWorks() = runBlocking {
        val suggests = repository.suggest("狗狗")
        assertFalse(suggests.isEmpty())
    }

    @Test
    fun homeCategoriesHaveItemsAndPosters() = runBlocking {
        val feed = repository.loadHome(forceRefresh = true)
        assertFalse(feed.categories.isEmpty())
        assertTrue(feed.categories.all { it.items.isNotEmpty() })
        assertTrue(feed.latest.any { !it.poster.isNullOrBlank() })
    }
}
