package com.guosen.vipvideo.core.link

object VideoLinkResolver {
    private val siteNoise = Regex(
        """爱奇艺|腾讯视频|腾讯|优酷|芒果TV|芒果|哔哩哔哩|bilibili|B站|搜狐视频|搜狐|""" +
            """在线观看|高清正版|免费观看|完整版|正片|预告|【|】""",
    )

    private val qqVid = Regex("""[?&]vid=([^&]+)""", RegexOption.IGNORE_CASE)
    private val qqCid = Regex("""[?&]cid=([^&]+)""", RegexOption.IGNORE_CASE)

    /** Prefer desktop/play URL forms that third-party parsers accept. */
    fun normalizePageUrl(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.contains("qq.com", ignoreCase = true)) return trimmed
        val vid = qqVid.find(trimmed)?.groupValues?.getOrNull(1)
        val cid = qqCid.find(trimmed)?.groupValues?.getOrNull(1)
        if (!vid.isNullOrBlank() && !cid.isNullOrBlank()) {
            return "https://v.qq.com/x/cover/$cid/$vid.html"
        }
        return trimmed
    }

    fun cleanTitle(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var t = raw.replace(siteNoise, " ").trim()
        t = t.replace(Regex("""第\s*\d{1,4}\s*[集期话]"""), " ")
        t = t.replace(Regex("""\s+\d{1,4}\s*$"""), " ")
        return t.split(Regex("""[-_\s|｜（(]+"""))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .firstOrNull()
            .orEmpty()
    }

    /** Extract episode number from title like「楚离 01」or「第12集」. */
    fun episodeFromTitle(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        Regex("""第\s*(\d{1,4})\s*[集期话]""").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
            return it
        }
        Regex("""(\d{1,4})\s*$""").find(raw.trim())?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
            return it
        }
        return 0
    }
}
