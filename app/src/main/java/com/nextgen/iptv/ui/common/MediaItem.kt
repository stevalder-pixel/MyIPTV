package com.nextgen.iptv.ui.common

data class MediaItem(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val overview: String,
    val year: String,
    val rating: Float,
    val type: String, // "movie" or "series"
    val stremioId: String // imdb id or tmdb id for stremio lookup
)
