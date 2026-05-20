package com.iptv.app

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a root layout container dynamically
        val container = android.widget.FrameLayout(this).apply {
            id = android.view.View.generateViewId()
        }
        setContentView(container)

        // Inject a clean Leanback grid fragment shell
        if (savedInstanceState == null) {
            val gridFragment = MainMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(container.id, gridFragment)
                .commit()
        }
    }
}

class MainMenuFragment : VerticalGridSupportFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        title = "MyIPTV - NextGen TV"
        
        // Set up a clean 2-column grid layout configuration
        val gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 2
        }
        setGridPresenter(gridPresenter)

        // Add placeholder menu tiles to verify the TV rendering shell is fully working
        val adapter = ArrayObjectAdapter(androidx.leanback.widget.StringPresenter())
        adapter.add("📺 Live TV (Xtream/Stalker Codes)")
        adapter.add("🎬 Movies (TorBox Debrid Link)")
        adapter.add("🍿 TV Series (Cached Torrents)")
        adapter.add("⚙️ Settings (Top Navigation Bar)")
        
        this.adapter = adapter

        setOnItemViewClickedListener { _, item, _, _ ->
            Toast.makeText(activity, "Clicked: $item", Toast.LENGTH_SHORT).show()
        }
    }
}
