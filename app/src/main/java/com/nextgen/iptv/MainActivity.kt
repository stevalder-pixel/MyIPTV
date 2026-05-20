package com.nextgen.iptv

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#001F3F"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val titleView = TextView(this).apply {
            text = "MyIPTV - NextGen TV Shell"
            textSize = 32f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 50)
        }
        rootLayout.addView(titleView)

        val subtitleView = TextView(this).apply {
            text = "Namespace Reset Successful. Ready to code UI."
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#87CEEB"))
        }
        rootLayout.addView(subtitleView)

        setContentView(rootLayout)
    }
}
