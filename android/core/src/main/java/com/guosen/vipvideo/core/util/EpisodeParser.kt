package com.guosen.vipvideo.core.util

import com.guosen.vipvideo.core.model.Episode

object EpisodeParser {
    /**
     * Parses MacCMS-style play URL: `第01集$https://...#第02集$https://...`
     * Uses first source group before `$$$` when multiple lines exist.
     */
    fun parse(playUrl: String?): List<Episode> {
        if (playUrl.isNullOrBlank()) return emptyList()
        val primary = playUrl.split("$$$").firstOrNull().orEmpty()
        return primary.split("#")
            .mapNotNull { segment ->
                val index = segment.indexOf('$')
                if (index <= 0) {
                    val url = segment.trim()
                    if (url.startsWith("http")) Episode("", url) else null
                } else {
                    val name = segment.substring(0, index).trim()
                    val url = segment.substring(index + 1).trim()
                    if (url.startsWith("http")) Episode(name, url) else null
                }
            }
    }
}
