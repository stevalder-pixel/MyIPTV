package com.nextgen.iptv.data.repository

import com.nextgen.iptv.data.api.XtreamApiService
import com.nextgen.iptv.data.api.XtreamAuthResponse
import com.nextgen.iptv.data.api.XtreamCategory
import com.nextgen.iptv.data.api.XtreamEpgResponse
import com.nextgen.iptv.data.api.XtreamStream
import com.nextgen.iptv.data.api.XtreamVod
import com.nextgen.iptv.data.models.Channel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class XtreamRepository {
    private var service: XtreamApiService? = null
    private var baseUrl = ""; private var username = ""; private var password = ""

    fun configure(url: String, user: String, pass: String) {
        baseUrl = url.trimEnd('/'); username = user; password = pass
        service = Retrofit.Builder().baseUrl("$baseUrl/")
            .client(OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(XtreamApiService::class.java)
    }

    suspend fun authenticate(): Result<XtreamAuthResponse> = try { Result.success(service!!.authenticate(username, password)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getLiveCategories(): Result<List<XtreamCategory>> = try { Result.success(service!!.getLiveCategories(username, password)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getLiveStreams(categoryId: String = ""): Result<List<XtreamStream>> = try { Result.success(service!!.getLiveStreams(username, password, categoryId = categoryId)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getVodStreams(categoryId: String = ""): Result<List<XtreamVod>> = try { Result.success(service!!.getVodStreams(username, password, categoryId = categoryId)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getEpg(streamId: String): Result<XtreamEpgResponse> = try { Result.success(service!!.getEpg(username, password, streamId = streamId)) } catch (e: Exception) { Result.failure(e) }

    fun buildStreamUrl(streamId: Int) = "$baseUrl/live/$username/$password/$streamId.ts"
    fun buildVodUrl(streamId: Int, ext: String = "mkv") = "$baseUrl/movie/$username/$password/$streamId.$ext"

    fun toChannel(s: XtreamStream) = Channel(
        id = s.streamId.toString(), name = s.name ?: "Unknown",
        url = buildStreamUrl(s.streamId), logo = s.streamIcon ?: "",
        group = s.categoryId ?: "", epgId = s.epgChannelId ?: ""
    )

    companion object { val instance = XtreamRepository() }
}
