package com.nextgen.iptv.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "myiptv_prefs")

object AppPreferences {
    private val TMDB_API_KEY = stringPreferencesKey("tmdb_api_key")
    private val RD_API_KEY = stringPreferencesKey("rd_api_key")
    private val TORBOX_API_KEY = stringPreferencesKey("torbox_api_key")
    private val ALLDEBRID_API_KEY = stringPreferencesKey("alldebrid_api_key")
    private val PREMIUMIZE_API_KEY = stringPreferencesKey("premiumize_api_key")
    private val M3U_URL = stringPreferencesKey("m3u_url")
    private val STALKER_PORTAL_URL = stringPreferencesKey("stalker_portal_url")
    private val STALKER_MAC = stringPreferencesKey("stalker_mac")

    fun getTmdbApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[TMDB_API_KEY] ?: "" }
    suspend fun setTmdbApiKey(c: Context, v: String) { c.dataStore.edit { it[TMDB_API_KEY] = v } }
    fun getRdApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[RD_API_KEY] ?: "" }
    suspend fun setRdApiKey(c: Context, v: String) { c.dataStore.edit { it[RD_API_KEY] = v } }
    fun getTorBoxApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[TORBOX_API_KEY] ?: "" }
    suspend fun setTorBoxApiKey(c: Context, v: String) { c.dataStore.edit { it[TORBOX_API_KEY] = v } }
    fun getAllDebridApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[ALLDEBRID_API_KEY] ?: "" }
    suspend fun setAllDebridApiKey(c: Context, v: String) { c.dataStore.edit { it[ALLDEBRID_API_KEY] = v } }
    fun getPremiumizeApiKey(c: Context): Flow<String> = c.dataStore.data.map { it[PREMIUMIZE_API_KEY] ?: "" }
    suspend fun setPremiumizeApiKey(c: Context, v: String) { c.dataStore.edit { it[PREMIUMIZE_API_KEY] = v } }
    fun getM3uUrl(c: Context): Flow<String> = c.dataStore.data.map { it[M3U_URL] ?: "" }
    suspend fun setM3uUrl(c: Context, v: String) { c.dataStore.edit { it[M3U_URL] = v } }
    fun getStalkerPortalUrl(c: Context): Flow<String> = c.dataStore.data.map { it[STALKER_PORTAL_URL] ?: "" }
    fun getStalkerMac(c: Context): Flow<String> = c.dataStore.data.map { it[STALKER_MAC] ?: "" }
    suspend fun setStalkerPortal(c: Context, url: String, mac: String) { c.dataStore.edit { it[STALKER_PORTAL_URL] = url; it[STALKER_MAC] = mac } }
}