package com.nextgen.iptv.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface StremioApiService {
    @GET
    suspend fun getManifest(@Url url: String): StremioManifest

    @GET
    suspend fun getCatalog(@Url url: String): StremioCatalogResponse

    @GET
    suspend fun getMeta(@Url url: String): StremioMetaResponse

    @GET
    suspend fun getStreams(@Url url: String): StremioStreamsResponse
}

data class StremioManifest(
    val id: String = "",
    val name: String = "",
    val version: String = "",
    val description: String = "",
    val types: List<String> = emptyList(),
    val catalogs: List<ManifestCatalog> = emptyList(),
    val resources: List<Any> = emptyList()
)

data class ManifestCatalog(
    val type: String = "",
    val id: String = "",
    val name: String = ""
)

data class StremioCatalogResponse(
    val metas: List<StremioMetaItem> = emptyList()
)

data class StremioMetaItem(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val poster: String = "",
    val background: String = "",
    val description: String = "",
    val year: Int = 0,
    @SerializedName("imdbRating") val imdbRating: String = "",
    val genres: List<String> = emptyList()
)

data class StremioMetaResponse(
    val meta: StremioMetaItem? = null
)

data class StremioStreamsResponse(
    val streams: List<StremioStreamItem> = emptyList()
)

data class StremioStreamItem(
    val name: String = "",
    val title: String = "",
    val url: String = "",
    val infoHash: String = "",
    val fileIdx: Int = -1,
    val behaviorHints: Map<String, Any> = emptyMap()
)
