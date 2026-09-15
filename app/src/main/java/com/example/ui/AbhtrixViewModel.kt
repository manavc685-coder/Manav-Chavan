package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MediaItem
import com.example.data.repository.AbhtrixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AbhtrixViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AbhtrixRepository.getInstance(application)

    val trending: StateFlow<List<MediaItem>> = repository.trendingMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popular: StateFlow<List<MediaItem>> = repository.popularMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latest: StateFlow<List<MediaItem>> = repository.latestMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webSeries: StateFlow<List<MediaItem>> = repository.webSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<MediaItem>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommended: StateFlow<List<MediaItem>> = repository.recommendedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<MediaItem>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    val searchResults: StateFlow<List<MediaItem>> = combine(
        _searchQuery,
        _selectedGenre,
        repository.allMedia
    ) { query, genre, list ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.genre.contains(query, ignoreCase = true) ||
                    item.cast.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)

            val matchesGenre = genre == "All" ||
                    item.genre.contains(genre, ignoreCase = true) ||
                    (genre == "Movies" && item.type == "MOVIE") ||
                    (genre == "Series" && (item.type == "SERIES" || item.isWebSeries))

            matchesQuery && matchesGenre
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
    }

    fun toggleWatchlist(mediaId: String) {
        viewModelScope.launch {
            repository.toggleWatchlist(mediaId)
        }
    }

    fun updateWatchProgress(mediaId: String, progress: Float, posMs: Long) {
        viewModelScope.launch {
            repository.updateWatchProgress(mediaId, progress, posMs)
        }
    }

    // Admin panel actions
    fun addOrUpdateMedia(item: MediaItem) {
        viewModelScope.launch {
            repository.addMedia(item)
        }
    }

    fun deleteMedia(id: String) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }

    fun resetCatalog() {
        viewModelScope.launch {
            repository.resetToDefaultCatalog()
        }
    }
}
