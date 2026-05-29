package com.nextgen.iptv.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nextgen.iptv.R
import com.nextgen.iptv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var sidebarExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.navHome.setOnClickListener { navigate(R.id.homeFragment) }
        binding.navMovies.setOnClickListener { navigate(R.id.moviesFragment) }
        binding.navTvShows.setOnClickListener { navigate(R.id.tvShowsFragment) }
        binding.navLiveTv.setOnClickListener { navigate(R.id.liveTvFragment) }
        binding.navSettings.setOnClickListener { navigate(R.id.settingsFragment) }
        binding.sidebarOverlay.setOnClickListener { collapseSidebar() }
        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.playerActivity -> binding.sidebarContainer.visibility = View.GONE
                else -> { binding.sidebarContainer.visibility = View.VISIBLE; updateHighlight(dest.id) }
            }
        }
    }

    private fun navigate(id: Int) {
        if (navController.currentDestination?.id != id) navController.navigate(id)
        collapseSidebar()
    }

    fun expandSidebar() {
        if (sidebarExpanded) return
        sidebarExpanded = true
        binding.sidebarContainer.layoutParams.width = resources.getDimensionPixelSize(R.dimen.sidebar_expanded_width)
        binding.sidebarContainer.requestLayout()
        binding.sidebarTitle.visibility = View.VISIBLE
        listOf(binding.navHomeLabel, binding.navMoviesLabel, binding.navTvShowsLabel, binding.navLiveTvLabel, binding.navSettingsLabel).forEach { it.visibility = View.VISIBLE }
        binding.sidebarOverlay.visibility = View.VISIBLE
        binding.sidebarOverlay.animate().alpha(0.6f).setDuration(220).start()
    }

    fun collapseSidebar() {
        if (!sidebarExpanded) return
        sidebarExpanded = false
        binding.sidebarContainer.layoutParams.width = resources.getDimensionPixelSize(R.dimen.sidebar_collapsed_width)
        binding.sidebarContainer.requestLayout()
        binding.sidebarTitle.visibility = View.GONE
        listOf(binding.navHomeLabel, binding.navMoviesLabel, binding.navTvShowsLabel, binding.navLiveTvLabel, binding.navSettingsLabel).forEach { it.visibility = View.GONE }
        binding.sidebarOverlay.animate().alpha(0f).setDuration(220).withEndAction { binding.sidebarOverlay.visibility = View.GONE }.start()
    }

    private fun updateHighlight(destId: Int) {
        val map = mapOf(
            R.id.homeFragment to Pair(binding.navHomeIcon, binding.navHomeLabel),
            R.id.moviesFragment to Pair(binding.navMoviesIcon, binding.navMoviesLabel),
            R.id.tvShowsFragment to Pair(binding.navTvShowsIcon, binding.navTvShowsLabel),
            R.id.liveTvFragment to Pair(binding.navLiveTvIcon, binding.navLiveTvLabel),
            R.id.settingsFragment to Pair(binding.navSettingsIcon, binding.navSettingsLabel)
        )
        map.forEach { (id, views) ->
            val active = id == destId
            views.first.setColorFilter(if (active) getColor(R.color.accent_blue) else getColor(R.color.nav_icon_inactive))
            views.second.setTextColor(if (active) getColor(R.color.accent_blue) else getColor(R.color.text_secondary))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> { if (!sidebarExpanded) { expandSidebar(); true } else super.onKeyDown(keyCode, event) }
        KeyEvent.KEYCODE_DPAD_RIGHT -> { if (sidebarExpanded) { collapseSidebar(); true } else super.onKeyDown(keyCode, event) }
        KeyEvent.KEYCODE_MENU -> { if (sidebarExpanded) collapseSidebar() else expandSidebar(); true }
        KeyEvent.KEYCODE_BACK -> { if (sidebarExpanded) { collapseSidebar(); true } else super.onKeyDown(keyCode, event) }
        else -> super.onKeyDown(keyCode, event)
    }
}
