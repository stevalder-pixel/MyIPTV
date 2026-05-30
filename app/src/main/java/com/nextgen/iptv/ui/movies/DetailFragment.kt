package com.nextgen.iptv.ui.movies

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.nextgen.iptv.R
import com.nextgen.iptv.data.repository.DebridRepository
import com.nextgen.iptv.data.repository.StremioRepository
import com.nextgen.iptv.databinding.FragmentDetailBinding
import com.nextgen.iptv.ui.common.MediaItem
import com.nextgen.iptv.ui.player.PlayerActivity
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailFragment : DialogFragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(item: MediaItem): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", item.id)
                    putString("title", item.title)
                    putString("overview", item.overview)
                    putString("poster", item.posterUrl)
                    putString("backdrop", item.backdropUrl)
                    putString("year", item.year)
                    putFloat("rating", item.rating)
                    putString("type", item.type)
                    putString("stremio_id", item.stremioId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MyIPTV_FullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title") ?: ""
        val overview = arguments?.getString("overview") ?: ""
        val backdrop = arguments?.getString("backdrop") ?: ""
        val poster = arguments?.getString("poster") ?: ""
        val year = arguments?.getString("year") ?: ""
        val rating = arguments?.getFloat("rating") ?: 0f
        val type = arguments?.getString("type") ?: "movie"
        val stremioId = arguments?.getString("stremio_id") ?: ""

        binding.detailTitle.text = title
        binding.detailOverview.text = overview
        binding.detailYear.text = year
        binding.detailRating.text = if (rating > 0) "★ " + "%.1f".format(rating) else ""

        val imageUrl = backdrop.ifEmpty { poster }
        if (imageUrl.isNotEmpty()) {
            Glide.with(this).load(imageUrl).centerCrop().into(binding.detailBackdrop)
        }

        binding.closeBtn.setOnClickListener { dismiss() }

        binding.playBtn.setOnClickListener {
            findAndPlay(type, stremioId, title)
        }

        binding.watchlistBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Added to watchlist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findAndPlay(type: String, stremioId: String, title: String) {
        binding.playBtn.isEnabled = false
        binding.playBtn.text = "Finding stream..."
        binding.loadingBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val addons = AppPreferences.getStremioAddons(requireContext()).first()
                val allAddons = if (addons.isEmpty())
                    listOf("https://v3-cinemeta.strem.io")
                else addons.map { it.replace("/manifest.json", "") }

                var played = false
                for (addonUrl in allAddons) {
                    if (played) break
                    val result = StremioRepository.instance.getStreams(addonUrl, type, stremioId)
                    val streams = result.getOrNull()?.streams ?: continue
                    val stream = streams.firstOrNull { it.url.isNotEmpty() }
                        ?: streams.firstOrNull { it.infoHash.isNotEmpty() }
                        ?: continue

                    val url = if (stream.url.isNotEmpty()) stream.url
                              else "magnet:?xt=urn:btih:" + stream.infoHash

                    if (url.startsWith("http")) {
                        launchPlayer(url, title)
                        played = true
                    } else {
                        val resolved = DebridRepository.instance.resolveStream(requireContext(), url)
                        val resolvedUrl = resolved.getOrNull()?.url
                        if (!resolvedUrl.isNullOrEmpty()) {
                            launchPlayer(resolvedUrl, title)
                            played = true
                        }
                    }
                }

                if (!played) {
                    binding.playBtn.isEnabled = true
                    binding.playBtn.text = "▶  Play"
                    binding.loadingBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "No streams found — add a Stremio addon in Settings", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.playBtn.isEnabled = true
                binding.playBtn.text = "▶  Play"
                binding.loadingBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
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
