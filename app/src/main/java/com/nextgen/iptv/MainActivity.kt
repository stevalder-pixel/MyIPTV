package com.nextgen.iptv

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var sidebar: LinearLayout
    private lateinit var contentArea: LinearLayout
    private val menuItems = listOf("📺 Live TV", "🎬 Movies", "🍿 TV Series", "⚙️ Settings")
    private val sidebarViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#0A1128"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(30, 80, 30, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#101F42"))
            layoutParams = LinearLayout.LayoutParams(
                320,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        menuItems.forEachIndexed { index, title ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 20f
                setTextColor(android.graphics.Color.LTGRAY)
                setPadding(20, 30, 20, 30)
                isFocusable = true
                isFocusableInTouchMode = true
                gravity = Gravity.CENTER_VERTICAL

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        view.setBackgroundColor(android.graphics.Color.parseColor("#87CEEB"))
                        (view as TextView).setTextColor(android.graphics.Color.BLACK)
                        updateContentArea(index)
                    } else {
                        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        (view as TextView).setTextColor(android.graphics.Color.LTGRAY)
                    }
                }
            }
            sidebar.addView(menuItem)
            sidebarViews.add(menuItem)
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        mainLayout.addView(sidebar)
        mainLayout.addView(contentArea)
        setContentView(mainLayout)

        sidebarViews.firstOrNull()?.requestFocus()
    }

    // FIX: Changed variable type from String to Int here
    private fun updateContentArea(menuIndex: Int) {
        contentArea.removeAllViews()
        
        val sectionTitle = TextView(this).apply {
            text = when(menuIndex) {
                0 -> "Xtream Codes & Stalker Portal Port"
                1 -> "Movies (Stremio Add-ons + TorBox Debrid)"
                2 -> "TV Series (Stremio Add-ons + TorBox Debrid)"
                3 -> "Settings (Trakt Login | Debrid Sync)"
                else -> ""
            }
            textSize = 28f
            setTextColor(android.graphics.Color.WHITE)
        }
        contentArea.addView(sectionTitle)
        
        val sectionDesc = TextView(this).apply {
            text = "Focus navigation interface active. Press UP/DOWN on remote to test sidebar."
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#87CEEB"))
            setPadding(0, 20, 0, 0)
        }
        contentArea.addView(sectionDesc)
    }
}
