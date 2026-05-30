package com.nextgen.iptv.ui.movies

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nextgen.iptv.data.repository.DebridRepository
import com.nextgen.iptv.data.repository.StremioRepository
import com.nextgen.iptv.databinding.BottomSheetDetailBinding
import com.nextgen.iptv.ui.common.MediaItem
import com.nextgen.iptv.ui.player.PlayerActivity
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM = "media_item"
        fun newInstance(item: MediaItem): DetailBottomSheet {
            return DetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("title", item.title)
                    putString("overview", item.overview)
                    putString("poster", item.posterUrl)
                    putString("backdrop", item.backdropUrl)
                    putString("year", item.year)
                    putFloat("rating", item.rating)
                    putString("type", item.type)
                    putString("stremio_id", item.stremioId)
                    putInt("id", item.id)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title") ?: ""
        val overview = arguments?.getString("overview") ?: ""
        val poster = arguments?.getString("poster") ?: ""
        val backdrop = arguments?.getString("backdrop") ?: ""
        val year = arguments?.getString("year") ?: ""
        val rating = arguments?.getFloat("rating") ?: 0f
        val type = arguments?.getString("type") ?: "movie"
        val stremioId = arguments?.getString("stremio_id") ?: ""

        binding.detailTitle.text = title
        binding.detailOverview.text = overview
        binding.detailYear.text = year
        binding.detailRating.text = if (rating > 0) "★ ${"%.1f".format(rating)}" else ""

        if (backdrop.isNotEmpty()) {
            Glide.with(this).load(backdrop).into(binding.detailBackdrop)
        } else if (poster.isNotEmpty()) {
            Glide.with(this).load(poster).into(binding.detailBackdrop)
        }

        binding.playBtn.setOnClickListener {
            findAndPlayStream(type, stremioId, title)
        }

        binding.watchlistBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Added to Trakt watchlist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findAndPlayStream(type: String, stremioId: String, title: String) {
        binding.playBtn.isEnabled = false
        binding.playBtn.text = "Finding stream..."

        lifecycleScope.launch {
            try {
                val addons = AppPreferences.getStremioAddons(requireContext()).first()

                if (addons.isEmpty()) {
                    // Try Cinemeta as default
                    tryStreamFromAddon("https://v3-cinemeta.strem.io", type, stremioId, title)
                    return@launch
                }

                var found = false
                for (addonUrl in addons) {
                    if (found) break
                    val baseUrl = addonUrl.replace("/manifest.json", "")
                    val result = StremioRepository.instance.getStreams(baseUrl, type, stremioId)
                    result.getOrNull()?.streams?.firstOrNull()?.let { stream ->
                        found = true
                        resolveAndPlay(stream.url.ifEmpty { stream.infoHash }, title)
                    }
                }

                if (!found) {
                    tryStreamFromAddon("https://v3-cinemeta.strem.io", type, stremioId, title)
                }
            } catch (e: Exception) {
                binding.playBtn.isEnabled = true
                binding.playBtn.text = "▶ Play"
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun tryStreamFromAddon(baseUrl: String, type: String, id: String, title: String) {
        val result = StremioRepository.instance.getStreams(baseUrl, type, id)
        result.getOrNull()?.streams?.firstOrNull()?.let { stream ->
            resolveAndPlay(stream.url.ifEmpty { "magnet:?xt=urn:btih:${stream.infoHash}" }, title)
        } ?: run {
            binding.playBtn.isEnabled = true
            binding.playBtn.text = "▶ Play"
            Toast.makeText(requireContext(), "No streams found", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun resolveAndPlay(url: String, title: String) {
        if (url.startsWith("http")) {
            // Direct stream - play immediately
            launchPlayer(url, title)
        } else {
            // Magnet/torrent - resolve with debrid
            val resolved = DebridRepository.instance.resolveStream(requireContext(), url)
            if (resolved.isSuccess && resolved.getOrNull()?.url?.isNotEmpty() == true) {
                launchPlayer(resolved.getOrNull()!!.url, title)
            } else {
                binding.playBtn.isEnabled = true
                binding.playBtn.text = "▶ Play"
                Toast.makeText(requireContext(), "Debrid resolution failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchPlayer(url: String, title: String) {
        dismiss()
        startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_URL, url)
            putExtra(PlayerActivity.EXTRA_TITLE, title)
            putExtra(PlayerActivity.EXTRA_IS_LIVE, false)
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
