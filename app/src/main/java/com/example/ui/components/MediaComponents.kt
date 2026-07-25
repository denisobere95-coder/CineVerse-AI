package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.StreamingProvider
import com.example.ui.theme.*

@Composable
fun PlatformBadge(
    provider: StreamingProvider,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    val (bgColor, textColor) = when (provider) {
        StreamingProvider.NETFLIX -> BrandNetflix to Color.White
        StreamingProvider.PRIME_VIDEO -> BrandPrime to Color.White
        StreamingProvider.DISNEY_PLUS -> BrandDisney to Color.White
        StreamingProvider.CRUNCHYROLL -> BrandCrunchyroll to Color.White
        StreamingProvider.TUBI -> BrandTubi to Color.White
        StreamingProvider.HULU -> BrandHulu to Color.Black
        StreamingProvider.MAX -> BrandMax to Color.White
        StreamingProvider.APPLE_TV -> BrandApple to Color.Black
        else -> MaterialTheme.colorScheme.surfaceVariant to Color.White
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = provider.displayName,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.Yellow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            } else if (provider.isFree) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "FREE",
                    color = Color.Yellow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun DetailedPlatformAvailabilityBadge(
    provider: StreamingProvider,
    accessTypeLabel: String,
    price: String?,
    quality: String = "4K UHD",
    audio: String = "5.1 Surround",
    isExclusive: Boolean = false,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (provider) {
        StreamingProvider.NETFLIX -> BrandNetflix to Color.White
        StreamingProvider.PRIME_VIDEO -> BrandPrime to Color.White
        StreamingProvider.DISNEY_PLUS -> BrandDisney to Color.White
        StreamingProvider.CRUNCHYROLL -> BrandCrunchyroll to Color.White
        StreamingProvider.TUBI -> BrandTubi to Color.White
        StreamingProvider.HULU -> BrandHulu to Color.Black
        StreamingProvider.MAX -> BrandMax to Color.White
        StreamingProvider.APPLE_TV -> BrandApple to Color.Black
        else -> MaterialTheme.colorScheme.surfaceVariant to Color.White
    }

    Surface(
        modifier = modifier.testTag("platform_availability_badge_${provider.id}"),
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(bgColor.copy(alpha = 0.8f), DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Platform Brand Pill
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = provider.displayName,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = accessTypeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (isExclusive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "EXCLUSIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (price != null) {
                        Text(
                            text = price,
                            fontSize = 11.sp,
                            color = GoldSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Specs Badges (4K UHD, Dolby Atmos)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = ElectricIndigo.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = quality,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = DeepViolet,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = audio,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaCard(
    media: MediaItem,
    isInWatchlist: Boolean,
    onCardClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLargeWidth: Boolean = false
) {
    val cardWidth = if (isLargeWidth) 180.dp else 140.dp
    val posterHeight = if (isLargeWidth) 260.dp else 200.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .testTag("media_card_${media.id}")
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(posterHeight)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top AI Score & Free Tag Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Score Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(0.5.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Score",
                                tint = GoldPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${media.ratingAiScore}%",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Watchlist Toggle
                    IconButton(
                        onClick = onWatchlistClick,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isInWatchlist) GoldPrimary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Primary Provider Tag at bottom of poster
                media.availableProviders.firstOrNull()?.let { avail ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        PlatformBadge(provider = avail.provider)
                    }
                }
            }

            // Card Text Details
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${media.releaseYear} • ${media.type.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "IMDb",
                            tint = GoldSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${media.ratingImdb}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
