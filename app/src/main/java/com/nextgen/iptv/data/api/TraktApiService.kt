package com.nextgen.iptv.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface TraktApiService {

    @GET("users/me/watchlist/movies")
    suspend fun getMovieWatchlist(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") clientId: String,
        @Header("trakt-api-version") version: String = "2"
    ): List<TraktWatchlistResponse>

    @GET("users/me/watchlist/shows")
    suspend fun getShowWatchlist(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") clientId: String,
        @Header("trakt-api-version") version: String = "2"
    ): List<TraktWatchlistResponse>

    @GET("users/me/history")
    suspend fun getHistory(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") clientId: String,
        @Header("trakt-api-version") version: String = "2",
        @Query("limit") limit: Int = 20
    ): List<TraktHistoryResponse>

    @POST("scrobble/start")
    suspend fun scrobbleStart(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") clientId: String,
        @Header("trakt-api-version") version: String = "2",
        @Body body: TraktScrobbleBody
    ): TraktScrobbleResponse

    @POST("scrobble/stop")
    suspend fun scrobbleStop(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") clientId: String,
        @Header("trakt-api-version") version: String = "2",
        @Body body: TraktScrobbleBody
    ): TraktScrobbleResponse

    @POST("oauth/device/code")
    suspend fun getDeviceCode(
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: TraktDeviceCodeBody
    ): TraktDeviceCodeResponse

    @POST("oauth/device/token")
    suspend fun pollDeviceToken(
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: TraktDeviceTokenBody
    ): TraktTokenResponse
}

data class TraktWatchlistResponse(
    val rank: Int = 0,
    val movie: TraktMovieItem? = null,
    val show: TraktShowItem? = null
)

data class TraktHistoryResponse(
    @SerializedName("watched_at") val watchedAt: String = "",
    val movie: TraktMovieItem? = null,
    val show: TraktShowItem? = null,
    val episode: TraktEpisodeItem? = null
)

data class TraktMovieItem(
    val title: String = "",
    val year: Int = 0,
    val ids: TraktIds = TraktIds()
)

data class TraktShowItem(
    val title: String = "",
    val year: Int = 0,
    val ids: TraktIds = TraktIds()
)

data class TraktEpisodeItem(
    val season: Int = 0,
    val number: Int = 0,
    val title: String = "",
    val ids: TraktIds = TraktIds()
)

data class TraktIds(
    val trakt: Int = 0,
    val imdb: String = "",
    val tmdb: Int = 0,
    val slug: String = ""
)

data class TraktScrobbleBody(
    val movie: TraktScrobbleMovie? = null,
    val show: TraktScrobbleShow? = null,
    val episode: TraktScrobbleEpisode? = null,
    val progress: Float = 0f
)

data class TraktScrobbleMovie(val title: String, val year: Int, val ids: TraktIds)
data class TraktScrobbleShow(val title: String, val ids: TraktIds)
data class TraktScrobbleEpisode(val season: Int, val number: Int)
data class TraktScrobbleResponse(val id: Long = 0, val action: String = "")

data class TraktDeviceCodeBody(
    @SerializedName("client_id") val clientId: String
)

data class TraktDeviceCodeResponse(
    @SerializedName("device_code") val deviceCode: String = "",
    @SerializedName("user_code") val userCode: String = "",
    @SerializedName("verification_url") val verificationUrl: String = "",
    @SerializedName("expires_in") val expiresIn: Int = 0,
    @SerializedName("interval") val interval: Int = 5
)

data class TraktDeviceTokenBody(
    val code: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)

data class TraktTokenResponse(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = "",
    @SerializedName("expires_in") val expiresIn: Long = 0
)
