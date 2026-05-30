package com.nextgen.iptv.data.repository

import com.nextgen.iptv.data.api.StremioApiService
import com.nextgen.iptv.data.api.StremioManifest
import com.nextgen.iptv.data.api.StremioCatalogResponse
import com.nextgen.iptv.data.api.StremioStreamsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class StremioRepository {

    private val service: StremioApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://v3-cinemeta.strem.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StremioApiService::class.java)
    }

    suspend fun getManifest(manifestUrl: String): Result<StremioManifest> = try {
        val url = if (manifestUrl.endsWith("/manifest.json")) manifestUrl
                  else "${ manifestUrl.trimEnd('/') }/manifest.json"
        Result.success(service.getManifest(url))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCatalog(baseUrl: String, type: String, id: String, extra: String = ""): Result<StremioCatalogResponse> = try {
        val url = "${ baseUrl.trimEnd('/') }/catalog/$type/$id${if (extra.isNotEmpty()) "/$extra" else ""}.json"
        Result.success(service.getCatalog(url))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getStreams(baseUrl: String, type: String, id: String): Result<StremioStreamsResponse> = try {
        val url = "${ baseUrl.trimEnd('/') }/stream/$type/$id.json"
        Result.success(service.getStreams(url))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getMeta(baseUrl: String, type: String, id: String): Result<com.nextgen.iptv.data.api.StremioMetaResponse> = try {
        val url = "${ baseUrl.trimEnd('/') }/meta/$type/$id.json"
        Result.success(service.getMeta(url))
    } catch (e: Exception) { Result.failure(e) }

    companion object { val instance = StremioRepository() }
}
