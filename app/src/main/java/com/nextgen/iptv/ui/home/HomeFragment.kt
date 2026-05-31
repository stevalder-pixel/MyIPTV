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
import com.nextgen.iptv.ui.movies.DetailFragment
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

        // Pre-install Cinemeta
        lifecycleScope.launch {
            val addons = AppPreferences.getStremioAddons(requireContext()).first()
            if (addons.isEmpty()) {
                AppPreferences.addStremioAddon(requireContext(), "https://v3-cinemeta.strem.io/manifest.json")
            }
        }

        val moviesAdapter = MediaRowAdapter(
            { item -> openDetail(item) }
        )
        val showsAdapter = MediaRowAdapter(
            { item -> openDetail(item) }
        )
        val watchlistAdapter = MediaRowAdapter(
            { item -> openDetail(item) }
        )

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

        binding.heroPlayBtn.setOnClickListener { heroItem?.let { openDetail(it) } }
        binding.heroInfoBtn.setOnClickListener { heroItem?.let { openDetail(it) } }

        lifecycleScope.launch {
            val savedKey = AppPreferences.getTmdbApiKey(requireContext()).first()
            val key = savedKey.ifEmpty { ApiClient.TMDB_KEY }

            if (key.isNotEmpty() && key != "YOUR_TMDB_KEY_HERE") {
                try {
                    val movies = ApiClient.tmdb.getTrendingMovies(key)
                    val movieItems = movies.results.map {
                        MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.releaseDate.take(4), it.voteAverage, "movie", "")
                    }
                    moviesAdapter.submitList(movieItems)
                    movieItems.firstOrNull()?.let { updateHero(it) }
                } catch (e: Exception) { }

                try {
                    val shows = ApiClient.tmdb.getTrendingShows(key)
                    showsAdapter.submitList(shows.results.map {
                        MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                            ApiClient.backdropUrl(it.backdropPath), it.overview,
                            it.firstAirDate.take(4), it.voteAverage, "series", "")
                    })
                } catch (e: Exception) { }
            } else {
                binding.heroTitle.text = "MyIPTV Hub"
                binding.heroOverview.text = "Add your TMDB API key in Settings"
            }

            try {
                TraktRepository.instance.getWatchlist(requireContext()).getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        binding.watchlistSection.visibility = View.VISIBLE
                        watchlistAdapter.submitList(items.map {
                            MediaItem(it.tmdbId, it.title, "", "", "",
                                it.year.toString(), 0f, it.type, it.imdbId)
                        })
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun updateHero(item: MediaItem) {
        heroItem = item
        if (item.backdropUrl.isNotEmpty()) {
            Glide.with(this).load(item.backdropUrl).centerCrop().into(binding.heroBackdrop)
        }
        binding.heroTitle.text = item.title
        binding.heroRating.text = if (item.rating > 0) "★ " + "%.1f".format(item.rating) else ""
        binding.heroYear.text = item.year
        binding.heroOverview.text = item.overview
    }

    private fun openDetail(item: MediaItem) {
        DetailFragment.newInstance(item).show(parentFragmentManager, "detail")
    }


    override fun onResume() {
        super.onResume()
        _binding?.trendingMoviesRow?.scrollToPosition(0)
        _binding?.trendingShowsRow?.scrollToPosition(0)
        _binding?.trendingMoviesRow?.postDelayed({
            _binding?.trendingMoviesRow?.layoutManager?.findViewByPosition(0)?.requestFocus()
        }, 300)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
