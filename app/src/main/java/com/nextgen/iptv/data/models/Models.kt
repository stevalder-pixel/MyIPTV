package com.nextgen.iptv.data.models

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logo: String = "",
    val group: String = "",
    val epgId: String = "",
    val isFavourite: Boolean = false,
    val nowPlaying: String = "",
    val nextUp: String = ""
)

data class EpgEntry(
    val channelId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long
)

data class PlaybackItem(
    val title: String,
    val streamUrl: String,
    val posterUrl: String = "",
    val isLiveStream: Boolean = false
)
