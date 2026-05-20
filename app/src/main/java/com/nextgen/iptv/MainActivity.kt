package com.nextgen.iptv

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var sidebar: LinearLayout
    private lateinit var contentArea: LinearLayout
    private val menuItems = listOf("Live TV", "Movies", "TV Series", "Settings")
    private val sidebarViews = mutableListOf<TextView>()
    
    // Track row wrappers to easily toggle their visibility
    private val sectionContainers = mutableListOf<LinearLayout>()
    private val movieRows = mutableListOf<LinearLayout>()
    
    private var isDisplayingDetails = false
    private var lastActiveMenuIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#050811"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(20, 80, 20, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#0A0F1D"))
            layoutParams = LinearLayout.LayoutParams(340, LinearLayout.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = false
        }

        menuItems.forEachIndexed { index, title ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 19f
                setTextColor(android.graphics.Color.parseColor("#5A6785"))
                setPadding(50, 30, 40, 30)
                isFocusable = true
                isFocusableInTouchMode = true
                gravity = Gravity.CENTER_VERTICAL

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        setTextColor(android.graphics.Color.WHITE)
                        view.scaleX = 1.06f
                        view.scaleY = 1.06f
                        lastActiveMenuIndex = index
                        updateContentArea(index)
                    } else {
                        setTextColor(android.graphics.Color.parseColor("#5A6785"))
                        view.scaleX = 1.0f
                        view.scaleY = 1.0f
                    }
                }

                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        hideSidebarLayout()
                        true
                    } else {
                        false
                    }
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
        sidebar.layoutParams.width = 0
        sidebar.requestLayout()
        
        if (movieRows.isNotEmpty() && movieRows[0].childCount > 0) {
            movieRows[0].getChildAt(0).requestFocus()
        }
    }

    private fun showSidebarLayout() {
        sidebar.layoutParams.width = 340
        sidebar.requestLayout()
        if (lastActiveMenuIndex in sidebarViews.indices) {
            sidebarViews[lastActiveMenuIndex].requestFocus()
        }
    }

    private fun updateContentArea(menuIndex: Int) {
        if (isDisplayingDetails) return
        contentArea.removeAllViews()
        movieRows.clear()
        sectionContainers.clear()

        val titleView = TextView(this).apply {
            text = when(menuIndex) {
                0 -> "Live Streams"
                1 -> "Cinematic Movies"
                2 -> "TV Series"
                3 -> "Configuration"
                else -> ""
            }
            textSize = 32f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(10, 0, 0, 40)
        }
        contentArea.addView(titleView)

        if (menuIndex == 1 || menuIndex == 2) {
            val sections = if (menuIndex == 1) listOf("Trending Content", "TorBox Debrid Direct") else listOf("Popular Series", "Recent Tracker Drops")
            val scrollContainer = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
            val verticalLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

            sections.forEachIndexed { rowIndex, sectionName ->
                // Outer layout container for the entire category row block
                val rowWrapper = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val rowLabel = TextView(this).apply {
                    text = sectionName
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#445373"))
                    setPadding(15, 25, 0, 15)
                }
                rowWrapper.addView(rowLabel)

                val horizontalScroll = HorizontalScrollView(this).apply {
                    isHorizontalScrollBarEnabled = false // FIX: Turn off horizontal scrollbar line
                }
                val rowItemsContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

                for (i in 1..8) {
                    val card = TextView(this).apply {
                        text = "Media Item $i"
                        textSize = 14f
                        setTextColor(android.graphics.Color.parseColor("#8E9CB3"))
                        gravity = Gravity.CENTER
                        isFocusable = true
                        isFocusableInTouchMode = true
                        
                        val cardNormal = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#0C1222"))
                            cornerRadius = 16f
                        }
                        val cardFocused = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#131D35"))
                            cornerRadius = 16f
                            setStroke(3, android.graphics.Color.WHITE)
                        }

                        background = cardNormal
                        layoutParams = LinearLayout.LayoutParams(240, 350).apply {
                            setMargins(12, 0, 12, 0)
                        }

                        setOnFocusChangeListener { view, hasFocus ->
                            view.background = if (hasFocus) cardFocused else cardNormal
                            if (hasFocus) {
                                view.scaleX = 1.04f
                                // DYNAMIC ISOLATION RULE: Show only this container row, hide the rest!
                                isolateFocusedRow(rowIndex)
                            } else {
                                view.scaleX = 1.0f
                            }
                        }

                        // Intercept LEFT button clicks to bring back sidebar
                        setOnKeyListener { _, keyCode, event ->
                            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                if (i == 1) { // We are on the very first card tile of this row
                                    showSidebarLayout()
                                    return@setOnKeyListener true
                                }
                            }
                            false
                        }

                        setOnClickListener {
                            showMediaDetails(text.toString())
                        }
                    }
                    rowItemsContainer.addView(card)
                }
                
                movieRows.add(rowItemsContainer)
                horizontalScroll.addView(rowItemsContainer)
                rowWrapper.addView(horizontalScroll)
                
                sectionContainers.add(rowWrapper)
                verticalLayout.addView(rowWrapper)
            }
            scrollContainer.addView(verticalLayout)
            contentArea.addView(scrollContainer)
        }
    }

    // Dynamic row visibility manager engine
    private fun isolateFocusedRow(focusedIndex: Int) {
        sectionContainers.forEachIndexed { idx, container ->
            if (idx == focusedIndex) {
                container.visibility = View.VISIBLE
                container.alpha = 1.0f
            } else {
                container.visibility = View.GONE // Hide completely from rendering pipeline
            }
        }
    }

    private fun showMediaDetails(mediaName: String) {
        isDisplayingDetails = true
        contentArea.removeAllViews()

        val detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 20, 20)
        }

        val titleView = TextView(this).apply {
            text = mediaName
            textSize = 38f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 15)
        }
        detailLayout.addView(titleView)

        val descriptionView = TextView(this).apply {
            text = "Premium Source Stream • Stremio Add-ons Network Verified\nReady to fetch cached high-speed torrent links via TorBox Debrid core channels."
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#7A89A8"))
            setPadding(0, 0, 0, 45)
        }
        detailLayout.addView(descriptionView)

        val trailerButton = Button(this).apply {
            text = "▶  Watch Trailer"
            textSize = 15f
            setTextColor(android.graphics.Color.WHITE)
            isFocusable = true
            setPadding(40, 20, 40, 20)
            
            val btnNormal = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#16223F"))
                cornerRadius = 12f
            }
            val btnFocused = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 12f
            }
            background = btnNormal

            setOnFocusChangeListener { v, hasFocus ->
                v.background = if (hasFocus) btnFocused else btnNormal
                setTextColor(if (hasFocus) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }

            setOnClickListener {
                Toast.makeText(this@MainActivity, "Streaming Trailer Source Panel...", Toast.LENGTH_SHORT).show()
            }
        }

        detailLayout.addView(trailerButton)
        contentArea.addView(detailLayout)
        trailerButton.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isDisplayingDetails) {
                isDisplayingDetails = false
                updateContentArea(lastActiveMenuIndex)
                return true
            }

            // Always open sidebar menu back up on BACK button press
            if (sidebar.layoutParams.width == 0) {
                showSidebarLayout()
                return true
            }
            
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
