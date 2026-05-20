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
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import java.io.IOException

data class TmdbResponse(
    @SerializedName("results") val results: List<TmdbMovie>
)

data class TmdbMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("backdrop_path") val backdropPath: String?
)

class MainActivity : FragmentActivity() {

    private val tmdbApiKey = "0d5f6d8e07ab385be6c228b7950798bf"

    private lateinit var mainLayout: LinearLayout
    private lateinit var sidebar: LinearLayout
    private lateinit var contentArea: LinearLayout
    
    private val sidebarViews = mutableListOf<View>()
    private val sectionContainers = mutableListOf<LinearLayout>()
    private val movieRows = mutableListOf<LinearLayout>()
    
    private var isDisplayingDetails = false
    private var lastActiveMenuIndex = 1
    private var currentFocusedRowIndex = 0
    private val httpClient = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#040710"))
            clipChildren = false 
            clipToPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
            setPadding(35, 0, 15, 0)
            setBackgroundColor(android.graphics.Color.TRANSPARENT) 
            layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = false
            clipChildren = false
            clipToPadding = false
        }

        val customVectorResIds = listOf(
            R.drawable.ic_tv_modern,
            R.drawable.ic_movie_modern,
            R.drawable.ic_series_modern,
            R.drawable.ic_settings_modern
        )

        customVectorResIds.forEachIndexed { index, resId ->
            val menuIconContainer = ImageView(this).apply {
                setImageResource(resId)
                setPadding(0, 40, 0, 40)
                isFocusable = true
                isFocusableInTouchMode = true
                setColorFilter(android.graphics.Color.parseColor("#4D5875"))
                layoutParams = LinearLayout.LayoutParams(65, 125).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        (view as ImageView).setColorFilter(android.graphics.Color.WHITE)
                        view.scaleX = 1.35f
                        view.scaleY = 1.35f
                        lastActiveMenuIndex = index
                        updateContentArea(index)
                    } else {
                        (view as ImageView).setColorFilter(android.graphics.Color.parseColor("#4D5875"))
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
            setPadding(35, 10, 45, 10) 
            clipChildren = false 
            clipToPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        mainLayout.addView(sidebar)
        mainLayout.addView(contentArea)
        setContentView(mainLayout)

        sidebarViews.getOrNull(1)?.requestFocus()
    }

    private fun hideSidebarLayout() {
        sidebar.visibility = View.GONE
        if (currentFocusedRowIndex in movieRows.indices && movieRows[currentFocusedRowIndex].childCount > 0) {
            movieRows[currentFocusedRowIndex].getChildAt(0).requestFocus()
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
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
            setPadding(20, 0, 0, 10)
        }
        contentArea.addView(titleView)

        if (menuIndex == 1 || menuIndex == 2) {
            val rowQueries = if (menuIndex == 1) {
                listOf("Trending Now" to "movie/popular", "Top Rated" to "movie/top_rated")
            } else {
                listOf("Popular Shows" to "tv/popular", "Top Rated Series" to "tv/top_rated")
            }

            val scrollContainer = ScrollView(this).apply {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                clipChildren = false
                clipToPadding = false
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val verticalLayout = LinearLayout(this).apply { 
                orientation = LinearLayout.VERTICAL 
                clipChildren = false
                clipToPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            rowQueries.forEachIndexed { rowIndex, pair ->
                val rowWrapper = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    clipChildren = false 
                    clipToPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 5, 0, 10)
                    }
                }

                val rowLabel = TextView(this).apply {
                    text = pair.first
                    textSize = 15f
                    setTextColor(android.graphics.Color.parseColor("#4E5B7C"))
                    setPadding(20, 0, 0, 0)
                }
                rowWrapper.addView(rowLabel)

                val horizontalScroll = HorizontalScrollView(this).apply {
                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                    setPadding(10, 20, 10, 40) 
                    clipToPadding = false      
                    clipChildren = false
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 420)
                }
                
                val rowItemsContainer = LinearLayout(this).apply { 
                    orientation = LinearLayout.HORIZONTAL 
                    clipChildren = false
                    clipToPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }

                horizontalScroll.addView(rowItemsContainer)
                rowWrapper.addView(horizontalScroll)
                verticalLayout.addView(rowWrapper)
                
                movieRows.add(rowItemsContainer)
                sectionContainers.add(rowWrapper)

                fetchTmdbMetadata(pair.second, rowItemsContainer, rowIndex)
            }
            
            scrollContainer.addView(verticalLayout)
            contentArea.addView(scrollContainer)
            isolateFocusedRow(0)
        }
    }

    private fun fetchTmdbMetadata(endpoint: String, container: LinearLayout, rowIndex: Int) {
        if (tmdbApiKey.isEmpty()) {
            addPlaceholderCards(container, rowIndex)
            return
        }

        val url = "https://api.themoviedb.org/3/" + endpoint + "?api_key=" + tmdbApiKey + "&language=en-US&page=1"
        val request = Request.Builder().url(url).build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { addPlaceholderCards(container, rowIndex) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread { addPlaceholderCards(container, rowIndex) }
                        return
                    }
                    val bodyString = response.body?.string() ?: ""
                    val tmdbData = gson.fromJson(bodyString, TmdbResponse::class.java)
                    
                    runOnUiThread {
                        if (tmdbData?.results != null && tmdbData.results.isNotEmpty()) {
                            populateMediaRow(container, tmdbData.results, rowIndex)
                        } else {
                            addPlaceholderCards(container, rowIndex)
                        }
                    }
                }
            }
        })
    }

    private fun populateMediaRow(container: LinearLayout, items: List<TmdbMovie>, rowIndex: Int) {
        items.forEachIndexed { itemIndex, mediaItem ->
            val cardContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isFocusable = true
                isFocusableInTouchMode = true
                clipChildren = false
                clipToPadding = false

                val cardNormal = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#0C1222"))
                    cornerRadius = 14f
                }
                val cardFocused = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#16213E"))
                    cornerRadius = 14f
                    setStroke(4, android.graphics.Color.WHITE)
                }
                background = cardNormal
                layoutParams = LinearLayout.LayoutParams(230, 345).apply {
                    setMargins(15, 0, 15, 0)
                    gravity = Gravity.CENTER_VERTICAL
                }

                val posterImageView = ImageView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    
                    val posterUrl = "https://image.tmdb.org/t/p/w342" + mediaItem.posterPath
                    load(posterUrl) {
                        crossfade(true)
                        transformations(RoundedCornersTransformation(14f))
                        error(android.R.drawable.ic_menu_gallery) 
                    }
                }
                addView(posterImageView)

                setOnFocusChangeListener { view, hasFocus ->
                    view.background = if (hasFocus) cardFocused else cardNormal
                    if (hasFocus) {
                        view.scaleX = 1.08f
                        view.scaleY = 1.08f
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
                                if (itemIndex == 0) {
                                    showSidebarLayout()
                                    return@setOnKeyListener true
                                }
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (rowIndex < sectionContainers.size - 1) {
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
                    showMediaDetails(mediaItem)
                }
            }
            container.addView(cardContainer)
        }
    }

    private fun addPlaceholderCards(container: LinearLayout, rowIndex: Int) {
        for (i in 1..6) {
            val placeholder = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(230, 345).apply { setMargins(15, 0, 15, 0) }
                setBackgroundColor(android.graphics.Color.parseColor("#0C1222"))
            }
            container.addView(placeholder)
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

    private fun showMediaDetails(movie: TmdbMovie) {
        isDisplayingDetails = true
        contentArea.removeAllViews()

        val detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.MATCH_PARENT)
            setPadding(30, 20, 20, 20)
        }

        val titleView = TextView(this).apply {
            text = movie.title ?: movie.name ?: "Unknown Title"
            textSize = 36f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 15)
        }
        detailLayout.addView(titleView)

        val descriptionView = TextView(this).apply {
            text = movie.overview ?: "No synopsis records found."
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#7A89A8"))
            setPadding(0, 0, 0, 45)
        }
        detailLayout.addView(descriptionView)

        val playButton = Button(this).apply {
            text = "▶  Stream Content"
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
                Toast.makeText(this@MainActivity, "Querying TorBox Stream Links for " + titleView.text + "...", Toast.LENGTH_SHORT).show()
            }
        }

        detailLayout.addView(playButton)
        contentArea.addView(detailLayout)
        playButton.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isDisplayingDetails) {
                isDisplayingDetails = false
                updateContentArea(lastActiveMenuIndex)
                return true
            }
            if (sidebar.visibility == View.GONE) {
                showSidebarLayout()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
