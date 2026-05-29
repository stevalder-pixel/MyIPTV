package com.nextgen.iptv.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TmdbApiService {
    @GET("trending/movie/week") suspend fun getTrendingMovies(@Query("api_key") k: String): TmdbMovieResponse
    @GET("movie/popular") suspend fun getPopularMovies(@Query("api_key") k: String): TmdbMovieResponse
    @GET("movie/top_rated") suspend fun getTopRatedMovies(@Query("api_key") k: String): TmdbMovieResponse
    @GET("trending/tv/week") suspend fun getTrendingShows(@Query("api_key") k: String): TmdbTvResponse
    @GET("tv/popular") suspend fun getPopularShows(@Query("api_key") k: String): TmdbTvResponse
    @GET("tv/top_rated") suspend fun getTopRatedShows(@Query("api_key") k: String): TmdbTvResponse
}

data class TmdbMovieResponse(val results: List<TmdbMovieResult>)
data class TmdbMovieResult(val id: Int, val title: String, val overview: String, @SerializedName("poster_path") val posterPath: String?, @SerializedName("backdrop_path") val backdropPath: String?, @SerializedName("release_date") val releaseDate: String = "", @SerializedName("vote_average") val voteAverage: Float = 0f)
data class TmdbTvResponse(val results: List<TmdbTvResult>)
data class TmdbTvResult(val id: Int, val name: String, val overview: String, @SerializedName("poster_path") val posterPath: String?, @SerializedName("backdrop_path") val backdropPath: String?, @SerializedName("first_air_date") val firstAirDate: String = "", @SerializedName("vote_average") val voteAverage: Float = 0f)

object ApiClient {
    val tmdb: TmdbApiService by lazy {
        Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/")
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }).connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(TmdbApiService::class.java)
    }
    fun posterUrl(path: String?) = if (!path.isNullOrEmpty()) "https://image.tmdb.org/t/p/w342$path" else ""
    fun backdropUrl(path: String?) = if (!path.isNullOrEmpty()) "https://image.tmdb.org/t/p/w780$path" else ""
}
