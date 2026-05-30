package com.nextgen.iptv.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "myiptv_prefs")

object AppPreferences {
    private val TMDB_KEY = stringPreferencesKey("tmdb_api_key")
    private val RD_KEY = stringPreferencesKey("rd_api_key")
    private val TORBOX_KEY = stringPreferencesKey("torbox_api_key")
    private val ALLDEBRID_KEY = stringPreferencesKey("alldebrid_api_key")
    private val PREMIUMIZE_KEY = stringPreferencesKey("premiumize_api_key")
    private val M3U_URL = stringPreferencesKey("m3u_url")
    private val STALKER_URL = stringPreferencesKey("stalker_url")
    private val STALKER_MAC = stringPreferencesKey("stalker_mac")
    private val TRAKT_TOKEN = stringPreferencesKey("trakt_access_token")
    private val TRAKT_REFRESH = stringPreferencesKey("trakt_refresh_token")
    private val AUTO_SCROBBLE = booleanPreferencesKey("auto_scrobble")
    private val XTREAM_URL = stringPreferencesKey("xtream_url")
    private val XTREAM_USER = stringPreferencesKey("xtream_username")
    private val XTREAM_PASS = stringPreferencesKey("xtream_password")
    private val ACTIVE_DEBRID = stringPreferencesKey("active_debrid")
    private val STREMIO_ADDONS = stringPreferencesKey("stremio_addons")

    fun getTmdbApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[TMDB_KEY] ?: "" }
    suspend fun setTmdbApiKey(c: Context, v: String) { c.dataStore.edit { it[TMDB_KEY] = v } }

    fun getRdApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[RD_KEY] ?: "" }
    suspend fun setRdApiKey(c: Context, v: String) { c.dataStore.edit { it[RD_KEY] = v } }

    fun getTorBoxApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[TORBOX_KEY] ?: "" }
    suspend fun setTorBoxApiKey(c: Context, v: String) { c.dataStore.edit { it[TORBOX_KEY] = v } }

    fun getAllDebridApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[ALLDEBRID_KEY] ?: "" }
    suspend fun setAllDebridApiKey(c: Context, v: String) { c.dataStore.edit { it[ALLDEBRID_KEY] = v } }

    fun getPremiumizeApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[PREMIUMIZE_KEY] ?: "" }
    suspend fun setPremiumizeApiKey(c: Context, v: String) { c.dataStore.edit { it[PREMIUMIZE_KEY] = v } }

    fun getM3uUrl(c: Context): Flow<String> = c.dataStore.data.map { it[M3U_URL] ?: "" }
    suspend fun setM3uUrl(c: Context, v: String) { c.dataStore.edit { it[M3U_URL] = v } }

    fun getStalkerPortalUrl(c: Context): Flow<String> = c.dataStore.data.map { it[STALKER_URL] ?: "" }
    fun getStalkerMac(c: Context): Flow<String> = c.dataStore.data.map { it[STALKER_MAC] ?: "" }
    suspend fun setStalkerPortal(c: Context, url: String, mac: String) {
        c.dataStore.edit { it[STALKER_URL] = url; it[STALKER_MAC] = mac }
    }

    fun getTraktAccessToken(c: Context): Flow<String> = c.dataStore.data.map { it[TRAKT_TOKEN] ?: "" }
    fun getTraktRefreshToken(c: Context): Flow<String> = c.dataStore.data.map { it[TRAKT_REFRESH] ?: "" }
    suspend fun setTraktTokens(c: Context, access: String, refresh: String) {
        c.dataStore.edit { it[TRAKT_TOKEN] = access; it[TRAKT_REFRESH] = refresh }
    }
    suspend fun setTraktAccessToken(c: Context, v: String) { c.dataStore.edit { it[TRAKT_TOKEN] = v } }

    fun getAutoScrobble(c: Context): Flow<Boolean> = c.dataStore.data.map { it[AUTO_SCROBBLE] ?: true }
    suspend fun setAutoScrobble(c: Context, v: Boolean) { c.dataStore.edit { it[AUTO_SCROBBLE] = v } }

    fun getXtreamUrl(c: Context): Flow<String> = c.dataStore.data.map { it[XTREAM_URL] ?: "" }
    fun getXtreamUsername(c: Context): Flow<String> = c.dataStore.data.map { it[XTREAM_USER] ?: "" }
    fun getXtreamPassword(c: Context): Flow<String> = c.dataStore.data.map { it[XTREAM_PASS] ?: "" }
    suspend fun setXtreamCredentials(c: Context, url: String, user: String, pass: String) {
        c.dataStore.edit { it[XTREAM_URL] = url; it[XTREAM_USER] = user; it[XTREAM_PASS] = pass }
    }

    fun getActiveDebrid(c: Context): Flow<String> = c.dataStore.data.map { it[ACTIVE_DEBRID] ?: "torbox" }
    suspend fun setActiveDebrid(c: Context, v: String) { c.dataStore.edit { it[ACTIVE_DEBRID] = v } }

    fun getStremioAddons(c: Context): Flow<List<String>> = c.dataStore.data.map {
        try { Gson().fromJson(it[STREMIO_ADDONS] ?: "[]", object : TypeToken<List<String>>() {}.type) }
        catch (e: Exception) { emptyList() }
    }
    suspend fun addStremioAddon(c: Context, url: String) {
        c.dataStore.edit { prefs ->
            val current: MutableList<String> = try {
                Gson().fromJson(prefs[STREMIO_ADDONS] ?: "[]", object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) { mutableListOf() }
            if (!current.contains(url)) current.add(url)
            prefs[STREMIO_ADDONS] = Gson().toJson(current)
        }
    }
    suspend fun removeStremioAddon(c: Context, url: String) {
        c.dataStore.edit { prefs ->
            val current: MutableList<String> = try {
                Gson().fromJson(prefs[STREMIO_ADDONS] ?: "[]", object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) { mutableListOf() }
            current.remove(url)
            prefs[STREMIO_ADDONS] = Gson().toJson(current)
        }
    }
}
