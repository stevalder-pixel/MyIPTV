package com.nextgen.iptv.data.repository

import com.nextgen.iptv.data.api.XtreamApiService
import com.nextgen.iptv.data.api.XtreamAuthResponse
import com.nextgen.iptv.data.api.XtreamCategory
import com.nextgen.iptv.data.api.XtreamEpgResponse
import com.nextgen.iptv.data.api.XtreamStream
import com.nextgen.iptv.data.api.XtreamVod
import com.nextgen.iptv.data.models.Channel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class XtreamRepository {

    private var service: XtreamApiService? = null
    private var baseUrl: String = ""
    private var username: String = ""
    private var password: String = ""

    fun configure(serverUrl: String, user: String, pass: String) {
        baseUrl = serverUrl.trimEnd('/')
        username = user
        password = pass

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        service = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApiService::class.java)
    }

    suspend fun authenticate(): Result<XtreamAuthResponse> {
        return try {
            val response = service!!.authenticate(username, password)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLiveCategories(): Result<List<XtreamCategory>> {
        return try {
            Result.success(service!!.getLiveCategories(username, password))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLiveStreams(categoryId: String = ""): Result<List<XtreamStream>> {
        return try {
            Result.success(service!!.getLiveStreams(username, password, categoryId = categoryId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVodStreams(categoryId: String = ""): Result<List<XtreamVod>> {
        return try {
            Result.success(service!!.getVodStreams(username, password, categoryId = categoryId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEpg(streamId: String): Result<XtreamEpgResponse> {
        return try {
            Result.success(service!!.getEpg(username, password, streamId = streamId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun buildStreamUrl(streamId: Int, extension: String = "ts"): String {
        return "$baseUrl/live/$username/$password/$streamId.$extension"
    }

    fun buildVodUrl(streamId: Int, extension: String = "mkv"): String {
        return "$baseUrl/movie/$username/$password/$streamId.$extension"
    }

    fun toChannel(stream: XtreamStream): Channel {
        return Channel(
            id = stream.streamId.toString(),
            name = stream.name,
            url = buildStreamUrl(stream.streamId),
            logo = stream.streamIcon,
            group = stream.categoryId,
            epgId = stream.epgChannelId
        )
    }

    companion object {
        val instance = XtreamRepository()
    }
}
