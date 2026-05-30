package com.nextgen.iptv.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nextgen.iptv.databinding.FragmentMoviesBinding

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoviesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trendingAdapter = MediaRowAdapter(onItemClick = { item -> DetailFragment.newInstance(item).show(parentFragmentManager, "detail") }
        val popularAdapter = MediaRowAdapter(onItemClick = { item -> DetailFragment.newInstance(item).show(parentFragmentManager, "detail") }
        val topRatedAdapter = MediaRowAdapter(onItemClick = { item -> DetailFragment.newInstance(item).show(parentFragmentManager, "detail") }
        val watchlistAdapter = MediaRowAdapter(onItemClick = { item -> DetailFragment.newInstance(item).show(parentFragmentManager, "detail") }

        binding.trendingRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = trendingAdapter }
        binding.popularRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = popularAdapter }
        binding.topRatedRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = topRatedAdapter }
        binding.watchlistRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = watchlistAdapter }

        viewModel.trendingMovies.observe(viewLifecycleOwner) { trendingAdapter.submitList(it) }
        viewModel.popularMovies.observe(viewLifecycleOwner) { popularAdapter.submitList(it) }
        viewModel.topRatedMovies.observe(viewLifecycleOwner) { topRatedAdapter.submitList(it) }
        viewModel.watchlist.observe(viewLifecycleOwner) { items ->
            watchlistAdapter.submitList(items)
            binding.watchlistSection.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.load(requireContext())
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
