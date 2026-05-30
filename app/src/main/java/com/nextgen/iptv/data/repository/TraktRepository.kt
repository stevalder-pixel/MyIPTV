package com.nextgen.iptv.data.repository

import android.content.Context
import com.nextgen.iptv.data.api.*
import com.nextgen.iptv.data.models.TraktWatchlistItem
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TraktRepository {

    // Use your own Trakt client ID
    val CLIENT_ID = "YOUR_TRAKT_CLIENT_ID"
    val CLIENT_SECRET = "YOUR_TRAKT_CLIENT_SECRET"

    private val service: TraktApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.trakt.tv/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TraktApiService::class.java)
    }

    suspend fun getWatchlist(context: Context): Result<List<TraktWatchlistItem>> {
        return try {
            val token = AppPreferences.getTraktAccessToken(context).first()
            if (token.isEmpty()) return Result.failure(Exception("Not logged in to Trakt"))

            val movies = service.getMovieWatchlist("Bearer $token", CLIENT_ID)
            val shows = service.getShowWatchlist("Bearer $token", CLIENT_ID)

            val items = mutableListOf<TraktWatchlistItem>()
            movies.forEach { r ->
                r.movie?.let { m ->
                    items.add(TraktWatchlistItem(
                        rank = r.rank,
                        traktId = m.ids.trakt,
                        tmdbId = m.ids.tmdb,
                        imdbId = m.ids.imdb,
                        title = m.title,
                        year = m.year,
                        type = "movie"
                    ))
                }
            }
            shows.forEach { r ->
                r.show?.let { s ->
                    items.add(TraktWatchlistItem(
                        rank = r.rank,
                        traktId = s.ids.trakt,
                        tmdbId = s.ids.tmdb,
                        imdbId = s.ids.imdb,
                        title = s.title,
                        year = s.year,
                        type = "show"
                    ))
                }
            }
            Result.success(items.sortedBy { it.rank })
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun scrobbleStart(context: Context, title: String, year: Int, imdbId: String, progress: Float = 0f): Result<Unit> {
        return try {
            val token = AppPreferences.getTraktAccessToken(context).first()
            if (token.isEmpty()) return Result.success(Unit)
            val autoScrobble = AppPreferences.getAutoScrobble(context).first()
            if (!autoScrobble) return Result.success(Unit)

            service.scrobbleStart(
                "Bearer $token", CLIENT_ID,
                body = TraktScrobbleBody(
                    movie = TraktScrobbleMovie(title, year, TraktIds(imdb = imdbId)),
                    progress = progress
                )
            )
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun scrobbleStop(context: Context, title: String, year: Int, imdbId: String, progress: Float): Result<Unit> {
        return try {
            val token = AppPreferences.getTraktAccessToken(context).first()
            if (token.isEmpty()) return Result.success(Unit)
            val autoScrobble = AppPreferences.getAutoScrobble(context).first()
            if (!autoScrobble) return Result.success(Unit)

            service.scrobbleStop(
                "Bearer $token", CLIENT_ID,
                body = TraktScrobbleBody(
                    movie = TraktScrobbleMovie(title, year, TraktIds(imdb = imdbId)),
                    progress = progress
                )
            )
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getDeviceCode(): Result<TraktDeviceCodeResponse> = try {
        Result.success(service.getDeviceCode(body = TraktDeviceCodeBody(CLIENT_ID)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun pollDeviceToken(deviceCode: String): Result<TraktTokenResponse> = try {
        Result.success(service.pollDeviceToken(body = TraktDeviceTokenBody(deviceCode, CLIENT_ID, CLIENT_SECRET)))
    } catch (e: Exception) { Result.failure(e) }

    companion object { val instance = TraktRepository() }
}
