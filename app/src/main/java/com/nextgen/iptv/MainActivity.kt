package com.nextgen.iptv

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nextgen.iptv.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- TMDB Data Models Required for Parsing JSON ---
data class MovieResponse(
    val results: List<TmdbMovie>
)

data class TmdbMovie(
    val movieId: Int, // Renamed from 'id' to fix conflict with resource system
    val title: String,
    val poster_path: String?
)

// --- TMDB API Interface Required by Retrofit ---
interface TmdbApiService {
    @GET("search/movie")
    fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Call<MovieResponse>
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val posterImageView = findViewById<ImageView>(R.id.posterImageView)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val tmdbApiService = retrofit.create(TmdbApiService::class.java)

        // Fetch sample poster metadata (Example query: "Inception")
        tmdbApiService.searchMovie("YOUR_TMDB_API_KEY_HERE", "Inception").enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movie = response.body()?.results?.firstOrNull()
                    val posterPath = movie?.poster_path

                    if (!posterPath.isNullOrEmpty()) {
                        val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                        
                        // Load poster image via Glide library
                        Glide.with(this@MainActivity)
                            .load(fullPosterUrl)
                            .into(posterImageView)
                    }
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                // Handle image fetch failure gracefully
            }
        })
    }
}
