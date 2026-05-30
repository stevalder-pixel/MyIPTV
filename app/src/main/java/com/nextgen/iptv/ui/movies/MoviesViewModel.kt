package com.nextgen.iptv.ui.movies

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.iptv.data.api.ApiClient
import com.nextgen.iptv.data.repository.TraktRepository
import com.nextgen.iptv.ui.common.MediaItem
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {
    private val _trendingMovies = MutableLiveData<List<MediaItem>>()
    val trendingMovies: LiveData<List<MediaItem>> = _trendingMovies

    private val _popularMovies = MutableLiveData<List<MediaItem>>()
    val popularMovies: LiveData<List<MediaItem>> = _popularMovies

    private val _topRatedMovies = MutableLiveData<List<MediaItem>>()
    val topRatedMovies: LiveData<List<MediaItem>> = _topRatedMovies

    private val _watchlist = MutableLiveData<List<MediaItem>>()
    val watchlist: LiveData<List<MediaItem>> = _watchlist

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _navigateToDetail = MutableLiveData<MediaItem?>()
    val navigateToDetail: LiveData<MediaItem?> = _navigateToDetail

    fun onItemSelected(item: MediaItem) { _navigateToDetail.value = item }
    fun onNavigationHandled() { _navigateToDetail.value = null }

    fun load(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val key = AppPreferences.getTmdbApiKey(context).first()
            if (key.isNotEmpty()) {
                try {
                    val trending = ApiClient.tmdb.getTrendingMovies(key)
                    _trendingMovies.value = trending.results.map {
                        MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.releaseDate.take(4), it.voteAverage, "movie", it.id.toString())
                    }
                } catch (e: Exception) { }

                try {
                    val popular = ApiClient.tmdb.getPopularMovies(key)
                    _popularMovies.value = popular.results.map {
                        MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.releaseDate.take(4), it.voteAverage, "movie", it.id.toString())
                    }
                } catch (e: Exception) { }

                try {
                    val topRated = ApiClient.tmdb.getTopRatedMovies(key)
                    _topRatedMovies.value = topRated.results.map {
                        MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.releaseDate.take(4), it.voteAverage, "movie", it.id.toString())
                    }
                } catch (e: Exception) { }
            }

            // Load Trakt watchlist
            try {
                val watchlistResult = TraktRepository.instance.getWatchlist(context)
                watchlistResult.getOrNull()?.filter { it.type == "movie" }?.let { items ->
                    _watchlist.value = items.map {
                        MediaItem(it.tmdbId, it.title, "", "", "",
                            it.year.toString(), 0f, "movie", it.imdbId)
                    }
                }
            } catch (e: Exception) { }

            _isLoading.value = false
        }
    }
}
