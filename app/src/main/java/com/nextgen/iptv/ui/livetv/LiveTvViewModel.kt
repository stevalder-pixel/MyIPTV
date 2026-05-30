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
    private var allChannels = listOf<Channel>()

    fun configure(url: String, user: String, pass: String) {
        repo.configure(url, user, pass)
    }

    fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Test auth first
                val auth = repo.authenticate()
                if (auth.isFailure) {
                    _error.value = "Auth failed: ${auth.exceptionOrNull()?.message}"
                    _isLoading.value = false
                    return@launch
                }

                // Load categories
                val cats = repo.getLiveCategories()
                if (cats.isSuccess) {
                    _categories.value = cats.getOrNull() ?: emptyList()
                } else {
                    _error.value = "Categories failed: ${cats.exceptionOrNull()?.message}"
                    _isLoading.value = false
                    return@launch
                }

                // Load streams
                val streams = repo.getLiveStreams()
                if (streams.isSuccess) {
                    val list = streams.getOrNull() ?: emptyList()
                    allChannels = list.map { repo.toChannel(it) }
                    _channels.value = allChannels
                    if (allChannels.isEmpty()) {
                        _error.value = "No channels found"
                    }
                } else {
                    _error.value = "Channels failed: ${streams.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun filterByCategory(id: String) {
        _channels.value = if (id == "all") allChannels
        else allChannels.filter { it.group == id }
    }

    fun searchChannels(q: String) {
        _channels.value = if (q.isEmpty()) allChannels
        else allChannels.filter { it.name.contains(q, true) }
    }
}
