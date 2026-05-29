package com.nextgen.iptv.ui.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.nextgen.iptv.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    companion object { const val EXTRA_STREAM_URL = "stream_url"; const val EXTRA_TITLE = "title"; const val EXTRA_IS_LIVE = "is_live" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()
        val url = intent.getStringExtra(EXTRA_STREAM_URL) ?: return
        binding.playerTitle.text = intent.getStringExtra(EXTRA_TITLE) ?: ""
        if (intent.getBooleanExtra(EXTRA_IS_LIVE, false)) binding.liveBadge.visibility = View.VISIBLE
        player = ExoPlayer.Builder(this).setTrackSelector(DefaultTrackSelector(this)).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(url))
            exo.playWhenReady = true
            exo.prepare()
            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    binding.bufferingSpinner.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                    if (state == Player.STATE_ENDED) finish()
                }
            })
        }
    }

    private fun hideSystemUI() { window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) }
    override fun onPause() { super.onPause(); player?.pause() }
    override fun onResume() { super.onResume(); player?.play(); hideSystemUI() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}
