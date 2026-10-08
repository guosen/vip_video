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
    ): VodListResponse

    @GET
    suspend fun detail(
        @Url url: String = "${VodApiConfig.API_BASE}?ac=detail",
        @Query("ids") ids: Int,
    ): VodListResponse

    @GET
    suspend fun suggest(
        @Url url: String = "${VodApiConfig.SUGGEST_BASE}?mid=1",
        @Query("wd") keyword: String,
    ): SuggestResponse
}

object VodApiConfig {
    const val API_BASE = "https://api.wsyzy.net/api.php/provide/vod/"
    const val SUGGEST_BASE = "https://wsyzy.cc/index.php/ajax/suggest"
    const val PLAYER_WRAPPER = "https://wsyzy.vip/m3u8/?url="
    const val DEFAULT_REFERER = "https://wsyzy.cc/"
}
