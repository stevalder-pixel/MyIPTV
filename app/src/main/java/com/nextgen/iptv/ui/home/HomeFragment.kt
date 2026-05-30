package com.nextgen.iptv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nextgen.iptv.data.api.ApiClient
import com.nextgen.iptv.data.repository.TraktRepository
import com.nextgen.iptv.databinding.FragmentHomeBinding
import com.nextgen.iptv.ui.common.MediaItem
import com.nextgen.iptv.ui.movies.DetailBottomSheet
import com.nextgen.iptv.ui.movies.MediaRowAdapter
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var heroItem: MediaItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moviesAdapter = MediaRowAdapter { item ->
            DetailBottomSheet.newInstance(item).show(parentFragmentManager, "detail")
        }
        val showsAdapter = MediaRowAdapter { item ->
            DetailBottomSheet.newInstance(item).show(parentFragmentManager, "detail")
        }
        val watchlistAdapter = MediaRowAdapter { item ->
            DetailBottomSheet.newInstance(item).show(parentFragmentManager, "detail")
        }

        binding.trendingMoviesRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        binding.trendingShowsRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }
        binding.watchlistRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = watchlistAdapter
        }

        binding.heroPlayBtn.setOnClickListener {
            heroItem?.let { DetailBottomSheet.newInstance(it).show(parentFragmentManager, "detail") }
        }

        binding.heroInfoBtn.setOnClickListener {
            heroItem?.let { DetailBottomSheet.newInstance(it).show(parentFragmentManager, "detail") }
        }

        lifecycleScope.launch {
            val key = AppPreferences.getTmdbApiKey(requireContext()).first()

            if (key.isNotEmpty()) {
                try {
                    val movies = ApiClient.tmdb.getTrendingMovies(key)
                    val movieItems = movies.results.map {
                        MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.releaseDate.take(4), it.voteAverage, "movie",
                            "tt${it.id}")
                    }
                    moviesAdapter.submitList(movieItems)

                    // Set hero to first trending movie
                    movieItems.firstOrNull()?.let { hero ->
                        heroItem = hero
                        Glide.with(this@HomeFragment)
                            .load(hero.backdropUrl)
                            .into(binding.heroBackdrop)
                        binding.heroTitle.text = hero.title
                        binding.heroRating.text = "★ ${"%.1f".format(hero.rating)}"
                        binding.heroYear.text = hero.year
                        binding.heroOverview.text = hero.overview
                    }
                } catch (e: Exception) { }

                try {
                    val shows = ApiClient.tmdb.getTrendingShows(key)
                    showsAdapter.submitList(shows.results.map {
                        MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.firstAirDate.take(4), it.voteAverage, "series",
                            "tt${it.id}")
                    })
                } catch (e: Exception) { }
            } else {
                binding.heroTitle.text = "MyIPTV Hub"
                binding.heroOverview.text = "Add your TMDB API key in Settings to see content"
            }

            // Load Trakt watchlist
            try {
                val watchlist = TraktRepository.instance.getWatchlist(requireContext())
                watchlist.getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        binding.watchlistSection.visibility = View.VISIBLE
                        watchlistAdapter.submitList(items.map {
                            MediaItem(it.tmdbId, it.title, "", "",
                                "", it.year.toString(), 0f, it.type, it.imdbId)
                        })
                    }
                }
            } catch (e: Exception) { }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
