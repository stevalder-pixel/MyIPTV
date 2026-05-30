package com.nextgen.iptv.tv

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import com.nextgen.iptv.ui.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@RequiresApi(Build.VERSION_CODES.O)
object TvChannelManager {

    private const val PREF_NAME = "tv_channels"
    private const val PREF_TRENDING_CHANNEL = "trending_channel_id"
    private const val PREF_WATCHLIST_CHANNEL = "watchlist_channel_id"

    suspend fun setupChannels(context: Context) = withContext(Dispatchers.IO) {
        try {
            setupTrendingChannel(context)
            setupWatchlistChannel(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTrendingChannel(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var channelId = prefs.getLong(PREF_TRENDING_CHANNEL, -1L)

        if (channelId == -1L) {
            val channel = Channel.Builder()
                .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName("MyIPTV — Trending")
                .setDescription("Trending movies and shows")
                .setAppLinkIntentUri(Uri.parse("myiptv://home"))
                .build()

            val uri = context.contentResolver.insert(
                TvContractCompat.Channels.CONTENT_URI,
                channel.toContentValues()
            )
            channelId = uri?.let { ContentUris.parseId(it) } ?: -1L
            if (channelId != -1L) {
                prefs.edit().putLong(PREF_TRENDING_CHANNEL, channelId).apply()
                TvContractCompat.requestChannelBrowsable(context, channelId)
            }
        }
        return channelId
    }

    private fun setupWatchlistChannel(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var channelId = prefs.getLong(PREF_WATCHLIST_CHANNEL, -1L)

        if (channelId == -1L) {
            val channel = Channel.Builder()
                .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName("MyIPTV — Watchlist")
                .setDescription("Your Trakt watchlist")
                .setAppLinkIntentUri(Uri.parse("myiptv://watchlist"))
                .build()

            val uri = context.contentResolver.insert(
                TvContractCompat.Channels.CONTENT_URI,
                channel.toContentValues()
            )
            channelId = uri?.let { ContentUris.parseId(it) } ?: -1L
            if (channelId != -1L) {
                prefs.edit().putLong(PREF_WATCHLIST_CHANNEL, channelId).apply()
                TvContractCompat.requestChannelBrowsable(context, channelId)
            }
        }
        return channelId
    }

    suspend fun updateTrendingPrograms(context: Context, items: List<MediaItem>) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val channelId = prefs.getLong(PREF_TRENDING_CHANNEL, -1L)
            if (channelId == -1L) return@withContext

            // Clear existing programs
            context.contentResolver.delete(
                TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
                null, null
            )

            // Add new programs
            items.take(10).forEach { item ->
                val program = PreviewProgram.Builder()
                    .setChannelId(channelId)
                    .setTitle(item.title)
                    .setDescription(item.overview)
                    .setPosterArtUri(Uri.parse(item.posterUrl))
                    .setIntentUri(Uri.parse("myiptv://play?type=${item.type}&id=${item.stremioId}&title=${Uri.encode(item.title)}"))
                    .setType(
                        if (item.type == "movie") TvContractCompat.PreviewPrograms.TYPE_MOVIE
                        else TvContractCompat.PreviewPrograms.TYPE_TV_SERIES
                    )
                    .build()

                context.contentResolver.insert(
                    TvContractCompat.PreviewPrograms.CONTENT_URI,
                    program.toContentValues()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateWatchlistPrograms(context: Context, items: List<MediaItem>) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val channelId = prefs.getLong(PREF_WATCHLIST_CHANNEL, -1L)
            if (channelId == -1L) return@withContext

            context.contentResolver.delete(
                TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
                null, null
            )

            items.take(10).forEach { item ->
                val program = PreviewProgram.Builder()
                    .setChannelId(channelId)
                    .setTitle(item.title)
                    .setDescription(item.overview)
                    .setPosterArtUri(if (item.posterUrl.isNotEmpty()) Uri.parse(item.posterUrl) else null)
                    .setIntentUri(Uri.parse("myiptv://play?type=${item.type}&id=${item.stremioId}&title=${Uri.encode(item.title)}"))
                    .setType(
                        if (item.type == "movie") TvContractCompat.PreviewPrograms.TYPE_MOVIE
                        else TvContractCompat.PreviewPrograms.TYPE_TV_SERIES
                    )
                    .build()

                context.contentResolver.insert(
                    TvContractCompat.PreviewPrograms.CONTENT_URI,
                    program.toContentValues()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
