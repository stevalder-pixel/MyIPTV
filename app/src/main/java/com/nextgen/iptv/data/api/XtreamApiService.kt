package com.nextgen.iptv.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {

    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): XtreamAuthResponse

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String = ""
    ): List<XtreamStream>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String = ""
    ): List<XtreamVod>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String = ""
    ): List<XtreamSeries>

    @GET("player_api.php")
    suspend fun getEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: String,
        @Query("limit") limit: Int = 4
    ): XtreamEpgResponse
}

data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: XtreamUserInfo?,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo?
)

data class XtreamUserInfo(
    val username: String = "",
    val password: String = "",
    val status: String = "",
    @SerializedName("exp_date") val expDate: String = "",
    @SerializedName("max_connections") val maxConnections: String = "",
    @SerializedName("active_cons") val activeCons: String = ""
)

data class XtreamServerInfo(
    val url: String = "",
    val port: String = "",
    val protocol: String = "http",
    val timezone: String = ""
)

data class XtreamCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

data class XtreamStream(
    @SerializedName("stream_id") val streamId: Int,
    val name: String,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("epg_channel_id") val epgChannelId: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("tv_archive") val tvArchive: Int = 0,
    val num: Int = 0
)

data class XtreamVod(
    @SerializedName("stream_id") val streamId: Int,
    val name: String,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    val rating: String = "",
    val plot: String = "",
    val year: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mkv"
)

data class XtreamSeries(
    @SerializedName("series_id") val seriesId: Int,
    val name: String,
    val cover: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    val rating: String = "",
    val plot: String = ""
)

data class XtreamEpgResponse(
    @SerializedName("epg_listings") val epgListings: List<XtreamEpgEntry> = emptyList()
)

data class XtreamEpgEntry(
    val title: String = "",
    val description: String = "",
    val start: String = "",
    val end: String = "",
    @SerializedName("start_timestamp") val startTimestamp: Long = 0,
    @SerializedName("stop_timestamp") val stopTimestamp: Long = 0
)
