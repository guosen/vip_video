package com.guosen.vipvideo.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VodListResponse(
    val code: Int = 0,
    val msg: String? = null,
    @SerialName("page") val page: String? = null,
    @SerialName("pagecount") val pageCount: String? = null,
    val limit: String? = null,
    val total: String? = null,
    val list: List<VodItem> = emptyList(),
    val `class`: List<VodType>? = null,
)

@Serializable
data class VodItem(
    @SerialName("vod_id") val vodId: Int? = null,
    val id: Int? = null,
    @SerialName("vod_name") val vodName: String? = null,
    val name: String? = null,
    @SerialName("vod_pic") val vodPic: String? = null,
    val pic: String? = null,
    @SerialName("type_id") val typeId: Int? = null,
    @SerialName("type_name") val typeName: String? = null,
    @SerialName("vod_remarks") val vodRemarks: String? = null,
    @SerialName("vod_year") val vodYear: String? = null,
    @SerialName("vod_area") val vodArea: String? = null,
    @SerialName("vod_actor") val vodActor: String? = null,
    @SerialName("vod_director") val vodDirector: String? = null,
    @SerialName("vod_content") val vodContent: String? = null,
    @SerialName("vod_play_url") val vodPlayUrl: String? = null,
    @SerialName("vod_time") val vodTime: String? = null,
) {
    val idResolved: Int get() = vodId ?: id ?: 0
    val title: String get() = vodName ?: name.orEmpty()
    val poster: String? get() = vodPic ?: pic
}

@Serializable
data class VodType(
    @SerialName("type_id") val typeId: Int,
    @SerialName("type_name") val typeName: String,
)

@Serializable
data class SuggestResponse(
    val code: Int = 0,
    val list: List<SuggestItem> = emptyList(),
)

@Serializable
data class SuggestItem(
    val id: Int,
    val name: String,
    val pic: String? = null,
)

@Serializable
data class Episode(
    val name: String,
    val url: String,
)

data class HomeCategory(
    val typeId: Int,
    val typeName: String,
    val items: List<VodItem>,
)

data class ParseSource(
    val id: String,
    val name: String,
    val baseUrl: String?,
    val isCloudDirect: Boolean = false,
)

object ParseSources {
    /** Mirrors default parser list from [88lin/video_vip](https://github.com/88lin/video_vip) userscript. */
    val defaults: List<ParseSource> = listOf(
        ParseSource("wsyzy", "无损云解析", baseUrl = null, isCloudDirect = true),
        ParseSource("maccms", "麒麟解析", "https://free.maccms.xyz/?url="),
        ParseSource("txnp", "TXNQ解析", "https://bfq.txnp.cn/player?url="),
        ParseSource("202617", "七哥解析", "https://jx.202617.xyz/tv.php?url="),
        ParseSource("fongmi", "fongmi解析", "https://json.fongmi.cc/web?url="),
        ParseSource("bingdou", "冰豆解析", "https://bd.jx.cn/?url="),
        ParseSource("hls", "HLS解析", "https://jx.hls.one/?url="),
        ParseSource("playerjy", "Player-JY", "https://jx.playerjy.com/?url="),
    )
}
