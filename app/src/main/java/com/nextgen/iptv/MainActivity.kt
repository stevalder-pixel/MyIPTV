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
    private val menuItems = listOf("Live TV", "Movies", "TV Series", "Settings")
    private val sidebarViews = mutableListOf<TextView>()
    private val movieRows = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#050811")) // Deeper Pitch Black-Navy
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Clean Sidebar panel structure
        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(20, 80, 20, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#0A0F1D")) // Sleek Dark Panel
            layoutParams = LinearLayout.LayoutParams(
                340,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        menuItems.forEachIndexed { index, title ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 19f
                setTextColor(android.graphics.Color.parseColor("#6C7A9C")) // Muted minimalist text
                setPadding(40, 30, 40, 30)
                isFocusable = true
                isFocusableInTouchMode = true
                gravity = Gravity.CENTER_VERTICAL

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        // Minimalist text accent focus (No bulky bright backgrounds)
                        setTextColor(android.graphics.Color.WHITE)
                        view.scaleX = 1.05f // Subtle cinematic lift animation
                        view.scaleY = 1.05f
                        updateContentArea(index)
                    } else {
                        setTextColor(android.graphics.Color.parseColor("#6C7A9C"))
                        view.scaleX = 1.0f
                        view.scaleY = 1.0f
                    }
                }

                // Smoothly hide sidebar if user presses RIGHT key to enter content items
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        hideSidebarLayout()
                    }
                    false
                }
            }
            sidebar.addView(menuItem)
            sidebarViews.add(menuItem)
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 50, 60, 50)
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

    private fun hideSidebarLayout() {
        sidebar.layoutParams.width = 0 // Completely collapses sidebar space
        sidebar.requestLayout()
    }

    private fun showSidebarLayout() {
        sidebar.layoutParams.width = 340 // Returns sidebar to standard size
        sidebar.requestLayout()
        sidebarViews.firstOrNull()?.requestFocus()
    }

    private fun updateContentArea(menuIndex: Int) {
        contentArea.removeAllViews()
        movieRows.clear()

        val titleView = TextView(this).apply {
            text = when(menuIndex) {
                0 -> "Live Channels"
                1 -> "Cinematic Movies"
                2 -> "TV Series"
                3 -> "Configuration"
                else -> ""
            }
            textSize = 34f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 40)
        }
        contentArea.addView(titleView)

        if (menuIndex == 1 || menuIndex == 2) {
            val sections = if (menuIndex == 1) listOf("Trending Now", "TorBox Links") else listOf("Popular", "Recent Releases")
            val scrollContainer = ScrollView(this)
            val verticalLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

            sections.forEach { sectionName ->
                val rowLabel = TextView(this).apply {
                    text = sectionName
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#495A80"))
                    setPadding(10, 25, 0, 15)
                }
                verticalLayout.addView(rowLabel)

                val horizontalScroll = HorizontalScrollView(this)
                val rowItemsContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

                for (i in 1..8) {
                    val card = TextView(this).apply {
                        text = "Poster $i"
                        textSize = 14f
                        setTextColor(android.graphics.Color.parseColor("#9EADCC"))
                        gravity = Gravity.CENTER
                        isFocusable = true
                        isFocusableInTouchMode = true
                        
                        val cardNormal = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#0F1626"))
                            cornerRadius = 16f
                        }
                        val cardFocused = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#152035"))
                            cornerRadius = 16f
                            setStroke(3, android.graphics.Color.WHITE) // Clean, high-end white focal border
                        }

                        background = cardNormal
                        layoutParams = LinearLayout.LayoutParams(260, 380).apply { // Modern vertical poster cards aspect ratio
                            setMargins(12, 0, 12, 0)
                        }

                        setOnFocusChangeListener { view, hasFocus ->
                            view.background = if (hasFocus) cardFocused else cardNormal
                            if (hasFocus) view.scaleX = 1.03f else view.scaleX = 1.0f
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
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Rule 1: If sidebar is completely hidden, bring it back instantly
            if (sidebar.layoutParams.width == 0) {
                showSidebarLayout()
                return true
            }
            
            // Rule 2: Snap back to first card block position
            movieRows.forEach { rowContainer ->
                if (rowContainer.hasFocus()) {
                    val firstChild = rowContainer.getChildAt(0)
                    if (firstChild != null && !firstChild.isFocused) {
                        firstChild.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
