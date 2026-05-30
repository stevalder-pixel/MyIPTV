package com.nextgen.iptv.tv

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.tv.TvContract
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat

@RequiresApi(Build.VERSION_CODES.O)
class TvLauncherService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread {
            setupLauncherChannel(applicationContext)
            jobFinished(params, false)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters?) = true

    companion object {
        private const val CHANNEL_ID_PREF = "launcher_channel_id"

        fun setupLauncherChannel(context: Context) {
            try {
                val prefs = context.getSharedPreferences("tv_launcher", Context.MODE_PRIVATE)
                var channelId = prefs.getLong(CHANNEL_ID_PREF, -1L)

                if (channelId == -1L) {
                    // Create the channel
                    val channel = Channel.Builder()
                        .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                        .setDisplayName("MyIPTV Hub")
                        .setDescription("Your IPTV Media Hub")
                        .setAppLinkIntentUri(Uri.parse("myiptv://home"))
                        .build()

                    val uri = context.contentResolver.insert(
                        TvContractCompat.Channels.CONTENT_URI,
                        channel.toContentValues()
                    )
                    channelId = uri?.let { ContentUris.parseId(it) } ?: -1L
                    prefs.edit().putLong(CHANNEL_ID_PREF, channelId).apply()

                    // Make it visible on launcher
                    TvContractCompat.requestChannelBrowsable(context, channelId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun addProgramToChannel(
            context: Context,
            channelId: Long,
            title: String,
            description: String,
            posterUri: String,
            intentUri: String
        ) {
            try {
                val program = PreviewProgram.Builder()
                    .setChannelId(channelId)
                    .setTitle(title)
                    .setDescription(description)
                    .setPosterArtUri(Uri.parse(posterUri))
                    .setIntentUri(Uri.parse(intentUri))
                    .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                    .build()

                context.contentResolver.insert(
                    TvContractCompat.PreviewPrograms.CONTENT_URI,
                    program.toContentValues()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
