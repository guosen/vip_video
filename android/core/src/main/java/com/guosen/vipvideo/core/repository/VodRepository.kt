package com.guosen.vipvideo.core.repository

import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.SuggestItem
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.core.model.VodType
import com.guosen.vipvideo.core.network.NetworkModule
import com.guosen.vipvideo.core.util.EpisodeParser
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class VodRepository(
    private val api: com.guosen.vipvideo.core.network.VodApi = NetworkModule.vodApi,
) {
    /**
     * Parent types 1–4 are empty on the provider; use leaf types that actually return data
     * while keeping user-facing section titles familiar.
     */
    private val featuredSections = listOf(
        6 to "电影",
        13 to "电视剧",
        25 to "综艺",
        29 to "动漫",
        30 to "日韩动漫",
    )

    suspend fun loadHome(forceRefresh: Boolean = false): HomeFeed {
        if (!forceRefresh) {
            HomeFeedCache.getFresh()?.let { return it }
        }
        val feed = coroutineScope {
            val latestDeferred = async {
                enrichPosters(api.list(page = 1, hours = 24).list)
            }
            val categories = featuredSections.map { (typeId, title) ->
                async {
                    val response = api.list(typeId = typeId, page = 1)
                    val name = response.`class`?.find { it.typeId == typeId }?.typeName ?: title
                    HomeCategory(typeId, name, enrichPosters(response.list))
                }
            }.map { it.await() }.filter { it.items.isNotEmpty() }

            HomeFeed(
                latest = latestDeferred.await(),
                categories = categories,
                loadedAtMs = System.currentTimeMillis(),
            )
        }
        HomeFeedCache.put(feed)
        return feed
    }

    suspend fun loadTypes(): List<VodType> {
        val response = api.list(page = 1)
        return response.`class`.orEmpty()
    }

    suspend fun listByType(typeId: Int, page: Int): Pair<List<VodItem>, Int> {
        val response = api.list(typeId = typeId, page = page)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return enrichPosters(response.list) to pageCount
    }

    /** MacCMS list `wd` is disabled on this provider; search via suggest API (same as web UI). */
    suspend fun search(keyword: String, page: Int = 1): Pair<List<VodItem>, Int> {
        val trimmed = keyword.trim()
        if (trimmed.length < 2) return emptyList<VodItem>() to 0
        val suggests = suggest(trimmed)
        val items = suggests.map { it.toVodItem() }
        return items to 1
    }

    suspend fun suggest(keyword: String): List<SuggestItem> {
        val trimmed = keyword.trim()
        if (trimmed.length < 2) return emptyList()
        return runCatching { api.suggest(keyword = trimmed).list }.getOrDefault(emptyList())
    }

    suspend fun detail(id: Int): VodItem? {
        val response = api.detail(ids = id.toString())
        return response.list.firstOrNull()
    }

    suspend fun episodes(id: Int) = EpisodeParser.parse(detail(id)?.vodPlayUrl)

    suspend fun latestUpdates(page: Int = 1): Pair<List<VodItem>, Int> {
        val response = api.list(page = page, hours = 24)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return enrichPosters(response.list) to pageCount
    }

    private suspend fun enrichPosters(items: List<VodItem>): List<VodItem> {
        if (items.isEmpty()) return items
        val missing = items.filter { it.poster.isNullOrBlank() }
        if (missing.isEmpty()) return items

        val detailsById = runCatching {
            val ids = missing.map { it.idResolved }.distinct().joinToString(",")
            api.detail(ids = ids).list.associateBy { it.idResolved }
        }.getOrDefault(emptyMap())

        return items.map { item ->
            if (!item.poster.isNullOrBlank()) {
                item
            } else {
                val detail = detailsById[item.idResolved]
                item.mergePoster(detail)
            }
        }
    }

    private fun SuggestItem.toVodItem() = VodItem(
        vodId = id,
        vodName = name,
        pic = pic,
        vodPic = pic,
    )

    private fun VodItem.mergePoster(detail: VodItem?): VodItem {
        if (detail == null) return this
        return copy(
            vodPic = vodPic ?: detail.vodPic,
            pic = pic ?: detail.pic,
            typeName = typeName ?: detail.typeName,
            vodRemarks = vodRemarks ?: detail.vodRemarks,
            vodYear = vodYear ?: detail.vodYear,
        )
    }
}
