package com.guosen.vipvideo.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeParserTest {
    @Test
    fun parseStandardMacCmsFormat() {
        val raw = "第01集\$https://cdn.example/a.m3u8#第02集\$https://cdn.example/b.m3u8"
        val episodes = EpisodeParser.parse(raw)
        assertEquals(2, episodes.size)
        assertEquals("第01集", episodes[0].name)
        assertTrue(episodes[0].url.endsWith("a.m3u8"))
    }

    @Test
    fun parseUsesFirstSourceGroupOnly() {
        val raw = "第01集\$https://a.test/1.m3u8\$\$\$备用\$ignored"
        val episodes = EpisodeParser.parse(raw)
        assertEquals(1, episodes.size)
    }
}
