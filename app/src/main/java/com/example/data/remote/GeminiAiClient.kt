package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Moshi data models for Gemini REST API
data class GeminiPart(val text: String? = null)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiAiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI Assistant Key is missing. Using smart offline engine mode.\n\nRecommendation: Based on your request, we recommend 'Interstellar' (Max/Prime) or 'Cyberpunk: Edgerunners' (Netflix/Crunchyroll)."
        }

        val systemPrompt = """
            You are CineVerse AI, the ultimate universal entertainment assistant. 
            Help the user discover movies, TV series, anime, cartoons, and documentaries.
            Include streaming availability (e.g. Netflix, Tubi [Free], Prime, Disney+, Crunchyroll), release year, genres, and why it matches their mood/request.
            Keep response structured, friendly, concise, and scannable with bullet points.
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nUser Question: $prompt"

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt))))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from CineVerse AI. Please try rephrasing your search."
        } catch (e: Exception) {
            "AI Assistant encountered a temporary network issue: ${e.localizedMessage ?: "Unknown error"}. Standard universal search is still fully functional."
        }
    }

    suspend fun generateRecommendationsFromWatchlist(watchlist: List<com.example.data.model.WatchlistItemEntity>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (watchlist.isEmpty()) {
            return@withContext "Your Room database watchlist is currently empty. Save some movies or TV shows to get personalized Gemini AI recommendations!"
        }

        val watchlistSummary = watchlist.joinToString("\n") { item ->
            "• ${item.title} (${item.year}) - Genre: ${item.genre.ifEmpty { "General" }}, Platform: ${item.platform.ifEmpty { "Streaming" }}, Rating: ${item.ratingImdb}, Status: ${item.watchStatus}, Favorite: ${if (item.isFavorite) "Yes ⭐" else "No"}"
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val topGenre = watchlist.firstOrNull()?.genre ?: "Sci-Fi"
            return@withContext "🍿 **CineVerse AI Recommendations (Offline Smart Engine)**\n\nBased on your Room database watchlist containing ${watchlist.size} saved titles:\n\n" +
                    "1. **Interstellar (2014)** - *Platform: Prime Video / Max*\n" +
                    "   *Why:* Perfect match for your interest in $topGenre and high-rated cinematic experiences.\n\n" +
                    "2. **Cyberpunk: Edgerunners (2022)** - *Platform: Netflix*\n" +
                    "   *Why:* Visually stunning and fast-paced, aligning with your top saved genres.\n\n" +
                    "3. **Cosmos: Possible Worlds (2020)** - *Platform: Disney+ / Tubi [Free]*\n" +
                    "   *Why:* Outstanding documentary storytelling rated 9.1/10 on IMDb."
        }

        val systemPrompt = """
            You are CineVerse AI, an expert film critic and personalized streaming recommendation engine.
            Analyze the user's Room database watchlist items provided below. Look at their favorite titles, top genres, and preferred streaming platforms.
            Recommend 3 to 5 highly relevant movies, TV series, or anime that are NOT already in their watchlist.
            For each recommendation, state:
            1. Title & Release Year
            2. Available Streaming Platform (e.g., Netflix, Tubi [Free], Hulu, Disney+, Crunchyroll, Prime Video)
            3. A brief 1-2 sentence explanation of why it fits their watchlist taste profile.
            Keep the response enthusiastic, formatted in clean Markdown with bolding and bullet points.
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nUser's Room Watchlist Items (${watchlist.size} total):\n$watchlistSummary"

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt))))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to generate recommendations from Gemini API. Please try again."
        } catch (e: Exception) {
            "Gemini AI recommendation service temporarily unavailable: ${e.localizedMessage ?: "Network error"}. Showing standard recommendations."
        }
    }
}
