package com.nextgen.iptv;

import android.os.Bundle;
import android.app.Activity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.VideoView;
import android.net.Uri;

public class MainActivity extends Activity {

    private RecyclerView posterRecyclerView;
    private VideoView mainVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        // Bind the right-hand media content layouts
        posterRecyclerView = findViewById(id.posterRecyclerView);
        mainVideoView = findViewById(id.mainVideoView);

        if (posterRecyclerView != null) {
            posterRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // TMDB parsing logic will bind data straight here without touching the sidebar
        }

        // Initialize a quick sample background stream so the layout isn't dead space
        if (mainVideoView != null) {
            Uri videoUri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            mainVideoView.setVideoURI(videoUri);
            mainVideoView.start();
        }
    }
}
