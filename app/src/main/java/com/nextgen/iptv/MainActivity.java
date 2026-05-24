package com.nextgen.iptv;

import android.os.Bundle;
import android.app.Activity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.VideoView;
import android.net.Uri;
// Explicitly import the generated R class to fix dexBuilder failure
import com.nextgen.iptv.R;

public class MainActivity extends Activity {

    private RecyclerView posterRecyclerView;
    private VideoView mainVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        posterRecyclerView = findViewById(R.id.posterRecyclerView);
        mainVideoView = findViewById(R.id.mainVideoView);

        if (posterRecyclerView != null) {
            posterRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        if (mainVideoView != null) {
            Uri videoUri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            mainVideoView.setVideoURI(videoUri);
            mainVideoView.start();
        }
    }
}
