package com.nextgen.iptv.data.repository

import android.content.Context
import com.nextgen.iptv.data.models.DebridResolvedStream
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DebridRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun resolveStream(
        context: Context,
        magnetOrUrl: String
    ): Result<DebridResolvedStream> {
        val activeService = AppPreferences.getActiveDebrid(context).first()
        return when (activeService) {
            "realdebrid" -> resolveWithRealDebrid(context, magnetOrUrl)
            "torbox" -> resolveWithTorBox(context, magnetOrUrl)
            "alldebrid" -> resolveWithAllDebrid(context, magnetOrUrl)
            "premiumize" -> resolveWithPremiumize(context, magnetOrUrl)
            else -> resolveWithTorBox(context, magnetOrUrl)
        }
    }

    private suspend fun resolveWithRealDebrid(context: Context, url: String): Result<DebridResolvedStream> {
        return try {
            val apiKey = AppPreferences.getRdApiKey(context).first()
            if (apiKey.isEmpty()) return Result.failure(Exception("Real-Debrid API key not set"))

            val body = FormBody.Builder().add("link", url).build()
            val request = Request.Builder()
                .url("https://api.real-debrid.com/rest/1.0/unrestrict/link")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            Result.success(DebridResolvedStream(
                url = json.optString("download", ""),
                quality = json.optString("quality", ""),
                filename = json.optString("filename", ""),
                size = json.optLong("filesize", 0),
                service = "realdebrid"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun resolveWithTorBox(context: Context, url: String): Result<DebridResolvedStream> {
        return try {
            val apiKey = AppPreferences.getTorBoxApiKey(context).first()
            if (apiKey.isEmpty()) return Result.failure(Exception("TorBox API key not set"))

            val body = FormBody.Builder().add("link", url).build()
            val request = Request.Builder()
                .url("https://api.torbox.app/v1/api/webdl/createwebdownload")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            val data = json.optJSONObject("data")
            Result.success(DebridResolvedStream(
                url = data?.optString("url", url) ?: url,
                quality = "HD",
                filename = data?.optString("name", "") ?: "",
                service = "torbox"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun resolveWithAllDebrid(context: Context, url: String): Result<DebridResolvedStream> {
        return try {
            val apiKey = AppPreferences.getAllDebridApiKey(context).first()
            if (apiKey.isEmpty()) return Result.failure(Exception("AllDebrid API key not set"))

            val request = Request.Builder()
                .url("https://api.alldebrid.com/v4/link/unlock?apikey=$apiKey&link=${java.net.URLEncoder.encode(url, "UTF-8")}")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            val data = json.optJSONObject("data")
            Result.success(DebridResolvedStream(
                url = data?.optString("link", url) ?: url,
                quality = "",
                filename = data?.optString("filename", "") ?: "",
                size = data?.optLong("filesize", 0) ?: 0,
                service = "alldebrid"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun resolveWithPremiumize(context: Context, url: String): Result<DebridResolvedStream> {
        return try {
            val apiKey = AppPreferences.getPremiumizeApiKey(context).first()
            if (apiKey.isEmpty()) return Result.failure(Exception("Premiumize API key not set"))

            val body = FormBody.Builder().add("src", url).build()
            val request = Request.Builder()
                .url("https://www.premiumize.me/api/transfer/directdl?apikey=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            val content = json.optJSONArray("content")
            val first = content?.optJSONObject(0)
            Result.success(DebridResolvedStream(
                url = first?.optString("link", url) ?: url,
                quality = "",
                filename = first?.optString("path", "") ?: "",
                service = "premiumize"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    companion object { val instance = DebridRepository() }
}
