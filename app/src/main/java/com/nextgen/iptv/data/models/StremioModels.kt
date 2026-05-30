package com.nextgen.iptv.data.models

data class StremioAddon(
    val id: String,
    val name: String,
    val version: String,
    val manifestUrl: String,
    val types: List<String> = emptyList(),
    val catalogs: List<StremioCatalog> = emptyList(),
    val isEnabled: Boolean = true
)

data class StremioCatalog(
    val type: String,
    val id: String,
    val name: String
)

data class StremioMeta(
    val id: String,
    val type: String,
    val name: String,
    val poster: String = "",
    val background: String = "",
    val description: String = "",
    val year: Int = 0,
    val imdbRating: String = "",
    val genres: List<String> = emptyList(),
    val runtime: String = "",
    val trailers: List<String> = emptyList()
)

data class StremioStream(
    val name: String = "",
    val title: String = "",
    val url: String = "",
    val infoHash: String = "",
    val fileIdx: Int = -1,
    val behaviorHints: StreamBehaviorHints = StreamBehaviorHints()
)

data class StreamBehaviorHints(
    val notWebReady: Boolean = false,
    val bingeGroup: String = ""
)

data class DebridResolvedStream(
    val url: String,
    val quality: String,
    val filename: String,
    val size: Long = 0,
    val service: String
)

data class TraktWatchlistItem(
    val rank: Int = 0,
    val traktId: Int,
    val tmdbId: Int = 0,
    val imdbId: String = "",
    val title: String,
    val year: Int = 0,
    val type: String, // "movie" or "show"
    val posterPath: String = "",
    val overview: String = ""
)

data class TraktHistoryItem(
    val watchedAt: String,
    val traktId: Int,
    val tmdbId: Int = 0,
    val imdbId: String = "",
    val title: String,
    val year: Int = 0,
    val type: String,
    val episode: TraktEpisode? = null
)

data class TraktEpisode(
    val season: Int,
    val number: Int,
    val title: String = ""
)
