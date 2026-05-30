package com.nextgen.iptv.ui.tv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nextgen.iptv.databinding.FragmentTvShowsBinding
import com.nextgen.iptv.ui.movies.DetailBottomSheet
import com.nextgen.iptv.ui.movies.MediaRowAdapter

class TvShowsFragment : Fragment() {
    private var _binding: FragmentTvShowsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TvShowsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTvShowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trendingAdapter = MediaRowAdapter { viewModel.onItemSelected(it) }
        val popularAdapter = MediaRowAdapter { viewModel.onItemSelected(it) }
        val topRatedAdapter = MediaRowAdapter { viewModel.onItemSelected(it) }
        val watchlistAdapter = MediaRowAdapter { viewModel.onItemSelected(it) }

        binding.trendingRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = trendingAdapter }
        binding.popularRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = popularAdapter }
        binding.topRatedRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = topRatedAdapter }
        binding.watchlistRow.apply { layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); adapter = watchlistAdapter }

        viewModel.trendingShows.observe(viewLifecycleOwner) { trendingAdapter.submitList(it) }
        viewModel.popularShows.observe(viewLifecycleOwner) { popularAdapter.submitList(it) }
        viewModel.topRatedShows.observe(viewLifecycleOwner) { topRatedAdapter.submitList(it) }
        viewModel.watchlist.observe(viewLifecycleOwner) { items ->
            watchlistAdapter.submitList(items)
            binding.watchlistSection.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.navigateToDetail.observe(viewLifecycleOwner) { item ->
            item?.let { DetailBottomSheet.newInstance(it).show(parentFragmentManager, "detail"); viewModel.onNavigationHandled() }
        }

        viewModel.load(requireContext())
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
