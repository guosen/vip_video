package com.guosen.vipvideo.core.repository

import com.guosen.vipvideo.core.model.HomeCategory
import com.guosen.vipvideo.core.model.SuggestItem
import com.guosen.vipvideo.core.model.VodItem
import com.guosen.vipvideo.core.model.VodType
import com.guosen.vipvideo.core.link.LinkResolveResult
import com.guosen.vipvideo.core.link.VideoLinkResolver
import com.guosen.vipvideo.core.network.NetworkModule
import com.guosen.vipvideo.core.network.VodApiConfig
import com.guosen.vipvideo.core.util.EpisodeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Request

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

    /** 首页每个横滑分区拉取的页数（每页最多 [VodApiConfig.LIST_PAGE_SIZE] 条） */
    private val homeSectionPages = 3

    /** 首页「最近更新」拉取页数（全站 list，不按 24h 过滤） */
    private val homeLatestPages = 4

    suspend fun loadHome(forceRefresh: Boolean = false): HomeFeed {
        if (!forceRefresh) {
            HomeFeedCache.getFresh()?.let { return it }
        }
        val feed = coroutineScope {
            val latestDeferred = async {
                enrichPosters(listPages(typeId = null, pages = homeLatestPages, hours = null))
            }
            val categories = featuredSections.map { (typeId, title) ->
                async {
                    val response = api.list(typeId = typeId, page = 1)
                    val name = response.`class`?.find { it.typeId == typeId }?.typeName ?: title
                    HomeCategory(
                        typeId,
                        name,
                        enrichPosters(listPages(typeId = typeId, pages = homeSectionPages)),
                    )
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
        val items = enrichPosters(suggests.map { it.toVodItem() })
        return items to 1
    }

    suspend fun suggest(keyword: String): List<SuggestItem> {
        val trimmed = keyword.trim()
        if (trimmed.length < 2) return emptyList()
        return runCatching {
            api.suggest(keyword = trimmed, limit = VodApiConfig.SUGGEST_MAX_LIMIT).list
        }.getOrDefault(emptyList())
    }

    suspend fun detail(id: Int): VodItem? {
        val response = api.detail(ids = id.toString())
        return response.list.firstOrNull()
    }

    suspend fun episodes(id: Int) = EpisodeParser.parse(detail(id)?.vodPlayUrl)

    /**
     * 1) Normalize VIP URL (e.g. m.v.qq.com → v.qq.com/x/cover/cid/vid.html)
     * 2) Read page title → search 无损云库 → native ExoPlayer when matched
     * 3) Otherwise fall back to third-party Web parser
     */
    suspend fun resolveShareLink(rawUrl: String): LinkResolveResult {
        val trimmed = rawUrl.trim()
        if (!trimmed.startsWith("http")) {
            return LinkResolveResult.Failed("链接必须以 http 开头")
        }
        val normalized = VideoLinkResolver.normalizePageUrl(trimmed)
        val pageTitle = fetchPageTitleForShareLink(trimmed, normalized)
        val keyword = VideoLinkResolver.cleanTitle(pageTitle)
        val episodeNum = VideoLinkResolver.episodeFromTitle(pageTitle)

        if (keyword.length >= 2) {
            val candidates = suggest(keyword)
            val match = candidates.firstOrNull { it.name == keyword }
                ?: candidates.firstOrNull { it.name.contains(keyword) || keyword.contains(it.name) }
                ?: candidates.firstOrNull()
            if (match != null) {
                val detail = detail(match.id)
                if (detail != null) {
                    val episodes = EpisodeParser.parse(detail.vodPlayUrl)
                    val index = when {
                        episodeNum <= 0 -> 0
                        episodeNum <= episodes.size -> episodeNum - 1
                        else -> 0
                    }
                    return LinkResolveResult.NativePlay(detail, index)
                }
            }
        }

        val webUrl = pickWebParseUrl(trimmed, normalized)
        return LinkResolveResult.WebParse(
            normalizedUrl = webUrl,
            reason = if (keyword.isBlank()) {
                "无法识别片名，已走网页解析（建议更换解析源）"
            } else {
                "资源库未收录「$keyword」，已走网页解析"
            },
        )
    }

    private fun pickWebParseUrl(raw: String, normalized: String): String {
        if (raw.contains("m.v.qq.com", ignoreCase = true)) return raw.trim()
        return normalized.ifBlank { raw.trim() }
    }

    private suspend fun fetchPageTitleForShareLink(raw: String, normalized: String): String {
        val candidates = linkedSetOf<String>()
        candidates += raw.trim()
        VideoLinkResolver.buildMobileQqPlayUrl(raw)?.let { candidates += it }
        if (normalized.isNotBlank()) candidates += normalized
        for (url in candidates) {
            val title = fetchPageTitle(url)
            if (VideoLinkResolver.isUsablePageTitle(title)) return title
        }
        return candidates.firstOrNull()?.let { fetchPageTitle(it) }.orEmpty()
    }

    private suspend fun fetchPageTitle(url: String): String = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Referer", "https://v.qq.com/")
                .build()
            NetworkModule.pageClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext ""
                parseTitleFromHtml(response.body?.string().orEmpty())
            }
        }.getOrDefault("")
    }

    private fun parseTitleFromHtml(html: String): String {
        Regex("""property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)
            ?.let { return decodeHtmlEntities(it) }
        Regex("""<title>([^<]+)</title>""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)
            ?.let { return decodeHtmlEntities(it) }
        return ""
    }

    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }

    suspend fun latestUpdates(page: Int = 1): Pair<List<VodItem>, Int> {
        val response = api.list(page = page, hours = 24)
        val pageCount = response.pageCount?.toIntOrNull() ?: 1
        return enrichPosters(response.list) to pageCount
    }

    private suspend fun listPages(
        typeId: Int?,
        pages: Int,
        hours: Int? = null,
    ): List<VodItem> {
        if (pages <= 0) return emptyList()
        val merged = LinkedHashMap<Int, VodItem>()
        for (page in 1..pages) {
            val chunk = api.list(typeId = typeId, page = page, hours = hours).list
            if (chunk.isEmpty()) break
            chunk.forEach { merged.putIfAbsent(it.idResolved, it) }
            if (chunk.size < VodApiConfig.LIST_PAGE_SIZE) break
        }
        return merged.values.toList()
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
