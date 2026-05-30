package com.nextgen.iptv.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.nextgen.iptv.data.repository.TraktRepository
import com.nextgen.iptv.databinding.ActivityPlayerBinding
import kotlinx.coroutines.launch

@UnstableApi
class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private val scrobbleHandler = Handler(Looper.getMainLooper())
    private var scrobbleRunnable: Runnable? = null
    private var hasScrobbled = false

    companion object {
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_IS_LIVE = "is_live"
        const val EXTRA_YEAR = "year"
        const val EXTRA_IMDB_ID = "imdb_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        val url = intent.getStringExtra(EXTRA_STREAM_URL) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, false)
        val year = intent.getIntExtra(EXTRA_YEAR, 0)
        val imdbId = intent.getStringExtra(EXTRA_IMDB_ID) ?: ""

        binding.playerTitle.text = title
        if (isLive) binding.liveBadge.visibility = View.VISIBLE

        // Start Trakt scrobble
        if (!isLive && title.isNotEmpty()) {
            lifecycleScope.launch {
                TraktRepository.instance.scrobbleStart(this@PlayerActivity, title, year, imdbId, 0f)
            }
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .build().also { exo ->
                binding.playerView.player = exo
                exo.setMediaItem(MediaItem.fromUri(url))
                exo.playWhenReady = true
                exo.prepare()

                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        binding.bufferingSpinner.visibility =
                            if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE

                        if (state == Player.STATE_READY && !isLive) {
                            // Schedule scrobble at 80% watched
                            scheduleScrobble(exo, title, year, imdbId)
                        }
                        if (state == Player.STATE_ENDED) finish()
                    }
                })
            }
    }

    private fun scheduleScrobble(exo: ExoPlayer, title: String, year: Int, imdbId: String) {
        scrobbleRunnable = Runnable {
            val duration = exo.duration
            val position = exo.currentPosition
            if (duration > 0) {
                val progress = (position.toFloat() / duration.toFloat()) * 100f
                if (progress >= 80f && !hasScrobbled) {
                    hasScrobbled = true
                    lifecycleScope.launch {
                        TraktRepository.instance.scrobbleStop(this@PlayerActivity, title, year, imdbId, progress)
                    }
                }
            }
            scrobbleHandler.postDelayed(scrobbleRunnable!!, 30000)
        }
        scrobbleHandler.postDelayed(scrobbleRunnable!!, 30000)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
        scrobbleRunnable?.let { scrobbleHandler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        scrobbleRunnable?.let { scrobbleHandler.removeCallbacks(it) }
        player?.release()
        player = null
    }
}
