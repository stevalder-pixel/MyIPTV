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
import com.nextgen.iptv.databinding.FragmentHomeBinding
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moviesAdapter = PosterAdapter()
        val showsAdapter = PosterAdapter()
        binding.trendingMoviesRow.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.trendingMoviesRow.adapter = moviesAdapter
        binding.trendingShowsRow.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.trendingShowsRow.adapter = showsAdapter

        lifecycleScope.launch {
            val key = AppPreferences.getTmdbApiKey(requireContext()).first()
            if (key.isEmpty()) return@launch
            try {
                val movies = ApiClient.tmdb.getTrendingMovies(key)
                val shows = ApiClient.tmdb.getTrendingShows(key)
                moviesAdapter.submitList(movies.results.map { PosterItem(it.id, it.title, ApiClient.posterUrl(it.posterPath), it.voteAverage, false) })
                showsAdapter.submitList(shows.results.map { PosterItem(it.id, it.name, ApiClient.posterUrl(it.posterPath), it.voteAverage, true) })
                movies.results.firstOrNull()?.let { hero ->
                    Glide.with(this@HomeFragment).load(ApiClient.backdropUrl(hero.backdropPath)).into(binding.heroBackdrop)
                    binding.heroTitle.text = hero.title
                    binding.heroRating.text = "★ ${"%.1f".format(hero.voteAverage)}"
                    binding.heroYear.text = hero.releaseDate.take(4)
                    binding.heroOverview.text = hero.overview
                }
            } catch (e: Exception) { }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
