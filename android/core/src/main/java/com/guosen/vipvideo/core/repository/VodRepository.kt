package com.guosen.vipvideo.core.repository

import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.SuggestItem
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.core.model.VodType
import com.guosen.vipvideo.core.network.NetworkModule
import com.guosen.vipvideo.core.util.EpisodeParser

class VodRepository(
    private val api: com.guosen.vipvideo.core.network.VodApi = NetworkModule.vodApi,
) {
    private val featuredTypeIds = listOf(
        1 to "电影",
        2 to "电视剧",
        3 to "综艺",
        4 to "动漫",
        29 to "国产动漫",
    )

    suspend fun loadHome(): List<HomeCategory> {
        return featuredTypeIds.map { (typeId, fallbackName) ->
            val response = api.list(typeId = typeId, page = 1)
            val name = response.`class`?.find { it.typeId == typeId }?.typeName ?: fallbackName
            HomeCategory(typeId, name, response.list)
        }
    }

    suspend fun loadTypes(): List<VodType> {
        val response = api.list(page = 1)
        return response.`class`.orEmpty()
    }

    suspend fun listByType(typeId: Int, page: Int): Pair<List<VodItem>, Int> {
        val response = api.list(typeId = typeId, page = page)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return response.list to pageCount
    }

    suspend fun search(keyword: String, page: Int = 1): Pair<List<VodItem>, Int> {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return emptyList<VodItem>() to 0
        val response = api.list(keyword = trimmed, page = page)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return response.list to pageCount
    }

    suspend fun suggest(keyword: String): List<SuggestItem> {
        val trimmed = keyword.trim()
        if (trimmed.length < 2) return emptyList()
        return runCatching { api.suggest(keyword = trimmed).list }.getOrDefault(emptyList())
    }

    suspend fun detail(id: Int): VodItem? {
        val response = api.detail(ids = id)
        return response.list.firstOrNull()
    }

    suspend fun episodes(id: Int) = EpisodeParser.parse(detail(id)?.vodPlayUrl)

    suspend fun latestUpdates(page: Int = 1): Pair<List<VodItem>, Int> {
        val response = api.list(page = page, hours = 24)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return response.list to pageCount
    }
}
