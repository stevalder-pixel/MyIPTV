package com.nextgen.iptv.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.nextgen.iptv.databinding.FragmentSettingsBinding
import com.nextgen.iptv.util.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var pendingQrTarget = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadValues()
        setupButtons()
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
        }
    }

    private fun setupButtons() {
        binding.saveTmdbBtn.setOnClickListener { save { AppPreferences.setTmdbApiKey(requireContext(), binding.tmdbApiKeyInput.text.toString()) }; toast("TMDB saved") }
        binding.saveRdBtn.setOnClickListener { save { AppPreferences.setRdApiKey(requireContext(), binding.rdApiKeyInput.text.toString()) }; toast("Real-Debrid saved") }
        binding.saveTorboxBtn.setOnClickListener { save { AppPreferences.setTorBoxApiKey(requireContext(), binding.torboxApiKeyInput.text.toString()) }; toast("TorBox saved") }
        binding.saveAlldebridBtn.setOnClickListener { save { AppPreferences.setAllDebridApiKey(requireContext(), binding.alldebridApiKeyInput.text.toString()) }; toast("AllDebrid saved") }
        binding.savePremiumizeBtn.setOnClickListener { save { AppPreferences.setPremiumizeApiKey(requireContext(), binding.premiumizeApiKeyInput.text.toString()) }; toast("Premiumize saved") }
        binding.saveM3uBtn.setOnClickListener { save { AppPreferences.setM3uUrl(requireContext(), binding.m3uUrlInput.text.toString()) }; toast("M3U saved") }
        binding.saveStalkerBtn.setOnClickListener { save { AppPreferences.setStalkerPortal(requireContext(), binding.stalkerUrlInput.text.toString(), binding.stalkerMacInput.text.toString()) }; toast("Stalker saved") }
        binding.saveXtreamBtn.setOnClickListener {
            save {
                AppPreferences.setXtreamCredentials(
                    requireContext(),
                    binding.xtreamUrlInput.text.toString(),
                    binding.xtreamUsernameInput.text.toString(),
                    binding.xtreamPasswordInput.text.toString()
                )
            }
            toast("Xtream credentials saved")
        }
        binding.autoScrobbleSwitch.setOnCheckedChangeListener { _, checked ->
            save { AppPreferences.setAutoScrobble(requireContext(), checked) }
        }
        binding.rdQrBtn.setOnClickListener { launchQr("rd") }
        binding.torboxQrBtn.setOnClickListener { launchQr("torbox") }
        binding.alldebridQrBtn.setOnClickListener { launchQr("alldebrid") }
        binding.premiumizeQrBtn.setOnClickListener { launchQr("premiumize") }
        binding.traktQrBtn.setOnClickListener { launchQr("trakt") }
        binding.traktLoginBtn.setOnClickListener { toast("Trakt OAuth coming soon") }
    }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) handleQrResult(result.contents)
    }

    private fun launchQr(target: String) {
        pendingQrTarget = target
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan QR for $target")
            setBeepEnabled(true)
        }
        scanLauncher.launch(options)
    }

    private fun handleQrResult(scanned: String) {
        when (pendingQrTarget) {
            "rd" -> { binding.rdApiKeyInput.setText(scanned as CharSequence); save { AppPreferences.setRdApiKey(requireContext(), scanned) }; toast("Real-Debrid scanned") }
            "torbox" -> { binding.torboxApiKeyInput.setText(scanned as CharSequence); save { AppPreferences.setTorBoxApiKey(requireContext(), scanned) }; toast("TorBox scanned") }
            "alldebrid" -> { binding.alldebridApiKeyInput.setText(scanned as CharSequence); save { AppPreferences.setAllDebridApiKey(requireContext(), scanned) }; toast("AllDebrid scanned") }
            "premiumize" -> { binding.premiumizeApiKeyInput.setText(scanned as CharSequence); save { AppPreferences.setPremiumizeApiKey(requireContext(), scanned) }; toast("Premiumize scanned") }
            "trakt" -> { save { AppPreferences.setTraktAccessToken(requireContext(), scanned) }; toast("Trakt scanned") }
        }
    }

    private fun save(block: suspend () -> Unit) { lifecycleScope.launch { block() } }
    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
