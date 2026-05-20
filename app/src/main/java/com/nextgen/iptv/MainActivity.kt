package com.nextgen.iptv

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var sidebar: LinearLayout
    private lateinit var contentArea: LinearLayout
    
    private val sidebarViews = mutableListOf<View>()
    private val sectionContainers = mutableListOf<LinearLayout>()
    private val movieRows = mutableListOf<LinearLayout>()
    
    private var isDisplayingDetails = false
    private var lastActiveMenuIndex = 1
    private var currentFocusedRowIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#040710"))
            clipChildren = false // Allow expanding elements to draw over layouts
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Clean semi-transparent sidebar menu
        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
            setPadding(10, 0, 10, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#CC080D1A")) // 80% opacity overlay
            layoutParams = LinearLayout.LayoutParams(140, LinearLayout.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = false
            clipChildren = false
        }

        // Linking our newly created crisp layout resource drawables
        val vectorResIds = listOf(
            R.drawable.ic_tv_modern,
            R.drawable.ic_movie_modern,
            R.drawable.ic_series_modern,
            R.drawable.ic_settings_modern
        )

        vectorResIds.forEachIndexed { index, resId ->
            val menuIconContainer = ImageView(this).apply {
                setImageResource(resId)
                setPadding(0, 40, 0, 40)
                isFocusable = true
                isFocusableInTouchMode = true
                
                // Slate gray base unselected state
                setColorFilter(android.graphics.Color.parseColor("#495573"))
                layoutParams = LinearLayout.LayoutParams(90, 130).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        (view as ImageView).setColorFilter(android.graphics.Color.WHITE)
                        view.scaleX = 1.25f
                        view.scaleY = 1.25f
                        lastActiveMenuIndex = index
                        updateContentArea(index)
                    } else {
                        (view as ImageView).setColorFilter(android.graphics.Color.parseColor("#495573"))
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
            sidebar.addView(menuIconContainer)
            sidebarViews.add(menuIconContainer)
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM 
            setPadding(60, 20, 60, 20)
            clipChildren = false // CRITICAL: Stop parent container frame pruning card rings
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
        sidebar.visibility = View.GONE
        if (currentFocusedRowIndex in movieRows.indices && movieRows[currentFocusedRowIndex].childCount > 0) {
            movieRows[currentFocusedRowIndex].getChildAt(0).requestFocus()
        } else if (movieRows.isNotEmpty() && movieRows[0].childCount > 0) {
            movieRows[0].getChildAt(0).requestFocus()
        }
    }

    private fun showSidebarLayout() {
        sidebar.visibility = View.VISIBLE
        if (lastActiveMenuIndex in sidebarViews.indices) {
            sidebarViews[lastActiveMenuIndex].requestFocus()
        }
    }

    private fun updateContentArea(menuIndex: Int) {
        if (isDisplayingDetails) return
        contentArea.removeAllViews()
        movieRows.clear()
        sectionContainers.clear()
        currentFocusedRowIndex = 0

        val topSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        contentArea.addView(topSpacer)

        val titleView = TextView(this).apply {
            text = when(menuIndex) {
                0 -> "Live TV Portals"
                1 -> "Cinematic Movies"
                2 -> "TV Series"
                3 -> "Configuration"
                else -> ""
            }
            textSize = 32f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(android.graphics.Color.WHITE)
            setPadding(20, 0, 0, 15)
        }
        contentArea.addView(titleView)

        if (menuIndex == 1 || menuIndex == 2) {
            val sections = if (menuIndex == 1) listOf("Trending Content", "TorBox Debrid Direct") else listOf("Popular Series", "Recent Tracker Drops")
            
            val scrollContainer = ScrollView(this).apply {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                clipChildren = false
                clipToPadding = false
            }
            val verticalLayout = LinearLayout(this).apply { 
                orientation = LinearLayout.VERTICAL 
                clipChildren = false
                clipToPadding = false
            }

            sections.forEachIndexed { rowIndex, sectionName ->
                val rowWrapper = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    clipChildren = false // CRITICAL: Stop row wrapper pruning card rings
                    clipToPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 10, 0, 30)
                    }
                }

                val rowLabel = TextView(this).apply {
                    text = sectionName
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#4E5B7C"))
                    setPadding(20, 5, 0, 5)
                }
                rowWrapper.addView(rowLabel)

                // High clearance horizontal engine
                val horizontalScroll = HorizontalScrollView(this).apply {
                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                    setPadding(10, 40, 10, 50) // Massive top/bottom clearance padding cushions
                    clipToPadding = false      // Allow scaling items to break out of borders cleanly
                    clipChildren = false
                }
                val rowItemsContainer = LinearLayout(this).apply { 
                    orientation = LinearLayout.HORIZONTAL 
                    clipChildren = false
                    clipToPadding = false
                }

                for (i in 1..8) {
                    val card = TextView(this).apply {
                        text = "Media Card \$i"
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
                            setColor(android.graphics.Color.parseColor("#16213E"))
                            cornerRadius = 16f
                            setStroke(4, android.graphics.Color.WHITE)
                        }

                        background = cardNormal
                        layoutParams = LinearLayout.LayoutParams(250, 350).apply {
                            setMargins(15, 10, 15, 10) // Extra margin workspace
                        }

                        setOnFocusChangeListener { view, hasFocus ->
                            view.background = if (hasFocus) cardFocused else cardNormal
                            if (hasFocus) {
                                view.scaleX = 1.06f
                                view.scaleY = 1.06f
                                currentFocusedRowIndex = rowIndex
                                isolateFocusedRow(rowIndex)
                            } else {
                                view.scaleX = 1.0f
                                view.scaleY = 1.0f
                            }
                        }

                        setOnKeyListener { _, keyCode, event ->
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                when (keyCode) {
                                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                                        if (i == 1) {
                                            showSidebarLayout()
                                            return@setOnKeyListener true
                                        }
                                    }
                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        if (rowIndex < sections.size - 1) {
                                            isolateFocusedRow(rowIndex + 1)
                                            movieRows[rowIndex + 1].getChildAt(0).requestFocus()
                                            return@setOnKeyListener true
                                        }
                                    }
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        if (rowIndex > 0) {
                                            isolateFocusedRow(rowIndex - 1)
                                            movieRows[rowIndex - 1].getChildAt(0).requestFocus()
                                            return@setOnKeyListener true
                                        }
                                    }
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
            
            isolateFocusedRow(0)
        }
    }

    private fun isolateFocusedRow(focusedIndex: Int) {
        sectionContainers.forEachIndexed { idx, container ->
            if (idx == focusedIndex) {
                container.visibility = View.VISIBLE
                container.alpha = 1.0f
            } else {
                container.visibility = View.GONE
            }
        }
    }

    private fun showMediaDetails(mediaName: String) {
        isDisplayingDetails = true
        contentArea.removeAllViews()

        val detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.MATCH_PARENT)
            setPadding(30, 20, 20, 20)
        }

        val titleView = TextView(this).apply {
            text = mediaName
            textSize = 40f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 15)
        }
        detailLayout.addView(titleView)

        val descriptionView = TextView(this).apply {
            text = "TMDB Metadata Backbone Loaded • Verified Stremio Feed Link\n\nPress the button below to trigger high-speed trailer streaming playback nodes directly on device."
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
            setPadding(45, 22, 45, 22)
            
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
                Toast.makeText(this@MainActivity, "Launching Video Player Container...", Toast.LENGTH_SHORT).show()
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

            if (sidebar.visibility == View.GONE) {
                if (currentFocusedRowIndex in movieRows.indices) {
                    val currentRowContainer = movieRows[currentFocusedRowIndex]
                    if (currentRowContainer.childCount > 0) {
                        val firstTile = currentRowContainer.getChildAt(0)
                        if (!firstTile.isFocused) {
                            firstTile.requestFocus()
                            return true
                        }
                    }
                }
                showSidebarLayout()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
