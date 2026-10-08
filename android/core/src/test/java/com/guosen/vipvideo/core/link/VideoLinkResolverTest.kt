package com.guosen.vipvideo.core.link

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoLinkResolverTest {
    @Test
    fun normalizeTencentMobileShareUrl() {
        val raw = "https://m.v.qq.com/x/m/play?vid=y4102o10vcn&cid=mzc00200yxhhqsu&ptag=share"
        assertEquals(
            "https://v.qq.com/x/cover/mzc00200yxhhqsu/y4102o10vcn.html",
            VideoLinkResolver.normalizePageUrl(raw),
        )
    }

    @Test
    fun cleanTitleFromTencentPageTitle() {
        assertEquals("楚离", VideoLinkResolver.cleanTitle("【腾讯视频】 楚离 01"))
    }

    @Test
    fun episodeFromTitleTrailingNumber() {
        assertEquals(1, VideoLinkResolver.episodeFromTitle("【腾讯视频】 楚离 01"))
    }
}
