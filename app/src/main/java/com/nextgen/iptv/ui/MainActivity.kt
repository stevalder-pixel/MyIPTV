package com.nextgen.iptv.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nextgen.iptv.R
import com.nextgen.iptv.databinding.ActivityMainBinding
import com.nextgen.iptv.tv.TvChannelManager
import com.nextgen.iptv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var sidebarExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigation()
        binding.sidebarOverlay.setOnClickListener { collapseSidebar() }

        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.playerActivity -> binding.sidebarContainer.visibility = View.GONE
                else -> {
                    binding.sidebarContainer.visibility = View.VISIBLE
                    updateHighlight(dest.id)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lifecycleScope.launch { TvChannelManager.setupChannels(this@MainActivity) }
        }

        handleIntent(intent)
        updateHighlight(R.id.homeFragment)
        binding.navHome.post { focusHomeNav() }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data ?: return
        when (data.host) {
            "home" -> navigate(R.id.homeFragment)
            "livetv" -> navigate(R.id.liveTvFragment)
            "play" -> {
                val streamUrl = data.getQueryParameter("url") ?: return
                val title = data.getQueryParameter("title") ?: ""
                startActivity(Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                    putExtra(PlayerActivity.EXTRA_TITLE, title)
                    putExtra(PlayerActivity.EXTRA_IS_LIVE, false)
                })
            }
        }
    }

    private fun setupNavigation() {
        binding.navHome.setOnClickListener { navigate(R.id.homeFragment) }
        binding.navHomeIcon.setColorFilter(getColor(R.color.accent_blue))
        binding.navMovies.setOnClickListener { navigate(R.id.moviesFragment) }
        binding.navTvShows.setOnClickListener { navigate(R.id.tvShowsFragment) }
        binding.navLiveTv.setOnClickListener { navigate(R.id.liveTvFragment) }
        binding.navSettings.setOnClickListener { navigate(R.id.settingsFragment) }
    }

    private fun navigate(id: Int) {
        if (navController.currentDestination?.id != id) navController.navigate(id)
        collapseSidebar()
    }

    fun focusHomeNav() { binding.navHome.requestFocus() }

    fun expandSidebar() {
        if (sidebarExpanded) return
        sidebarExpanded = true
        binding.sidebarContainer.layoutParams.width = resources.getDimensionPixelSize(R.dimen.sidebar_expanded_width)
        binding.sidebarContainer.requestLayout()
        binding.sidebarTitle.visibility = View.VISIBLE
        listOf(binding.navHomeLabel, binding.navMoviesLabel, binding.navTvShowsLabel,
            binding.navLiveTvLabel, binding.navSettingsLabel).forEach { it.visibility = View.VISIBLE }
        binding.sidebarOverlay.visibility = View.VISIBLE
        binding.sidebarOverlay.animate().alpha(0.6f).setDuration(220).start()
        binding.navHome.requestFocus()
    }

    fun collapseSidebar() {
        if (!sidebarExpanded) return
        sidebarExpanded = false
        binding.sidebarContainer.layoutParams.width = resources.getDimensionPixelSize(R.dimen.sidebar_collapsed_width)
        binding.sidebarContainer.requestLayout()
        binding.sidebarTitle.visibility = View.GONE
        listOf(binding.navHomeLabel, binding.navMoviesLabel, binding.navTvShowsLabel,
            binding.navLiveTvLabel, binding.navSettingsLabel).forEach { it.visibility = View.GONE }
        binding.sidebarOverlay.animate().alpha(0f).setDuration(220).withEndAction {
            binding.sidebarOverlay.visibility = View.GONE
        }.start()
    }

    private fun updateHighlight(destId: Int) {
        val icons = mapOf(
            R.id.homeFragment to binding.navHomeIcon,
            R.id.moviesFragment to binding.navMoviesIcon,
            R.id.tvShowsFragment to binding.navTvShowsIcon,
            R.id.liveTvFragment to binding.navLiveTvIcon,
            R.id.settingsFragment to binding.navSettingsIcon
        )
        val labels = mapOf(
            R.id.homeFragment to binding.navHomeLabel,
            R.id.moviesFragment to binding.navMoviesLabel,
            R.id.tvShowsFragment to binding.navTvShowsLabel,
            R.id.liveTvFragment to binding.navLiveTvLabel,
            R.id.settingsFragment to binding.navSettingsLabel
        )
        icons.forEach { (id, icon) ->
            icon.setColorFilter(if (id == destId) getColor(R.color.accent_blue) else getColor(R.color.nav_icon_inactive))
        }
        labels.forEach { (id, label) ->
            label.setTextColor(if (id == destId) getColor(R.color.accent_blue) else getColor(R.color.text_secondary))
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN && !sidebarExpanded) {
            val frag = supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.firstOrNull()
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> when (frag) {
                    is com.nextgen.iptv.ui.home.HomeFragment -> { frag.nextRow(); return true }
                    is com.nextgen.iptv.ui.movies.MoviesFragment -> { frag.nextRow(); return true }
                    is com.nextgen.iptv.ui.tv.TvShowsFragment -> { frag.nextRow(); return true }
                }
                android.view.KeyEvent.KEYCODE_DPAD_UP -> when (frag) {
                    is com.nextgen.iptv.ui.home.HomeFragment -> { frag.prevRow(); return true }
                    is com.nextgen.iptv.ui.movies.MoviesFragment -> { frag.prevRow(); return true }
                    is com.nextgen.iptv.ui.tv.TvShowsFragment -> { frag.prevRow(); return true }
                }
            }
        }
        if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
            if (sidebarExpanded) { collapseSidebar(); return true }
            if (navController.currentDestination?.id != R.id.homeFragment) { navigate(R.id.homeFragment); return true }
            val frag = supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.firstOrNull()
            if (frag is com.nextgen.iptv.ui.home.HomeFragment) { frag.scrollToFirst(); return true }
            focusHomeNav(); return true
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            sidebarExpanded -> collapseSidebar()
            navController.currentDestination?.id != R.id.homeFragment -> navigate(R.id.homeFragment)
            else -> super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (sidebarExpanded.not()) { expandSidebar(); return true }
                else return super.onKeyDown(keyCode, event)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sidebarExpanded) { collapseSidebar(); true }
                else super.onKeyDown(keyCode, event)
            }
            KeyEvent.KEYCODE_MENU -> {
                if (sidebarExpanded) collapseSidebar() else expandSidebar(); true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}