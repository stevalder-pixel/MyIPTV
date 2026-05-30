package com.nextgen.iptv.ui.livetv

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nextgen.iptv.databinding.FragmentLiveTvBinding
import com.nextgen.iptv.ui.player.PlayerActivity
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LiveTvFragment : Fragment() {

    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveTvViewModel by viewModels()
    private lateinit var channelAdapter: ChannelAdapter
    private var categoryAdapter: CategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelAdapter = ChannelAdapter { channel ->
            // Update now playing panel
            binding.nowPlayingChannel.text = channel.name
            binding.nowPlayingTitle.text = "Tap to play"
            binding.nowPlayingTime.text = channel.url

            // Load channel logo as backdrop
            if (channel.logo.isNotEmpty()) {
                Glide.with(this)
                    .load(channel.logo)
                    .centerCrop()
                    .into(binding.nowPlayingBackdrop)
            }

            // Launch player
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.url)
                putExtra(PlayerActivity.EXTRA_TITLE, channel.name)
                putExtra(PlayerActivity.EXTRA_IS_LIVE, true)
            }
            startActivity(intent)
        }

        binding.channelRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = channelAdapter
            setHasFixedSize(true)
        }

        binding.epgRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Search
        binding.channelSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchChannels(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        observeViewModel()
        loadChannels()
    }

    private fun loadChannels() {
        lifecycleScope.launch {
            val ctx = requireContext()
            val url = AppPreferences.getXtreamUrl(ctx).first()
            val user = AppPreferences.getXtreamUsername(ctx).first()
            val pass = AppPreferences.getXtreamPassword(ctx).first()

            if (url.isNotEmpty() && user.isNotEmpty()) {
                viewModel.configure(url, user, pass)
                viewModel.loadChannels()
            } else {
                binding.emptyText.text = "Go to Settings → add your Xtream Codes server"
                binding.emptyText.visibility = View.VISIBLE
            }
        }
    }

    private fun observeViewModel() {
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            channelAdapter.submitList(channels)
            binding.emptyText.visibility = if (channels.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val list = mutableListOf(Pair("all", "All"))
            list.addAll(categories.map { Pair(it.categoryId, it.categoryName) })
            categoryAdapter = CategoryAdapter(list) { categoryId ->
                viewModel.filterByCategory(categoryId)
            }
            binding.categoryRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryAdapter
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.emptyText.text = it
                binding.emptyText.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
