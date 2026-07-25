package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recommendedMedia: List<MediaItem> = emptyList()
)

data class FilterState(
    val query: String = "",
    val mediaType: MediaType? = null,
    val freeOnly: Boolean = false,
    val selectedProvider: StreamingProvider? = null,
    val selectedGenre: String? = null,
    val minRating: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = MediaRepository(db.watchlistDao())

    val watchlist = repository.watchlistItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customLists = repository.customListNames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val watchedCount = repository.watchedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Room Database Search & Filter State
    private val _dbSearchQuery = MutableStateFlow("")
    val dbSearchQuery: StateFlow<String> = _dbSearchQuery.asStateFlow()

    private val _dbSelectedPlatform = MutableStateFlow("")
    val dbSelectedPlatform: StateFlow<String> = _dbSelectedPlatform.asStateFlow()

    private val _dbSelectedGenre = MutableStateFlow("")
    val dbSelectedGenre: StateFlow<String> = _dbSelectedGenre.asStateFlow()

    private val _dbFavoritesOnly = MutableStateFlow(false)
    val dbFavoritesOnly: StateFlow<Boolean> = _dbFavoritesOnly.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialRoomDatabase()
        }
    }

    val roomSearchResults: StateFlow<List<WatchlistItemEntity>> = combine(
        _dbSearchQuery,
        _dbSelectedPlatform,
        _dbSelectedGenre,
        _dbFavoritesOnly,
        watchlist
    ) { query, platform, genre, favoritesOnly, allWatchlist ->
        allWatchlist.filter { item ->
            val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true)
            val matchesPlatform = platform.isBlank() || item.platform.contains(platform, ignoreCase = true)
            val matchesGenre = genre.isBlank() || item.genre.contains(genre, ignoreCase = true)
            val matchesFavorite = !favoritesOnly || item.isFavorite
            matchesQuery && matchesPlatform && matchesGenre && matchesFavorite
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateDbSearchQuery(query: String) {
        _dbSearchQuery.value = query
    }

    fun setDbPlatformFilter(platform: String) {
        _dbSelectedPlatform.value = if (_dbSelectedPlatform.value == platform) "" else platform
    }

    fun setDbGenreFilter(genre: String) {
        _dbSelectedGenre.value = if (_dbSelectedGenre.value == genre) "" else genre
    }

    fun toggleDbFavoritesOnly(favoritesOnly: Boolean) {
        _dbFavoritesOnly.value = favoritesOnly
    }

    fun clearDbFilters() {
        _dbSearchQuery.value = ""
        _dbSelectedPlatform.value = ""
        _dbSelectedGenre.value = ""
        _dbFavoritesOnly.value = false
    }

    fun toggleRoomItemFavorite(item: WatchlistItemEntity) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(item.mediaId, !item.isFavorite)
        }
    }

    fun deleteRoomItem(item: WatchlistItemEntity) {
        viewModelScope.launch {
            repository.deleteWatchlistItem(item)
        }
    }

    // Filter & Search State
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Filtered Catalog
    val filteredMedia: StateFlow<List<MediaItem>> = _filterState.map { filter ->
        repository.searchAndFilter(
            query = filter.query,
            type = filter.mediaType,
            freeOnly = filter.freeOnly,
            selectedProvider = filter.selectedProvider,
            genre = filter.selectedGenre,
            minRating = filter.minRating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.getAllMedia()
    )

    // Selected Media Item Detail Sheet
    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

    // Trailer Player Modal State
    private val _playingTrailerUrl = MutableStateFlow<String?>(null)
    val playingTrailerUrl: StateFlow<String?> = _playingTrailerUrl.asStateFlow()

    // AI Assistant Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "Hello! I am your CineVerse AI Assistant 🍿. Ask me anything like:\n• 'Find sci-fi movies like Interstellar under 2 hours'\n• 'Best free anime on Tubi or Crunchyroll'\n• 'I'm tired and want a funny lighthearted comedy'"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _isGeneratingWatchlistRecs = MutableStateFlow(false)
    val isGeneratingWatchlistRecs: StateFlow<Boolean> = _isGeneratingWatchlistRecs.asStateFlow()

    private val _watchlistRecsResult = MutableStateFlow<String?>(null)
    val watchlistRecsResult: StateFlow<String?> = _watchlistRecsResult.asStateFlow()

    // Watch Party State
    private val _activeWatchParty = MutableStateFlow<WatchPartyRoom?>(null)
    val activeWatchParty: StateFlow<WatchPartyRoom?> = _activeWatchParty.asStateFlow()

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun setMediaTypeFilter(type: MediaType?) {
        _filterState.value = _filterState.value.copy(mediaType = type)
    }

    fun toggleFreeOnlyFilter(freeOnly: Boolean) {
        _filterState.value = _filterState.value.copy(freeOnly = freeOnly)
    }

    fun setProviderFilter(provider: StreamingProvider?) {
        _filterState.value = _filterState.value.copy(selectedProvider = provider)
    }

    fun setGenreFilter(genre: String?) {
        _filterState.value = _filterState.value.copy(selectedGenre = genre)
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }

    fun selectMedia(media: MediaItem?) {
        _selectedMedia.value = media
    }

    fun playTrailer(url: String?) {
        _playingTrailerUrl.value = url
    }

    fun toggleWatchlist(media: MediaItem, customListName: String? = null) {
        viewModelScope.launch {
            repository.toggleWatchlist(media, customListName = customListName)
        }
    }

    fun updateWatchStatus(mediaId: String, status: String) {
        viewModelScope.launch {
            repository.updateWatchStatus(mediaId, status)
        }
    }

    fun sendAiPrompt(userPrompt: String) {
        if (userPrompt.isBlank()) return

        val userMsg = ChatMessage("user", userPrompt)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAiThinking.value = true

        viewModelScope.launch {
            val responseText = repository.askAiAssistant(userPrompt)
            val matchedItems = repository.getAllMedia().filter { media ->
                userPrompt.contains(media.title, ignoreCase = true) ||
                        responseText.contains(media.title, ignoreCase = true) ||
                        media.genres.any { userPrompt.contains(it, ignoreCase = true) }
            }.take(3)

            val aiMsg = ChatMessage(
                sender = "ai",
                text = responseText,
                recommendedMedia = matchedItems
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    fun generateWatchlistRecommendations() {
        val currentWatchlist = watchlist.value
        _isGeneratingWatchlistRecs.value = true
        _isAiThinking.value = true

        val promptText = "Generate personalized movie recommendations based on my Room database watchlist (${currentWatchlist.size} saved titles)."
        val userMsg = ChatMessage("user", promptText)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val recsText = repository.generateWatchlistRecommendations(currentWatchlist)
            _watchlistRecsResult.value = recsText

            val matchedMedia = repository.getAllMedia().filter { media ->
                recsText.contains(media.title, ignoreCase = true) ||
                        media.genres.any { genre -> currentWatchlist.any { it.genre.contains(genre, ignoreCase = true) } }
            }.take(4)

            val aiMsg = ChatMessage(
                sender = "ai",
                text = recsText,
                recommendedMedia = matchedMedia
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isGeneratingWatchlistRecs.value = false
            _isAiThinking.value = false
        }
    }

    fun joinWatchParty(room: WatchPartyRoom) {
        _activeWatchParty.value = room
    }

    fun leaveWatchParty() {
        _activeWatchParty.value = null
    }
}
