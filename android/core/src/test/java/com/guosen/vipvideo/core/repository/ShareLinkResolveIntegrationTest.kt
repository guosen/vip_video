package com.guosen.vipvideo.core.repository

import com.guosen.vipvideo.core.link.LinkResolveResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareLinkResolveIntegrationTest {
    private val repository = VodRepository()

    @Test
    fun resolveTencentChuliShareLinkToNativePlay() = runBlocking {
        val url =
            "https://m.v.qq.com/x/m/play?vid=y4102o10vcn&cid=mzc00200yxhhqsu&ptag=share_11_11"
        when (val result = repository.resolveShareLink(url)) {
            is LinkResolveResult.NativePlay -> {
                assertEquals("楚离", result.item.title)
                assertEquals(0, result.episodeIndex)
            }
            else -> error("Expected NativePlay but got $result")
        }
    }
}
