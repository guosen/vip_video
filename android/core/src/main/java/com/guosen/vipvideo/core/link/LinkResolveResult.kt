package com.guosen.vipvideo.core.link

import com.guosen.vipvideo.core.model.VodItem

sealed class LinkResolveResult {
    data class NativePlay(
        val item: VodItem,
        val episodeIndex: Int,
    ) : LinkResolveResult()

    data class WebParse(
        val normalizedUrl: String,
        val reason: String? = null,
    ) : LinkResolveResult()

    data class Failed(val message: String) : LinkResolveResult()
}
