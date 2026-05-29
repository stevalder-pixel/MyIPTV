package com.nextgen.iptv.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nextgen.iptv.data.api.ApiClient
import com.nextgen.iptv.databinding.FragmentMoviesBinding
import com.nextgen.iptv.ui.home.PosterAdapter
import com.nextgen.iptv.ui.home.PosterItem
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val adapter = PosterAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.moviesGrid.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.moviesGrid.adapter = adapter
        binding.moviesTabs.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) { when (tab?.position) { 0 -> load("trending"); 1 -> load("popular"); 2 -> load("top") } }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
        load("trending")
    }

    private fun load(type: String) {
        lifecycleScope.launch {
            val key = AppPreferences.getTmdbApiKey(requireContext()).first()
            if (key.isEmpty()) return@launch
            binding.progressBar.visibility = View.VISIBLE
            try {
                val r = when (type) { "popular" -> ApiClient.tmdb.getPopularMovies(key).results; "top" -> ApiClient.tmdb.getTopRatedMovies(key).results; else -> ApiClient.tmdb.getTrendingMovies(key).results }
                adapter.submitList(r.map { PosterItem(it.id, it.title, ApiClient.posterUrl(it.posterPath), it.voteAverage, false) })
            } catch (e: Exception) { }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
