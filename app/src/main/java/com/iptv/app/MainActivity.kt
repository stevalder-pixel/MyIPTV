package com.iptv.app

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a root layout shell
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#001F3F")) // Midnight Blue
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Welcome Header
        val titleView = TextView(this).apply {
            text = "MyIPTV - NextGen TV Shell"
            textSize = 32f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 50)
        }
        rootLayout.addView(titleView)

        // Status Subtitle
        val subtitleView = TextView(this).apply {
            text = "Core Engine Active. Ready for Interface Build."
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#87CEEB")) // Sky Blue accent
        }
        rootLayout.addView(subtitleView)

        setContentView(rootLayout)
    }
}
