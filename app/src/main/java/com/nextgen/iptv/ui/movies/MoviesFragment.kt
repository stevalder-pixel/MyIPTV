package com.nextgen.iptv.ui.movies

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
import com.nextgen.iptv.databinding.FragmentMoviesBinding
import com.nextgen.iptv.ui.common.MediaItem
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private var allRows = listOf<Pair<String, List<MediaItem>>>()
    private var currentRow = 0
    private val rowAdapter = MediaRowAdapter { DetailFragment.newInstance(it).show(parentFragmentManager, "detail") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rowAdapter.onItemFocused = { item ->
            if (item.backdropUrl.isNotEmpty())
                _binding?.let { Glide.with(this).load(item.backdropUrl).centerCrop().into(it.rowBackdrop) }
        }
        binding.activeRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = rowAdapter
            setOnKeyListener { _, k, e ->
                if (e.action == KeyEvent.ACTION_DOWN) when (k) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> { goToRow(currentRow + 1); true }
                    KeyEvent.KEYCODE_DPAD_UP -> { goToRow(currentRow - 1); true }
                    KeyEvent.KEYCODE_BACK -> { scrollToFirst(); true }
                    else -> false
                } else false
            }
        }
        loadContent()
    }

    private fun goToRow(i: Int) {
        if (i < 0 || i >= allRows.size) return
        currentRow = i
        binding.rowLabel.text = allRows[i].first
        rowAdapter.submitList(allRows[i].second)
        scrollToFirst()
        updateDots()
    }

    private fun scrollToFirst() {
        binding.activeRow.scrollToPosition(0)
        binding.activeRow.post { binding.activeRow.layoutManager?.findViewByPosition(0)?.requestFocus() }
    }

    private fun updateDots() {
        binding.rowIndicators.removeAllViews()
        allRows.forEachIndexed { i, _ ->
            binding.rowIndicators.addView(TextView(requireContext()).apply {
                text = if (i == currentRow) "●" else "○"
                setTextColor(if (i == currentRow) 0xFF48CAE4.toInt() else 0xFF607D8B.toInt())
                textSize = 10f; setPadding(4, 0, 4, 0)
            })
        }
    }

    private fun loadContent() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val key = AppPreferences.getTmdbApiKey(requireContext()).first().ifEmpty { "0d5f6d8e07ab385be6c228b7950798bf" }
            val rows = mutableListOf<Pair<String, List<MediaItem>>>()
            try { rows.add("Trending" to ApiClient.tmdb.getTrendingMovies(key).results.map { MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath), ApiClient.backdropUrl(it.backdropPath), it.overview, it.releaseDate.take(4), it.voteAverage, "movie", "") }) } catch (e: Exception) {}
            try { rows.add("Popular" to ApiClient.tmdb.getPopularMovies(key).results.map { MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath), ApiClient.backdropUrl(it.backdropPath), it.overview, it.releaseDate.take(4), it.voteAverage, "movie", "") }) } catch (e: Exception) {}
            try { rows.add("Top Rated" to ApiClient.tmdb.getTopRatedMovies(key).results.map { MediaItem(it.id, it.title, ApiClient.posterUrl(it.posterPath), ApiClient.backdropUrl(it.backdropPath), it.overview, it.releaseDate.take(4), it.voteAverage, "movie", "") }) } catch (e: Exception) {}
            allRows = rows
            binding.progressBar.visibility = View.GONE
            if (rows.isNotEmpty()) goToRow(0)
        }
    }

    override fun onResume() { super.onResume(); binding.activeRow.post { binding.activeRow.layoutManager?.findViewByPosition(0)?.requestFocus() } }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
