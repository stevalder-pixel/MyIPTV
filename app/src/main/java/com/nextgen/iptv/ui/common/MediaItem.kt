package com.nextgen.iptv.ui.common

data class MediaItem(
    val id: Int,          // TMDB id
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val overview: String,
    val year: String,
    val rating: Float,
    val type: String,     // "movie" or "series"
    val stremioId: String // real IMDB id e.g. "tt1234567" - fetched from TMDB
)
