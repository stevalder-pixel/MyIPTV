package com.nextgen.iptv.ui.livetv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.iptv.data.api.XtreamCategory
import com.nextgen.iptv.data.models.Channel
import com.nextgen.iptv.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class LiveTvViewModel : ViewModel() {

    private val repo = XtreamRepository.instance

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _categories = MutableLiveData<List<XtreamCategory>>()
    val categories: LiveData<List<XtreamCategory>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _authStatus = MutableLiveData<String>()
    val authStatus: LiveData<String> = _authStatus

    private var allChannels = listOf<Channel>()

    fun configure(serverUrl: String, username: String, password: String) {
        repo.configure(serverUrl, username, password)
    }

    fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Authenticate first
                val auth = repo.authenticate()
                if (auth.isSuccess) {
                    val info = auth.getOrNull()?.userInfo
                    _authStatus.value = "Connected — expires ${info?.expDate ?: "unknown"}"
                }

                // Load categories
                val cats = repo.getLiveCategories()
                if (cats.isSuccess) {
                    _categories.value = cats.getOrNull() ?: emptyList()
                }

                // Load all streams
                val streams = repo.getLiveStreams()
                if (streams.isSuccess) {
                    allChannels = streams.getOrNull()?.map { repo.toChannel(it) } ?: emptyList()
                    _channels.value = allChannels
                } else {
                    _error.value = "Failed to load channels"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(categoryId: String) {
        if (categoryId == "all") {
            _channels.value = allChannels
        } else {
            _channels.value = allChannels.filter { it.group == categoryId }
        }
    }

    fun searchChannels(query: String) {
        _channels.value = if (query.isEmpty()) allChannels
        else allChannels.filter { it.name.contains(query, ignoreCase = true) }
    }
}
