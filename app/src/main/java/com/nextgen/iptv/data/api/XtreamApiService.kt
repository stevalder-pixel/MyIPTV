package com.nextgen.iptv.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {
    @GET("player_api.php")
    suspend fun authenticate(@Query("username") u: String, @Query("password") p: String): XtreamAuthResponse

    @GET("player_api.php")
    suspend fun getLiveCategories(@Query("username") u: String, @Query("password") p: String, @Query("action") a: String = "get_live_categories"): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getLiveStreams(@Query("username") u: String, @Query("password") p: String, @Query("action") a: String = "get_live_streams", @Query("category_id") categoryId: String = ""): List<XtreamStream>

    @GET("player_api.php")
    suspend fun getVodStreams(@Query("username") u: String, @Query("password") p: String, @Query("action") a: String = "get_vod_streams", @Query("category_id") categoryId: String = ""): List<XtreamVod>

    @GET("player_api.php")
    suspend fun getEpg(@Query("username") u: String, @Query("password") p: String, @Query("action") a: String = "get_short_epg", @Query("stream_id") streamId: String, @Query("limit") limit: Int = 4): XtreamEpgResponse
}

data class XtreamAuthResponse(@SerializedName("user_info") val userInfo: XtreamUserInfo?, @SerializedName("server_info") val serverInfo: XtreamServerInfo?)
data class XtreamUserInfo(val username: String = "", val status: String = "", @SerializedName("exp_date") val expDate: String = "")
data class XtreamServerInfo(val url: String = "", val port: String = "")
data class XtreamCategory(@SerializedName("category_id") val categoryId: String = "", @SerializedName("category_name") val categoryName: String = "", @SerializedName("parent_id") val parentId: Int = 0)
data class XtreamStream(@SerializedName("stream_id") val streamId: Int = 0, val name: String? = null, @SerializedName("stream_icon") val streamIcon: String? = null, @SerializedName("epg_channel_id") val epgChannelId: String? = null, @SerializedName("category_id") val categoryId: String? = null, @SerializedName("tv_archive") val tvArchive: Int = 0, val num: Int = 0)
data class XtreamVod(@SerializedName("stream_id") val streamId: Int = 0, val name: String? = null, @SerializedName("stream_icon") val streamIcon: String? = null, @SerializedName("category_id") val categoryId: String? = null, val rating: String = "", val plot: String = "", val year: String = "", @SerializedName("container_extension") val containerExtension: String = "mkv")
data class XtreamEpgResponse(@SerializedName("epg_listings") val epgListings: List<XtreamEpgEntry> = emptyList())
data class XtreamEpgEntry(val title: String = "", val description: String = "", val start: String = "", val end: String = "", @SerializedName("start_timestamp") val startTimestamp: Long = 0, @SerializedName("stop_timestamp") val stopTimestamp: Long = 0)
