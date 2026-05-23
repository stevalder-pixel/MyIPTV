package com.nextgen.iptv

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize the IPTV Video Player framework
        val videoView = findViewById<VideoView>(R.id.mainVideoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Default public test channel stream link
        val defaultStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        videoView.setVideoURI(Uri.parse(defaultStreamUrl))
        videoView.start()

        // 2. Set up your TMDB Media Posters row
        val recyclerView = findViewById<RecyclerView>(R.id.posterRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // A curated sample array of TMDB poster assets to test render immediately
        val sampleTmdbPosters = listOf(
            "/vpn8bK6vBYm9wH6wZ88g9FlwZ4B.jpg", // Inside Out 2
            "/fqv8v6AycAb6r9gO9uPTwSgU6m5.jpg", // Deadpool & Wolverine
            "/8Y4N2Yv8616vbc9gOKo8Y6X2W9z.jpg", // Despicable Me 4
            "/qn98X2C5A9bZ8wgS9U6W0YgFlx4.jpg"  // Kingdom of the Planet of the Apes
        )

        // Bind the adapter logic to make clicking a poster fire up an action
        recyclerView.adapter = PosterAdapter(sampleTmdbPosters) { selectedPosterUrl ->
            Toast.makeText(this, "Loading media linked to poster!", Toast.LENGTH_SHORT).show()
            // Right here is where we link your stream link playlist hooks later!
        }
    }
}
