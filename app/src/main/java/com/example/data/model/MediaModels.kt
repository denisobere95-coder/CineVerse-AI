package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType(val displayName: String) {
    MOVIE("Movie"),
    TV_SHOW("TV Series"),
    ANIME("Anime"),
    CARTOON("Cartoon"),
    DOCUMENTARY("Documentary"),
    K_DRAMA("Korean Drama"),
    NOLLYWOOD("Nollywood / African"),
    BOLLYWOOD("Bollywood")
}

enum class StreamingProvider(
    val id: String,
    val displayName: String,
    val isFree: Boolean,
    val accentHex: String
) {
    TUBI("tubi", "Tubi TV", true, "#FA233B"),
    PLUTO_TV("pluto", "Pluto TV", true, "#000000"),
    YOUTUBE("youtube", "YouTube Free / Rentals", true, "#FF0000"),
    CRACKLE("crackle", "Crackle", true, "#FF5A00"),
    PLEX("plex", "Plex Free TV", true, "#E5A00D"),
    NETFLIX("netflix", "Netflix", false, "#E50914"),
    PRIME_VIDEO("prime", "Prime Video", false, "#00A8E1"),
    DISNEY_PLUS("disney", "Disney+", false, "#0063E5"),
    CRUNCHYROLL("crunchyroll", "Crunchyroll", false, "#FF6600"),
    MAX("max", "Max", false, "#5822B4"),
    HULU("hulu", "Hulu", false, "#1CE783"),
    APPLE_TV("apple", "Apple TV+", false, "#A2A2A2"),
    PARAMOUNT_PLUS("paramount", "Paramount+", false, "#0064FF")
}

enum class AccessType(val label: String) {
    STREAM("Included in Subscription"),
    FREE_WITH_ADS("Free with Ads"),
    RENT("Rent"),
    BUY("Buy"),
    COMING_SOON("Coming Soon"),
    LEAVING_SOON("Leaving Soon")
}

data class ProviderAvailability(
    val provider: StreamingProvider,
    val accessType: AccessType,
    val price: String? = null,
    val deepLinkUrl: String,
    val quality: String = "4K UHD",
    val audio: String = "5.1 Surround",
    val badgeText: String? = null,
    val isExclusive: Boolean = false
)

data class ReviewSummary(
    val pros: List<String>,
    val cons: List<String>,
    val audienceScore: Int,
    val criticScore: Int,
    val aiConsensus: String
)

data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val releaseYear: Int,
    val runtimeMinutes: Int,
    val posterUrl: String,
    val backdropUrl: String? = null,
    val genres: List<String>,
    val ratingImdb: Double,
    val ratingRottenTomatoes: Int,
    val ratingAiScore: Int,
    val overview: String,
    val tagline: String,
    val ageRating: String,
    val director: String,
    val cast: List<String>,
    val languages: List<String>,
    val subtitles: List<String>,
    val trailerUrl: String,
    val availableProviders: List<ProviderAvailability>,
    val reviewSummary: ReviewSummary,
    val country: String = "Global",
    val badge: String? = null
)

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val mediaType: String,
    val posterUrl: String,
    val year: Int = 0,
    val platform: String = "",
    val genre: String = "",
    val isFavorite: Boolean = false,
    val ratingImdb: Double = 0.0,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val watchStatus: String = "WANT_TO_WATCH", // WANT_TO_WATCH, WATCHED, IN_PROGRESS
    val userRating: Int? = null,
    val notes: String? = null,
    val customListName: String? = null
)

data class UserBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progressPercent: Float = 1.0f
)

data class WatchPartyRoom(
    val id: String,
    val title: String,
    val mediaTitle: String,
    val providerName: String,
    val hostName: String,
    val participantsCount: Int,
    val isLive: Boolean = true
)
