package com.guosen.vipvideo.core.network

import com.guosen.vipvideo.core.model.SuggestResponse
import com.guosen.vipvideo.core.model.VodListResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface VodApi {
    @GET
    suspend fun list(
        @Url url: String = "${VodApiConfig.API_BASE}?ac=list",
        @Query("pg") page: Int = 1,
        @Query("t") typeId: Int? = null,
        @Query("wd") keyword: String? = null,
        @Query("h") hours: Int? = null,
        @Query("limit") limit: Int? = null,
    ): VodListResponse

    @GET
    suspend fun detail(
        @Url url: String = "${VodApiConfig.API_BASE}?ac=detail",
        @Query("ids") ids: String,
    ): VodListResponse

    @GET
    suspend fun suggest(
        @Url url: String = "${VodApiConfig.SUGGEST_BASE}?mid=1",
        @Query("wd") keyword: String,
        @Query("limit") limit: Int? = null,
    ): SuggestResponse
}

object VodApiConfig {
    /** MacCMS 影视采集 JSON 接口（与 [88lin/video_vip](https://github.com/88lin/video_vip) 无损云同源） */
    const val API_BASE = "https://api.wsyzy.net/api.php/provide/vod/"
    /** 联想搜索（该源 list 的 wd 搜索已关闭，仅 suggest 可用） */
    const val SUGGEST_BASE = "https://wsyzy.cc/index.php/ajax/suggest"
    /** 列表接口单页条数上限（实测最大 20） */
    const val LIST_PAGE_SIZE = 20
    /** 联想搜索单次最多条数（实测 limit=50 有效） */
    const val SUGGEST_MAX_LIMIT = 50
    const val PLAYER_WRAPPER = "https://wsyzy.vip/m3u8/?url="
    const val DEFAULT_REFERER = "https://wsyzy.cc/"
}
