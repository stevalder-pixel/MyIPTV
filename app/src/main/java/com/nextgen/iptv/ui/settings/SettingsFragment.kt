package com.nextgen.iptv.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nextgen.iptv.databinding.FragmentSettingsBinding
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            binding.tmdbApiKeyInput.setText(AppPreferences.getTmdbApiKey(requireContext()).first())
            binding.rdApiKeyInput.setText(AppPreferences.getRdApiKey(requireContext()).first())
            binding.torboxApiKeyInput.setText(AppPreferences.getTorBoxApiKey(requireContext()).first())
            binding.alldebridApiKeyInput.setText(AppPreferences.getAllDebridApiKey(requireContext()).first())
            binding.premiumizeApiKeyInput.setText(AppPreferences.getPremiumizeApiKey(requireContext()).first())
            binding.m3uUrlInput.setText(AppPreferences.getM3uUrl(requireContext()).first())
            binding.stalkerUrlInput.setText(AppPreferences.getStalkerPortalUrl(requireContext()).first())
            binding.stalkerMacInput.setText(AppPreferences.getStalkerMac(requireContext()).first())
        }
        binding.saveTmdbBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setTmdbApiKey(requireContext(), binding.tmdbApiKeyInput.text.toString()); toast("TMDB saved") } }
        binding.saveRdBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setRdApiKey(requireContext(), binding.rdApiKeyInput.text.toString()); toast("Real-Debrid saved") } }
        binding.saveTorboxBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setTorBoxApiKey(requireContext(), binding.torboxApiKeyInput.text.toString()); toast("TorBox saved") } }
        binding.saveAlldebridBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setAllDebridApiKey(requireContext(), binding.alldebridApiKeyInput.text.toString()); toast("AllDebrid saved") } }
        binding.savePremiumizeBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setPremiumizeApiKey(requireContext(), binding.premiumizeApiKeyInput.text.toString()); toast("Premiumize saved") } }
        binding.saveM3uBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setM3uUrl(requireContext(), binding.m3uUrlInput.text.toString()); toast("M3U saved") } }
        binding.saveStalkerBtn.setOnClickListener { lifecycleScope.launch { AppPreferences.setStalkerPortal(requireContext(), binding.stalkerUrlInput.text.toString(), binding.stalkerMacInput.text.toString()); toast("Stalker saved") } }
        binding.traktLoginBtn.setOnClickListener { toast("Trakt login coming soon") }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}