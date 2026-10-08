package com.guosen.vipvideo.core.repository

import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.VodItem

data class HomeFeed(
    val latest: List<VodItem> = emptyList(),
    val categories: List<HomeCategory> = emptyList(),
    val loadedAtMs: Long = 0L,
)

object HomeFeedCache {
    private const val TTL_MS = 10 * 60 * 1000L

    @Volatile
    var feed: HomeFeed? = null

    fun getFresh(): HomeFeed? {
        val cached = feed ?: return null
        return if (System.currentTimeMillis() - cached.loadedAtMs < TTL_MS) cached else null
    }

    fun put(feed: HomeFeed) {
        this.feed = feed
    }
}
