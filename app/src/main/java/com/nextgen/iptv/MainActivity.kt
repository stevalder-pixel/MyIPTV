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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
            setPadding(10, 0, 10, 0)
            setBackgroundColor(android.graphics.Color.parseColor("#CC080D1A"))
            layoutParams = LinearLayout.LayoutParams(140, LinearLayout.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = false
        }

        val iconDrawables = listOf(
            createTvVectorIcon(),
            createMovieVectorIcon(),
            createSeriesVectorIcon(),
            createSettingsVectorIcon()
        )

        iconDrawables.forEachIndexed { index, drawable ->
            val menuIconContainer = ImageView(this).apply {
                setImageDrawable(drawable)
                setPadding(0, 45, 0, 45)
                isFocusable = true
                isFocusableInTouchMode = true
                layoutParams = LinearLayout.LayoutParams(100, 150).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        setColorFilter(android.graphics.Color.WHITE)
                        view.scaleX = 1.15f
                        view.scaleY = 1.15f
                        lastActiveMenuIndex = index
                        updateContentArea(index)
                    } else {
                        setColorFilter(android.graphics.Color.parseColor("#4E5B7C"))
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
            setPadding(60, 40, 60, 40)
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

    private fun createTvVectorIcon(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.ShapeDrawable(object : android.graphics.drawable.shapes.Shape() {
            override fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint) {
                paint.color = android.graphics.Color.WHITE
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 5f
                canvas.drawRoundRect(15f, 25f, 85f, 75f, 8f, 8f, paint)
                canvas.drawLine(35f, 75f, 25f, 90f, paint)
                canvas.drawLine(65f, 75f, 75f, 90f, paint)
            }
        })
    }

    private fun createMovieVectorIcon(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.ShapeDrawable(object : android.graphics.drawable.shapes.Shape() {
            override fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint) {
                paint.color = android.graphics.Color.WHITE
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 5f
                canvas.drawRoundRect(15f, 20f, 85f, 80f, 6f, 6f, paint)
                canvas.drawLine(15f, 42f, 85f, 42f, paint)
                canvas.drawLine(35f, 20f, 45f, 42f, paint)
                canvas.drawLine(60f, 20f, 70f, 42f, paint)
            }
        })
    }

    private fun createSeriesVectorIcon(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.ShapeDrawable(object : android.graphics.drawable.shapes.Shape() {
            override fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint) {
                paint.color = android.graphics.Color.WHITE
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 5f
                canvas.drawRoundRect(10f, 35f, 70f, 85f, 10f, 10f, paint)
                canvas.drawRoundRect(30f, 15f, 90f, 65f, 10f, 10f, paint)
            }
        })
    }

    private fun createSettingsVectorIcon(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.ShapeDrawable(object : android.graphics.drawable.shapes.Shape() {
            override fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint) {
                paint.color = android.graphics.Color.WHITE
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 5f
                canvas.drawCircle(50f, 50f, 22f, paint)
                canvas.drawCircle(50f, 50f, 7f, paint)
                for (i in 0..360 step 45) {
                    val angle = Math.toRadians(i.toDouble())
                    val x1 = (50 + Math.cos(angle) * 22).toFloat()
                    val y1 = (50 + Math.sin(angle) * 22).toFloat()
                    val x2 = (50 + Math.cos(angle) * 32).toFloat()
                    val y2 = (50 + Math.sin(angle) * 32).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
            }
        })
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
                0 -> "Live Streams"
                1 -> "Cinematic Movies"
                2 -> "TV Series"
                3 -> "Configuration"
                else -> ""
            }
            textSize = 34f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(20, 0, 0, 10)
        }
        contentArea.addView(titleView)

        if (menuIndex == 1 || menuIndex == 2) {
            val sections = if (menuIndex == 1) listOf("Trending Content", "TorBox Debrid Direct") else listOf("Popular Series", "Recent Tracker Drops")
            
            val scrollContainer = ScrollView(this).apply {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
            val verticalLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

            sections.forEachIndexed { rowIndex, sectionName ->
                val rowWrapper = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val rowLabel = TextView(this).apply {
                    text = sectionName
                    textSize = 17f
                    setTextColor(android.graphics.Color.parseColor("#445373"))
                    setPadding(20, 10, 0, 10)
                }
                rowWrapper.addView(rowLabel)

                val horizontalScroll = HorizontalScrollView(this).apply {
                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                    setPadding(0, 25, 0, 25) 
                    clipToPadding = false    
                }
                val rowItemsContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

                for (i in 1..8) {
                    val card = TextView(this).apply {
                        text = "Media Card \$i"
                        textSize = 14f
                        setTextColor(android.graphics.Color.parseColor("#8E9CB3"))
                        gravity = Gravity.CENTER
                        isFocusable = true
                        isFocusableInTouchMode = true
                        
                        val cardNormal = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#0B1121"))
                            cornerRadius = 20f
                        }
                        val cardFocused = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#141E38"))
                            cornerRadius = 20f
                            setStroke(4, android.graphics.Color.WHITE)
                        }

                        background = cardNormal
                        layoutParams = LinearLayout.LayoutParams(260, 370).apply {
                            setMargins(15, 0, 15, 0)
                        }

                        setOnFocusChangeListener { view, hasFocus ->
                            view.background = if (hasFocus) cardFocused else cardNormal
                            if (hasFocus) {
                                view.scaleX = 1.05f
                                view.scaleY = 1.05f
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

            // BACK BUTTON ESCAPE ROUTE MANAGEMENT ENGINE
            if (sidebar.visibility == View.GONE) {
                if (currentFocusedRowIndex in movieRows.indices) {
                    val currentRowContainer = movieRows[currentFocusedRowIndex]
                    if (currentRowContainer.childCount > 0) {
                        val firstTile = currentRowContainer.getChildAt(0)
                        
                        // Condition A: If the first tile isn't focused yet, bounce focus directly to it!
                        if (!firstTile.isFocused) {
                            firstTile.requestFocus()
                            return true
                        }
                    }
                }
                
                // Condition B: If we're already on the first tile, slide open the sidebar menu
                showSidebarLayout()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
