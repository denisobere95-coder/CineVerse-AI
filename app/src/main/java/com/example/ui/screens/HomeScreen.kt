package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MediaItem
import com.example.ui.MainViewModel
import com.example.ui.components.MediaCard
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    watchlistIds: Set<String>,
    onSelectMedia: (MediaItem) -> Unit,
    onNavigateToAiAssistant: () -> Unit
) {
    val trending = viewModel.repository.getTrending()
    val freeToWatch = viewModel.repository.getFreeToWatch()
    val anime = viewModel.repository.getAnime()
    val leavingSoon = viewModel.repository.getLeavingSoon()
    val awardWinners = viewModel.repository.getAwardWinners()
    val hiddenGems = viewModel.repository.getHiddenGems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("home_screen")
    ) {
        // Hero Spotlight Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1784668289677),
                    contentDescription = "Spotlight Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkCanvas.copy(alpha = 0.7f),
                                    DarkCanvas
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = GoldPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SPOTLIGHT FEATURE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Interstellar & Sci-Fi Universes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Available on Prime Video • Max • Pluto TV (Free)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { trending.firstOrNull()?.let { onSelectMedia(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Explore Show", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAiAssistant,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, ElectricIndigo)))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask AI", color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Quick AI Prompt Search Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToAiAssistant() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, ElectricIndigo)))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ask CineVerse AI Assistant...",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "e.g. \"Funny action movie under 2 hrs on Tubi or Prime\"",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GoldPrimary)
                }
            }
        }

        // 100% Free Legal Streaming Carousel
        item {
            MediaSectionHeader(
                title = "100% FREE LEGAL STREAMING",
                subtitle = "Tubi, Pluto TV, YouTube Free, Plex",
                badgeText = "FREE"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(freeToWatch) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { viewModel.toggleWatchlist(media) }
                    )
                }
            }
        }

        // Trending Now Carousel
        item {
            MediaSectionHeader(
                title = "TRENDING ACROSS PLATFORMS",
                subtitle = "Netflix, Disney+, Prime, Crunchyroll"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trending) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { viewModel.toggleWatchlist(media) },
                        isLargeWidth = true
                    )
                }
            }
        }

        // Anime & Animation
        item {
            MediaSectionHeader(
                title = "ANIME & ANIMATION",
                subtitle = "Crunchyroll, Netflix, Ghibli"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(anime) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { viewModel.toggleWatchlist(media) }
                    )
                }
            }
        }

        // Leaving Soon & Award Winners
        item {
            MediaSectionHeader(
                title = "AWARD WINNERS & HIDDEN GEMS",
                subtitle = "Oscar Winners & High AI Scores"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(awardWinners + hiddenGems) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { viewModel.toggleWatchlist(media) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MediaSectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = BrandTubi,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
