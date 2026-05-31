package com.nextgen.iptv.ui.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private var allRows = listOf<Pair<String, List<MediaItem>>>()
    private var currentRow = 0
    private val rowAdapter = MediaRowAdapter { openDetail(it) }.also {
        it.onItemFocused = { item -> updateHero(item) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.activeRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = rowAdapter
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> { goToRow(currentRow + 1); true }
                    KeyEvent.KEYCODE_DPAD_UP -> { goToRow(currentRow - 1); true }
                    KeyEvent.KEYCODE_BACK -> { focusFirstItem(); true }
                    else -> false
                } else false
            }
        }

        binding.heroPlayBtn.setOnClickListener { heroItem?.let { openDetail(it) } }
        binding.heroInfoBtn.setOnClickListener { heroItem?.let { openDetail(it) } }
        loadContent()
    }

    fun goToRow(index: Int) {
        if (index < 0 || index >= allRows.size) return
        currentRow = index
        val (label, items) = allRows[currentRow]
        binding.rowLabel.text = label
        rowAdapter.submitList(items)
        binding.activeRow.scrollToPosition(0)
        binding.activeRow.post {
            binding.activeRow.layoutManager?.findViewByPosition(0)?.requestFocus()
        }
        items.firstOrNull()?.let { updateHero(it) }
        updateDots()
    }

    private fun focusFirstItem() {
        binding.activeRow.scrollToPosition(0)
        binding.activeRow.post {
            binding.activeRow.layoutManager?.findViewByPosition(0)?.requestFocus()
        }
    }

    private fun updateDots() {
        binding.rowIndicators.removeAllViews()
        allRows.forEachIndexed { i, _ ->
            val dot = TextView(requireContext()).apply {
                text = if (i == currentRow) "●" else "○"
                setTextColor(if (i == currentRow) 0xFF48CAE4.toInt() else 0xFF607D8B.toInt())
                textSize = 10f
                setPadding(4, 0, 4, 0)
            }
            binding.rowIndicators.addView(dot)
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

    private fun loadContent() {
        lifecycleScope.launch {
            val key = AppPreferences.getTmdbApiKey(requireContext()).first()
                .ifEmpty { "0d5f6d8e07ab385be6c228b7950798bf" }
            val rows = mutableListOf<Pair<String, List<MediaItem>>>()
            try {
                val movies = ApiClient.tmdb.getTrendingMovies(key).results.map {
                    MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                        ApiClient.backdropUrl(it.backdropPath), it.overview,
                        it.releaseDate.take(4), it.voteAverage, "movie", "")
                }
                rows.add(Pair("Trending Movies", movies))
            } catch (e: Exception) { }
            try {
                val popular = ApiClient.tmdb.getPopularMovies(key).results.map {
                    MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath),
                        ApiClient.backdropUrl(it.backdropPath), it.overview,
                        it.releaseDate.take(4), it.voteAverage, "movie", "")
                }
                rows.add(Pair("Popular Movies", popular))
            } catch (e: Exception) { }
            try {
                val shows = ApiClient.tmdb.getTrendingShows(key).results.map {
                    MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                        ApiClient.backdropUrl(it.backdropPath), it.overview,
                        it.firstAirDate.take(4), it.voteAverage, "series", "")
                }
                rows.add(Pair("Trending Shows", shows))
            } catch (e: Exception) { }
            try {
                val topShows = ApiClient.tmdb.getTopRatedShows(key).results.map {
                    MediaItem(it.id, it.name, ApiClient.posterUrl(it.posterPath),
                        ApiClient.backdropUrl(it.backdropPath), it.overview,
                        it.firstAirDate.take(4), it.voteAverage, "series", "")
                }
                rows.add(Pair("Top Rated Shows", topShows))
            } catch (e: Exception) { }
            try {
                TraktRepository.instance.getWatchlist(requireContext()).getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        rows.add(0, Pair("My Watchlist", items.map {
                            MediaItem(it.tmdbId, it.title, "", "", "",
                                it.year.toString(), 0f, it.type, it.imdbId)
                        }))
                    }
                }
            } catch (e: Exception) { }
            allRows = rows
            if (rows.isNotEmpty()) goToRow(0)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.activeRow.post {
            binding.activeRow.layoutManager?.findViewByPosition(0)?.requestFocus()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
