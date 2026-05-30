package com.nextgen.iptv.ui.tv

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

class TvShowsViewModel : ViewModel() {
    private val _trendingShows = MutableLiveData<List<MediaItem>>()
    val trendingShows: LiveData<List<MediaItem>> = _trendingShows

    private val _popularShows = MutableLiveData<List<MediaItem>>()
    val popularShows: LiveData<List<MediaItem>> = _popularShows

    private val _topRatedShows = MutableLiveData<List<MediaItem>>()
    val topRatedShows: LiveData<List<MediaItem>> = _topRatedShows

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
            val key = AppPreferences.getTmdbApiKey(context).first().ifEmpty { "0d5f6d8e07ab385be6c228b7950798bf" }
            if (key.isNotEmpty()) {
                try {
                    _trendingShows.value = ApiClient.tmdb.getTrendingShows(key).results.map {
                        MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.firstAirDate.take(4), it.voteAverage, "series", it.id.toString())
                    }
                } catch (e: Exception) { }

                try {
                    _popularShows.value = ApiClient.tmdb.getPopularShows(key).results.map {
                        MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.firstAirDate.take(4), it.voteAverage, "series", it.id.toString())
                    }
                } catch (e: Exception) { }

                try {
                    _topRatedShows.value = ApiClient.tmdb.getTopRatedShows(key).results.map {
                        MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.firstAirDate.take(4), it.voteAverage, "series", it.id.toString())
                    }
                } catch (e: Exception) { }
            }

            try {
                TraktRepository.instance.getWatchlist(context).getOrNull()
                    ?.filter { it.type == "show" }?.let { items ->
                    _watchlist.value = items.map {
                        MediaItem(it.tmdbId, it.title, "", "", "",
                            it.year.toString(), 0f, "series", it.imdbId)
                    }
                }
            } catch (e: Exception) { }

            _isLoading.value = false
        }
    }
}
