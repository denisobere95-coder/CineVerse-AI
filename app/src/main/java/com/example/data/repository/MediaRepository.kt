package com.example.data.repository

import com.example.data.db.WatchlistDao
import com.example.data.model.*
import com.example.data.remote.GeminiAiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val watchlistDao: WatchlistDao) {

    val watchlistItems: Flow<List<WatchlistItemEntity>> = watchlistDao.getAllWatchlistItems()
    val favoriteItems: Flow<List<WatchlistItemEntity>> = watchlistDao.getFavoriteMovies()
    val watchedCount: Flow<Int> = watchlistDao.getWatchedCount()
    val customListNames: Flow<List<String>> = watchlistDao.getCustomListNames()

    fun searchRoomDatabase(query: String = "", platform: String = "", genre: String = ""): Flow<List<WatchlistItemEntity>> {
        return watchlistDao.searchAndFilterWatchlist(query, platform, genre)
    }

    suspend fun seedInitialRoomDatabase() {
        if (watchlistDao.getWatchlistCount() == 0) {
            val sampleMedia = getAllMedia().take(6)
            sampleMedia.forEachIndexed { index, media ->
                watchlistDao.insertOrUpdateItem(
                    WatchlistItemEntity(
                        mediaId = media.id,
                        title = media.title,
                        mediaType = media.type.displayName,
                        posterUrl = media.posterUrl,
                        year = media.releaseYear,
                        platform = media.availableProviders.firstOrNull()?.provider?.displayName ?: if (index % 2 == 0) "Netflix" else "Tubi",
                        genre = media.genres.firstOrNull() ?: "Sci-Fi",
                        isFavorite = (index % 2 == 0),
                        ratingImdb = media.ratingImdb,
                        watchStatus = if (index == 0) "WATCHED" else "WANT_TO_WATCH"
                    )
                )
            }
        }
    }

    suspend fun toggleWatchlist(media: MediaItem, status: String = "WANT_TO_WATCH", customListName: String? = null) {
        val existing = watchlistDao.getItemById(media.id)
        if (existing != null) {
            watchlistDao.deleteItemById(media.id)
        } else {
            val entity = WatchlistItemEntity(
                mediaId = media.id,
                title = media.title,
                mediaType = media.type.displayName,
                posterUrl = media.posterUrl,
                year = media.releaseYear,
                platform = media.availableProviders.firstOrNull()?.provider?.displayName ?: "Streaming",
                genre = media.genres.firstOrNull() ?: "General",
                isFavorite = false,
                ratingImdb = media.ratingImdb,
                watchStatus = status,
                customListName = customListName
            )
            watchlistDao.insertOrUpdateItem(entity)
        }
    }

    suspend fun toggleFavorite(media: MediaItem) {
        val existing = watchlistDao.getItemById(media.id)
        if (existing != null) {
            watchlistDao.updateFavoriteStatus(media.id, !existing.isFavorite)
        } else {
            val entity = WatchlistItemEntity(
                mediaId = media.id,
                title = media.title,
                mediaType = media.type.displayName,
                posterUrl = media.posterUrl,
                year = media.releaseYear,
                platform = media.availableProviders.firstOrNull()?.provider?.displayName ?: "Streaming",
                genre = media.genres.firstOrNull() ?: "General",
                isFavorite = true,
                ratingImdb = media.ratingImdb,
                watchStatus = "WANT_TO_WATCH"
            )
            watchlistDao.insertOrUpdateItem(entity)
        }
    }

    suspend fun updateWatchStatus(mediaId: String, status: String) {
        watchlistDao.updateWatchStatus(mediaId, status)
    }

    suspend fun updateFavoriteStatus(mediaId: String, isFavorite: Boolean) {
        watchlistDao.updateFavoriteStatus(mediaId, isFavorite)
    }

    suspend fun deleteWatchlistItem(item: WatchlistItemEntity) {
        watchlistDao.deleteItem(item)
    }

    suspend fun isMediaInWatchlist(mediaId: String): Boolean {
        return watchlistDao.getItemById(mediaId) != null
    }

    suspend fun askAiAssistant(query: String): String {
        return GeminiAiClient.askAssistant(query)
    }

    suspend fun generateWatchlistRecommendations(watchlist: List<WatchlistItemEntity>): String {
        return GeminiAiClient.generateRecommendationsFromWatchlist(watchlist)
    }

    fun getAllMedia(): List<MediaItem> = mockMediaCatalog

    fun getTrending(): List<MediaItem> = mockMediaCatalog.filter { it.badge == "Trending" || it.ratingAiScore >= 92 }

    fun getFreeToWatch(): List<MediaItem> = mockMediaCatalog.filter { media ->
        media.availableProviders.any { it.provider.isFree || it.accessType == AccessType.FREE_WITH_ADS }
    }

    fun getAnime(): List<MediaItem> = mockMediaCatalog.filter { it.type == MediaType.ANIME }

    fun getLeavingSoon(): List<MediaItem> = mockMediaCatalog.filter { media ->
        media.availableProviders.any { it.accessType == AccessType.LEAVING_SOON } || media.badge == "Leaving Soon"
    }

    fun getAwardWinners(): List<MediaItem> = mockMediaCatalog.filter { it.badge == "Oscar Winner" || it.ratingImdb >= 8.7 }

    fun getHiddenGems(): List<MediaItem> = mockMediaCatalog.filter { it.badge == "Hidden Gem" || (it.ratingAiScore >= 90 && it.ratingImdb >= 7.8) }

    fun searchAndFilter(
        query: String = "",
        type: MediaType? = null,
        freeOnly: Boolean = false,
        selectedProvider: StreamingProvider? = null,
        genre: String? = null,
        minRating: Double = 0.0
    ): List<MediaItem> {
        return mockMediaCatalog.filter { media ->
            val matchesQuery = query.isEmpty() ||
                    media.title.contains(query, ignoreCase = true) ||
                    media.director.contains(query, ignoreCase = true) ||
                    media.cast.any { it.contains(query, ignoreCase = true) } ||
                    media.genres.any { it.contains(query, ignoreCase = true) }

            val matchesType = type == null || media.type == type

            val matchesFree = !freeOnly || media.availableProviders.any {
                it.provider.isFree || it.accessType == AccessType.FREE_WITH_ADS
            }

            val matchesProvider = selectedProvider == null || media.availableProviders.any {
                it.provider == selectedProvider
            }

            val matchesGenre = genre.isNullOrEmpty() || media.genres.contains(genre)

            val matchesRating = media.ratingImdb >= minRating

            matchesQuery && matchesType && matchesFree && matchesProvider && matchesGenre && matchesRating
        }
    }

    val badges: List<UserBadge> = listOf(
        UserBadge("1", "Movie Master", "Watched 25+ feature films", "movie", true, 1.0f),
        UserBadge("2", "Anime Otaku", "Explored 10+ anime series", "tv", true, 1.0f),
        UserBadge("3", "Free Stream Explorer", "Discovered 5+ legal free platforms", "volunteer_activism", true, 1.0f),
        UserBadge("4", "Sci-Fi Voyager", "Watched Interstellar & Dune", "rocket_launch", true, 1.0f),
        UserBadge("5", "Comedy Enthusiast", "Laughed through 15+ comedy shows", "sentiment_very_satisfied", false, 0.6f),
        UserBadge("6", "AI Cinephile", "Prompted AI assistant 10+ times", "auto_awesome", true, 1.0f)
    )

    val watchPartyRooms: List<WatchPartyRoom> = listOf(
        WatchPartyRoom("wp1", "Sci-Fi Lovers Night", "Interstellar", "Prime Video", "Elena_Stream", 42),
        WatchPartyRoom("wp2", "One Piece Episode Marathon", "One Piece", "Crunchyroll & Tubi", "AnimeFan99", 128),
        WatchPartyRoom("wp3", "Classic Free Horror", "Night of the Living Dead", "Tubi TV", "MovieBuff_Alex", 35),
        WatchPartyRoom("wp4", "Studio Ghibli Co-Watch", "Spirited Away", "Max", "Chihiro_Fan", 89)
    )

    companion object {
        val mockMediaCatalog = listOf(
            MediaItem(
                id = "m1",
                title = "Interstellar",
                type = MediaType.MOVIE,
                releaseYear = 2014,
                runtimeMinutes = 169,
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop",
                backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1000&auto=format&fit=crop",
                genres = listOf("Sci-Fi", "Drama", "Adventure"),
                ratingImdb = 8.7,
                ratingRottenTomatoes = 73,
                ratingAiScore = 98,
                overview = "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans.",
                tagline = "Mankind was born on Earth. It was never meant to die here.",
                ageRating = "PG-13",
                director = "Christopher Nolan",
                cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"),
                languages = listOf("English", "Spanish", "French"),
                subtitles = listOf("English [CC]", "Spanish", "French", "Japanese"),
                trailerUrl = "https://www.youtube.com/watch?v=zSWdZVtXT7E",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.PRIME_VIDEO, AccessType.STREAM, null, "https://www.amazon.com/Prime-Video/b?node=2676882011"),
                    ProviderAvailability(StreamingProvider.MAX, AccessType.STREAM, null, "https://www.max.com"),
                    ProviderAvailability(StreamingProvider.PLUTO_TV, AccessType.FREE_WITH_ADS, null, "https://pluto.tv"),
                    ProviderAvailability(StreamingProvider.YOUTUBE, AccessType.RENT, "$3.99", "https://youtube.com/movies")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Visual masterpiece by Christopher Nolan", "Emotional Hans Zimmer score", "Scientifically grounded physics"),
                    cons = listOf("Complex exposition in final act", "Sound mixing can obscure dialogue"),
                    audienceScore = 92,
                    criticScore = 73,
                    aiConsensus = "A monumental sci-fi epic that blends high-concept space exploration with profound emotional resonance."
                ),
                badge = "Trending"
            ),
            MediaItem(
                id = "m2",
                title = "One Piece",
                type = MediaType.ANIME,
                releaseYear = 1999,
                runtimeMinutes = 24,
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1000&auto=format&fit=crop",
                genres = listOf("Anime", "Action", "Adventure", "Fantasy"),
                ratingImdb = 8.9,
                ratingRottenTomatoes = 90,
                ratingAiScore = 96,
                overview = "Monkey D. Luffy sets off on an epic journey across the high seas with his straw-hat-wearing crew to find the legendary treasure 'One Piece' and become the Pirate King.",
                tagline = "Freedom, friendship, and the ultimate treasure of the seas!",
                ageRating = "TV-14",
                director = "Tatsuya Nagamine",
                cast = listOf("Mayumi Tanaka", "Kazuya Nakai", "Akemi Okamura", "Kappei Yamaguchi"),
                languages = listOf("Japanese", "English", "Spanish"),
                subtitles = listOf("English", "Spanish", "Portuguese", "French"),
                trailerUrl = "https://www.youtube.com/watch?v=MCb13lbVGE0",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.CRUNCHYROLL, AccessType.FREE_WITH_ADS, null, "https://www.crunchyroll.com/series/GRMG8WQ1W/one-piece"),
                    ProviderAvailability(StreamingProvider.NETFLIX, AccessType.STREAM, null, "https://www.netflix.com/title/80107103"),
                    ProviderAvailability(StreamingProvider.TUBI, AccessType.FREE_WITH_ADS, null, "https://tubitv.com/series/300008064/one-piece"),
                    ProviderAvailability(StreamingProvider.PLUTO_TV, AccessType.FREE_WITH_ADS, null, "https://pluto.tv/en/live-tv/one-piece")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("World-building is unrivaled in anime history", "Incredible emotional character arcs", "Huge episode library with free tiers"),
                    cons = listOf("Pacing slows during certain arcs"),
                    audienceScore = 95,
                    criticScore = 90,
                    aiConsensus = "The gold standard of shonen anime, offering unforgettable adventures, world-building, and heart."
                ),
                badge = "Trending"
            ),
            MediaItem(
                id = "m3",
                title = "Night of the Living Dead",
                type = MediaType.MOVIE,
                releaseYear = 1968,
                runtimeMinutes = 96,
                posterUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=600&auto=format&fit=crop",
                genres = listOf("Horror", "Classic", "Sci-Fi"),
                ratingImdb = 7.8,
                ratingRottenTomatoes = 96,
                ratingAiScore = 91,
                overview = "A ragtag group of survivors barricade themselves in an abandoned farmhouse while bloodthirsty reanimated corpses surround them.",
                tagline = "They won't stay dead!",
                ageRating = "NR",
                director = "George A. Romero",
                cast = listOf("Duane Jones", "Judith O'Dea", "Karl Hardman"),
                languages = listOf("English"),
                subtitles = listOf("English [CC]", "Spanish"),
                trailerUrl = "https://www.youtube.com/watch?v=0T13v9-p3m8",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.TUBI, AccessType.FREE_WITH_ADS, null, "https://tubitv.com"),
                    ProviderAvailability(StreamingProvider.PLUTO_TV, AccessType.FREE_WITH_ADS, null, "https://pluto.tv"),
                    ProviderAvailability(StreamingProvider.PLEX, AccessType.FREE_WITH_ADS, null, "https://www.plex.tv"),
                    ProviderAvailability(StreamingProvider.YOUTUBE, AccessType.FREE_WITH_ADS, "Free Public Domain", "https://youtube.com")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Revolutionary indie masterpiece", "Tense claustrophobic atmosphere", "100% legally free public domain classic"),
                    cons = listOf("Vintage black-and-white print quality"),
                    audienceScore = 88,
                    criticScore = 96,
                    aiConsensus = "George A. Romero's seminal horror landmark that birthed the modern zombie genre. Available 100% free everywhere."
                ),
                badge = "Free Classic"
            ),
            MediaItem(
                id = "m4",
                title = "Arcane",
                type = MediaType.CARTOON,
                releaseYear = 2021,
                runtimeMinutes = 40,
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop",
                genres = listOf("Animation", "Action", "Sci-Fi", "Fantasy"),
                ratingImdb = 9.0,
                ratingRottenTomatoes = 100,
                ratingAiScore = 99,
                overview = "Amid the stark discord of twin cities Piltover and Zaun, two sisters fight on rival sides of a war between magic technologies and conviction.",
                tagline = "Every legend has a beginning.",
                ageRating = "TV-14",
                director = "Pascal Charrue, Arnaud Delord",
                cast = listOf("Hailee Steinfeld", "Ella Purnell", "Kevin Alejandro"),
                languages = listOf("English", "French", "Japanese", "German"),
                subtitles = listOf("English [CC]", "Spanish", "Japanese", "Arabic"),
                trailerUrl = "https://www.youtube.com/watch?v=fXmAurh012s",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.NETFLIX, AccessType.STREAM, null, "https://www.netflix.com/title/81435684")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Breathtaking hybrid 2D/3D animation style", "Deep character psychology & tragedy", "Flawless soundtrack"),
                    cons = listOf("Requires waiting between seasons"),
                    audienceScore = 98,
                    criticScore = 100,
                    aiConsensus = "A staggering visual and narrative achievement that transcends videogame adaptations to become a modern animated classic."
                ),
                badge = "Award Winner"
            ),
            MediaItem(
                id = "m5",
                title = "Free Solo",
                type = MediaType.DOCUMENTARY,
                releaseYear = 2018,
                runtimeMinutes = 100,
                posterUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop",
                genres = listOf("Documentary", "Sports", "Adventure"),
                ratingImdb = 8.1,
                ratingRottenTomatoes = 98,
                ratingAiScore = 94,
                overview = "Follow Alex Honnold as he attempts to achieve the lifelong dream of free solo climbing the 3,000ft El Capitan wall in Yosemite National Park without a rope.",
                tagline = "Imagine stepping off the edge of a cliff with no safety net.",
                ageRating = "PG-13",
                director = "Elizabeth Chai Vasarhelyi, Jimmy Chin",
                cast = listOf("Alex Honnold", "Tommy Caldwell", "Jimmy Chin"),
                languages = listOf("English"),
                subtitles = listOf("English [CC]", "Spanish", "French"),
                trailerUrl = "https://www.youtube.com/watch?v=urRVZ4SW7WU",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.DISNEY_PLUS, AccessType.STREAM, null, "https://www.disneyplus.com"),
                    ProviderAvailability(StreamingProvider.HULU, AccessType.STREAM, null, "https://www.hulu.com"),
                    ProviderAvailability(StreamingProvider.YOUTUBE, AccessType.RENT, "$3.99", "https://youtube.com/movies")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Jaw-dropping cinematography", "Palms-sweaty tension", "Oscar-winning documentary brilliance"),
                    cons = listOf("Not for those afraid of heights"),
                    audienceScore = 93,
                    criticScore = 98,
                    aiConsensus = "An exhilarating look at human potential and extreme focus that will keep you on the edge of your seat."
                ),
                badge = "Oscar Winner"
            ),
            MediaItem(
                id = "m6",
                title = "Spirited Away",
                type = MediaType.ANIME,
                releaseYear = 2001,
                runtimeMinutes = 125,
                posterUrl = "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=600&auto=format&fit=crop",
                genres = listOf("Anime", "Fantasy", "Family", "Adventure"),
                ratingImdb = 8.6,
                ratingRottenTomatoes = 97,
                ratingAiScore = 97,
                overview = "During her family's move to the suburbs, a 10-year-old girl wanders into a world ruled by gods, witches, and spirits where humans are changed into beasts.",
                tagline = "Nothing that happens is ever forgotten, even if you can't remember it.",
                ageRating = "PG",
                director = "Hayao Miyazaki",
                cast = listOf("Rumi Hiiragi", "Miyu Irino", "Mari Natsuki"),
                languages = listOf("Japanese", "English"),
                subtitles = listOf("English", "Spanish", "French", "German"),
                trailerUrl = "https://www.youtube.com/watch?v=ByXuk9QqQkk",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.MAX, AccessType.STREAM, null, "https://www.max.com"),
                    ProviderAvailability(StreamingProvider.NETFLIX, AccessType.STREAM, "Global Ex. US", "https://www.netflix.com"),
                    ProviderAvailability(StreamingProvider.APPLE_TV, AccessType.BUY, "$14.99", "https://tv.apple.com")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Enchanting hand-drawn Studio Ghibli art", "Timeless story of growth & courage", "Joe Hisaishi's haunting score"),
                    cons = listOf("Some bizarre spirit designs for young toddlers"),
                    audienceScore = 96,
                    criticScore = 97,
                    aiConsensus = "Hayao Miyazaki's masterwork that won the Academy Award, capturing the magic and wonder of childhood."
                ),
                badge = "Studio Ghibli"
            ),
            MediaItem(
                id = "m7",
                title = "The King's Affection",
                type = MediaType.K_DRAMA,
                releaseYear = 2021,
                runtimeMinutes = 60,
                posterUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=600&auto=format&fit=crop",
                genres = listOf("Korean Drama", "Romance", "Historical", "Drama"),
                ratingImdb = 8.0,
                ratingRottenTomatoes = 88,
                ratingAiScore = 89,
                overview = "When the Crown Prince is killed, his twin sister assumes the throne while trying to keep her identity and her affection for her royal tutor a secret.",
                tagline = "A royal secret that could change the fate of a kingdom.",
                ageRating = "TV-14",
                director = "Song Hyun-wook",
                cast = listOf("Park Eun-bin", "Rowoon", "Nam Yoon-su"),
                languages = listOf("Korean"),
                subtitles = listOf("English", "Spanish", "French", "Indonesian"),
                trailerUrl = "https://www.youtube.com/watch?v=m2G-q4m4nUI",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.NETFLIX, AccessType.STREAM, null, "https://www.netflix.com/title/81430282")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Stunning historical Joseon costumes", "Sensational performance by Park Eun-bin", "Captivating romantic tension"),
                    cons = listOf("20 episodes require a significant time commitment"),
                    audienceScore = 90,
                    criticScore = 88,
                    aiConsensus = "A gorgeously filmed historical K-Drama with heart, humor, and royal intrigue."
                ),
                badge = "K-Drama Spotlight"
            ),
            MediaItem(
                id = "m8",
                title = "King of Boys",
                type = MediaType.NOLLYWOOD,
                releaseYear = 2018,
                runtimeMinutes = 182,
                posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop",
                genres = listOf("Nollywood / African", "Crime", "Drama", "Thriller"),
                ratingImdb = 8.2,
                ratingRottenTomatoes = 85,
                ratingAiScore = 90,
                overview = "A businesswoman and philanthropist with a checkered past gets sucked into a power struggle that threatens everything she holds dear.",
                tagline = "Power is not given. It is seized.",
                ageRating = "TV-MA",
                director = "Kemi Adetiba",
                cast = listOf("Sola Sobowale", "Adesua Etomi-Wellington", "Reminisce"),
                languages = listOf("English", "Yoruba"),
                subtitles = listOf("English [CC]", "French"),
                trailerUrl = "https://www.youtube.com/watch?v=L2Gj3n-J8s4",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.NETFLIX, AccessType.STREAM, null, "https://www.netflix.com"),
                    ProviderAvailability(StreamingProvider.TUBI, AccessType.FREE_WITH_ADS, null, "https://tubitv.com")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Tour-de-force performance by Sola Sobowale", "Pioneering Nollywood crime drama", "Deep political intrigue"),
                    cons = listOf("Long runtime over 3 hours"),
                    audienceScore = 91,
                    criticScore = 85,
                    aiConsensus = "A powerhouse African crime saga that established a milestone in modern Nollywood cinema."
                ),
                badge = "African Cinema"
            ),
            MediaItem(
                id = "m9",
                title = "Cosmos: Possible Worlds",
                type = MediaType.DOCUMENTARY,
                releaseYear = 2020,
                runtimeMinutes = 45,
                posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop",
                genres = listOf("Documentary", "Sci-Fi", "Science", "Education"),
                ratingImdb = 9.1,
                ratingRottenTomatoes = 95,
                ratingAiScore = 97,
                overview = "Neil deGrasse Tyson takes viewers on a journey through space and time across billions of years of cosmic history.",
                tagline = "The ship of the imagination journeys further than ever before.",
                ageRating = "TV-PG",
                director = "Ann Druyan",
                cast = listOf("Neil deGrasse Tyson", "Seth MacFarlane"),
                languages = listOf("English"),
                subtitles = listOf("English [CC]", "Spanish"),
                trailerUrl = "https://www.youtube.com/watch?v=mXbf4M1G7e8",
                availableProviders = listOf(
                    ProviderAvailability(StreamingProvider.DISNEY_PLUS, AccessType.STREAM, null, "https://www.disneyplus.com"),
                    ProviderAvailability(StreamingProvider.PLUTO_TV, AccessType.FREE_WITH_ADS, null, "https://pluto.tv"),
                    ProviderAvailability(StreamingProvider.TUBI, AccessType.FREE_WITH_ADS, null, "https://tubitv.com")
                ),
                reviewSummary = ReviewSummary(
                    pros = listOf("Visual effects are cosmic and sublime", "Inspirational storytelling by Neil deGrasse Tyson", "Free educational stream on Pluto TV"),
                    cons = listOf("Can feel dense with scientific facts"),
                    audienceScore = 95,
                    criticScore = 95,
                    aiConsensus = "An inspiring celebration of human curiosity and science that expands minds of all ages."
                ),
                badge = "Free To Watch"
            )
        )
    }
}
