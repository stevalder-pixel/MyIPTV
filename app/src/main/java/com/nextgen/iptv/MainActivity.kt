package com.nextgen.iptv

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var sidebar: LinearLayout
    private lateinit var contentArea: LinearLayout
    private val menuItems = listOf("📺 Live TV", "🎬 Movies", "🍿 TV Series", "⚙️ Settings")
    private val sidebarViews = mutableListOf<TextView>()
    
    // Track row containers for the back button focus rule
    private val movieRows = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#060B19")) // Cinematic Deep Midnight Navy
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Sleek, modern sidebar panel
        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(25, 60, 25, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#0B132B")) // Translucent dark slate
            layoutParams = LinearLayout.LayoutParams(
                340,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        menuItems.forEachIndexed { index, title ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#A0AABF"))
                setPadding(30, 25, 30, 25)
                isFocusable = true
                isFocusableInTouchMode = true
                gravity = Gravity.CENTER_VERTICAL
                
                // Rounded corner pill backgrounds for menus
                val normalDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.TRANSPARENT)
                    cornerRadius = 15f
                }
                val focusDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#87CEEB")) // Premium Sky Blue Focus
                    cornerRadius = 15f
                }

                background = normalDrawable

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        view.background = focusDrawable
                        setTextColor(android.graphics.Color.parseColor("#060B19")) // Dark text on light background
                        updateContentArea(index)
                    } else {
                        view.background = normalDrawable
                        setTextColor(android.graphics.Color.parseColor("#A0AABF"))
                    }
                }
            }
            sidebar.addView(menuItem)
            sidebarViews.add(menuItem)
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
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

    private fun updateContentArea(menuIndex: Int) {
        contentArea.removeAllViews()
        movieRows.clear()

        val titleView = TextView(this).apply {
            text = when(menuIndex) {
                0 -> "Live Streams & TV Portals"
                1 -> "Premium Movies Catalog"
                2 -> "Premium TV Series Feed"
                3 -> "System Settings"
                else -> ""
            }
            textSize = 30f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 30)
        }
        contentArea.addView(titleView)

        // Generate Cinematic Horizontal Rows for Movies & TV
        if (menuIndex == 1 || menuIndex == 2) {
            val sections = if (menuIndex == 1) listOf("Trending Content", "Stremio Hot Add-ons", "TorBox Cached Links") else listOf("Popular Series", "Recently Updated")
            
            val scrollContainer = ScrollView(this)
            val verticalLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

            sections.forEach { sectionName ->
                val rowLabel = TextView(this).apply {
                    text = sectionName
                    textSize = 18f
                    setTextColor(android.graphics.Color.parseColor("#87CEEB")) // Sky Blue section labels
                    setPadding(10, 20, 0, 15)
                }
                verticalLayout.addView(rowLabel)

                val horizontalScroll = HorizontalScrollView(this)
                val rowItemsContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

                // Generate individual stylized media cards
                for (i in 1..8) {
                    val card = TextView(this).apply {
                        text = "Media Item $i\n[TorBox 4K]"
                        textSize = 14f
                        setTextColor(android.graphics.Color.WHITE)
                        gravity = Gravity.CENTER
                        setPadding(20, 20, 20, 20)
                        isFocusable = true
                        isFocusableInTouchMode = true
                        
                        val cardNormal = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#1C2541"))
                            cornerRadius = 20f
                        }
                        val cardFocused = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#112244"))
                            cornerRadius = 20f
                            setStroke(4, android.graphics.Color.parseColor("#87CEEB")) // Glowing Sky Blue stroke border
                        }

                        background = cardNormal
                        layoutParams = LinearLayout.LayoutParams(240, 160).apply {
                            setMargins(10, 0, 10, 0)
                        }

                        setOnFocusChangeListener { view, hasFocus ->
                            view.background = if (hasFocus) cardFocused else cardNormal
                        }
                    }
                    rowItemsContainer.addView(card)
                }
                
                movieRows.add(rowItemsContainer)
                horizontalScroll.addView(rowItemsContainer)
                verticalLayout.addView(horizontalScroll)
            }
            scrollContainer.addView(verticalLayout)
            contentArea.addView(scrollContainer)
        } else {
            // Placeholder text layout for Live TV / Settings
            val descriptionView = TextView(this).apply {
                text = "Interface framework ready. Preparing authentication components..."
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#A0AABF"))
            }
            contentArea.addView(descriptionView)
        }
    }

    // CRITICAL REQUIREMENT: Intercept back button click to reset focus to first row tile
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            movieRows.forEach { rowContainer ->
                if (rowContainer.hasFocus()) {
                    val firstChild = rowContainer.getChildAt(0)
                    if (firstChild != null && !firstChild.isFocused) {
                        firstChild.requestFocus() // Snap focus back to the first tile instantly
                        return true // Prevent app from exiting or going backward
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
