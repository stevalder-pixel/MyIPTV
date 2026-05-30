package com.nextgen.iptv.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nextgen.iptv.databinding.FragmentSettingsBinding
import com.nextgen.iptv.data.repository.TraktRepository
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var addonAdapter: AddonAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddonList()
        loadValues()
        setupButtons()
    }

    private fun setupAddonList() {
        addonAdapter = AddonAdapter { url ->
            lifecycleScope.launch {
                AppPreferences.removeStremioAddon(requireContext(), url)
                loadAddonList()
                toast("Addon removed")
            }
        }
        binding.addonsList.layoutManager = LinearLayoutManager(requireContext())
        binding.addonsList.adapter = addonAdapter
    }

    private fun loadAddonList() {
        lifecycleScope.launch {
            val addons = AppPreferences.getStremioAddons(requireContext()).first()
            addonAdapter.submitList(addons)
            binding.addonsEmptyText.visibility = if (addons.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun loadValues() {
        lifecycleScope.launch {
            val ctx = requireContext()
            binding.tmdbApiKeyInput.setText(AppPreferences.getTmdbApiKey(ctx).first() as CharSequence)
            binding.rdApiKeyInput.setText(AppPreferences.getRdApiKey(ctx).first() as CharSequence)
            binding.torboxApiKeyInput.setText(AppPreferences.getTorBoxApiKey(ctx).first() as CharSequence)
            binding.alldebridApiKeyInput.setText(AppPreferences.getAllDebridApiKey(ctx).first() as CharSequence)
            binding.premiumizeApiKeyInput.setText(AppPreferences.getPremiumizeApiKey(ctx).first() as CharSequence)
            binding.m3uUrlInput.setText(AppPreferences.getM3uUrl(ctx).first() as CharSequence)
            binding.stalkerUrlInput.setText(AppPreferences.getStalkerPortalUrl(ctx).first() as CharSequence)
            binding.stalkerMacInput.setText(AppPreferences.getStalkerMac(ctx).first() as CharSequence)
            binding.xtreamUrlInput.setText(AppPreferences.getXtreamUrl(ctx).first() as CharSequence)
            binding.xtreamUsernameInput.setText(AppPreferences.getXtreamUsername(ctx).first() as CharSequence)
            binding.xtreamPasswordInput.setText(AppPreferences.getXtreamPassword(ctx).first() as CharSequence)
            binding.autoScrobbleSwitch.isChecked = AppPreferences.getAutoScrobble(ctx).first()

            // Active debrid selector
            val active = AppPreferences.getActiveDebrid(ctx).first()
            when (active) {
                "realdebrid" -> binding.debridGroup.check(binding.rdRadio.id)
                "torbox" -> binding.debridGroup.check(binding.torboxRadio.id)
                "alldebrid" -> binding.debridGroup.check(binding.alldebridRadio.id)
                "premiumize" -> binding.debridGroup.check(binding.premiumizeRadio.id)
            }

            // Trakt status
            val traktToken = AppPreferences.getTraktAccessToken(ctx).first()
            binding.traktStatusText.text = if (traktToken.isNotEmpty()) "✓ Connected" else "Not connected"
            binding.traktStatusText.setTextColor(
                if (traktToken.isNotEmpty()) 0xFF48CAE4.toInt() else 0xFFB0BEC5.toInt()
            )

            loadAddonList()
        }
    }

    private fun setupButtons() {
        // TMDB
        binding.saveTmdbBtn.setOnClickListener {
            save { AppPreferences.setTmdbApiKey(requireContext(), binding.tmdbApiKeyInput.text.toString()) }
            toast("TMDB saved")
        }

        // Active debrid
        binding.debridGroup.setOnCheckedChangeListener { _, checkedId ->
            val service = when (checkedId) {
                binding.rdRadio.id -> "realdebrid"
                binding.torboxRadio.id -> "torbox"
                binding.alldebridRadio.id -> "alldebrid"
                binding.premiumizeRadio.id -> "premiumize"
                else -> "torbox"
            }
            save { AppPreferences.setActiveDebrid(requireContext(), service) }
        }

        // Debrid API keys
        binding.saveRdBtn.setOnClickListener { save { AppPreferences.setRdApiKey(requireContext(), binding.rdApiKeyInput.text.toString()) }; toast("Real-Debrid saved") }
        binding.saveTorboxBtn.setOnClickListener { save { AppPreferences.setTorBoxApiKey(requireContext(), binding.torboxApiKeyInput.text.toString()) }; toast("TorBox saved") }
        binding.saveAlldebridBtn.setOnClickListener { save { AppPreferences.setAllDebridApiKey(requireContext(), binding.alldebridApiKeyInput.text.toString()) }; toast("AllDebrid saved") }
        binding.savePremiumizeBtn.setOnClickListener { save { AppPreferences.setPremiumizeApiKey(requireContext(), binding.premiumizeApiKeyInput.text.toString()) }; toast("Premiumize saved") }

        // Trakt device login
        binding.traktLoginBtn.setOnClickListener { startTraktLogin() }
        binding.traktLogoutBtn.setOnClickListener {
            save { AppPreferences.setTraktTokens(requireContext(), "", "") }
            binding.traktStatusText.text = "Not connected"
            binding.traktStatusText.setTextColor(0xFFB0BEC5.toInt())
            toast("Logged out of Trakt")
        }

        binding.autoScrobbleSwitch.setOnCheckedChangeListener { _, checked ->
            save { AppPreferences.setAutoScrobble(requireContext(), checked) }
        }

        // Stremio addons
        binding.addAddonBtn.setOnClickListener {
            val url = binding.addonUrlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                lifecycleScope.launch {
                    AppPreferences.addStremioAddon(requireContext(), url)
                    binding.addonUrlInput.setText("")
                    loadAddonList()
                    toast("Addon added!")
                }
            } else {
                toast("Enter an addon manifest URL")
            }
        }

        // M3U
        binding.saveM3uBtn.setOnClickListener { save { AppPreferences.setM3uUrl(requireContext(), binding.m3uUrlInput.text.toString()) }; toast("M3U saved") }

        // Stalker
        binding.saveStalkerBtn.setOnClickListener {
            save { AppPreferences.setStalkerPortal(requireContext(), binding.stalkerUrlInput.text.toString(), binding.stalkerMacInput.text.toString()) }
            toast("Stalker saved")
        }

        // Xtream
        binding.saveXtreamBtn.setOnClickListener {
            save { AppPreferences.setXtreamCredentials(requireContext(), binding.xtreamUrlInput.text.toString(), binding.xtreamUsernameInput.text.toString(), binding.xtreamPasswordInput.text.toString()) }
            toast("Xtream saved!")
        }
    }

    private fun startTraktLogin() {
        lifecycleScope.launch {
            binding.traktLoginBtn.isEnabled = false
            binding.traktLoginBtn.text = "Getting code..."
            try {
                val codeResult = TraktRepository.instance.getDeviceCode()
                if (codeResult.isFailure) {
                    toast("Failed to get Trakt code")
                    binding.traktLoginBtn.isEnabled = true
                    binding.traktLoginBtn.text = "Login with Trakt"
                    return@launch
                }

                val codeResponse = codeResult.getOrNull()!!
                binding.traktCodeText.text = "Go to: " + codeResponse.verificationUrl + "
Enter code: " + codeResponse.userCode
Enter code: " + codeResponse.userCode
                binding.traktCodeText.visibility = View.VISIBLE
                binding.traktLoginBtn.text = "Waiting..."

                // Poll for token
                var attempts = 0
                val maxAttempts = codeResponse.expiresIn / codeResponse.interval
                while (attempts < maxAttempts) {
                    delay(codeResponse.interval * 1000L)
                    val tokenResult = TraktRepository.instance.pollDeviceToken(codeResponse.deviceCode)
                    if (tokenResult.isSuccess) {
                        val tokens = tokenResult.getOrNull()!!
                        AppPreferences.setTraktTokens(requireContext(), tokens.accessToken, tokens.refreshToken)
                        binding.traktStatusText.text = "✓ Connected"
                        binding.traktStatusText.setTextColor(0xFF48CAE4.toInt())
                        binding.traktCodeText.visibility = View.GONE
                        binding.traktLoginBtn.text = "Login with Trakt"
                        binding.traktLoginBtn.isEnabled = true
                        toast("Trakt connected!")
                        return@launch
                    }
                    attempts++
                }
                toast("Trakt login timed out")
                binding.traktCodeText.visibility = View.GONE
                binding.traktLoginBtn.text = "Login with Trakt"
                binding.traktLoginBtn.isEnabled = true
            } catch (e: Exception) {
                toast("Trakt error: ${e.message}")
                binding.traktLoginBtn.isEnabled = true
                binding.traktLoginBtn.text = "Login with Trakt"
            }
        }
    }

    private fun save(block: suspend () -> Unit) { lifecycleScope.launch { block() } }
    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
