package com.nextgen.iptv.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nextgen.iptv.R
import com.nextgen.iptv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var sidebarExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        binding.sideNav.setupWithNavController(navController)
        binding.sidebarToggle.setOnClickListener { toggleSidebar() }
        binding.sidebarOverlay.setOnClickListener { collapseSidebar() }
        binding.topBarToggle.setOnClickListener {
            binding.topBar.visibility = if (binding.topBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playerFragment -> { binding.sidebarContainer.visibility = View.GONE; binding.topBar.visibility = View.GONE; binding.bottomNav.visibility = View.GONE }
                else -> { binding.sidebarContainer.visibility = View.VISIBLE; binding.bottomNav.visibility = View.VISIBLE }
            }
        }
    }

    private fun toggleSidebar() { if (sidebarExpanded) collapseSidebar() else expandSidebar() }

    private fun expandSidebar() {
        sidebarExpanded = true
        binding.sidebarOverlay.visibility = View.VISIBLE
        binding.sidebarOverlay.animate().alpha(1f).setDuration(250).start()
    }

    private fun collapseSidebar() {
        sidebarExpanded = false
        binding.sidebarOverlay.animate().alpha(0f).setDuration(250).withEndAction {
            binding.sidebarOverlay.visibility = View.GONE
        }.start()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) { toggleSidebar(); return true }
        return super.onKeyDown(keyCode, event)
    }
}