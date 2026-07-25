package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailSheet(
    media: MediaItem,
    isInWatchlist: Boolean,
    onDismiss: () -> Unit,
    onToggleWatchlist: (customList: String?) -> Unit,
    onPlayTrailer: (url: String) -> Unit
) {
    val context = LocalContext.current
    var showCustomListDialog by remember { mutableStateOf(false) }
    var selectedCustomListInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCanvas,
        contentColor = TextPrimary,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier.testTag("media_detail_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Backdrop Header + Poster Overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(media.backdropUrl ?: media.posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.title,
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
                                        DarkCanvas.copy(alpha = 0.8f),
                                        DarkCanvas
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(90.dp)
                                .height(130.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            AsyncImage(
                                model = media.posterUrl,
                                contentDescription = media.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Surface(
                                color = DeepViolet,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = media.type.displayName,
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "${media.releaseYear} • ${media.runtimeMinutes} min • ${media.ageRating}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Platform Badges Row
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(media.availableProviders) { avail ->
                                    PlatformBadge(
                                        provider = avail.provider,
                                        badgeText = if (avail.isExclusive) "EXCLUSIVE" else null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions (Watchlist + Trailer)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onToggleWatchlist(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInWatchlist) GoldPrimary else DarkSurfaceVariant,
                            contentColor = if (isInWatchlist) Color.Black else TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isInWatchlist) "In Watchlist" else "+ Add Watchlist")
                    }

                    OutlinedButton(
                        onClick = { onPlayTrailer(media.trailerUrl) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldSecondary)))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Trailer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trailer")
                    }
                }
            }

            // Ratings Row (IMDb, Rotten Tomatoes, AI Score)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("IMDb", fontSize = 11.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${media.ratingImdb}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = DarkBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rotten Tomatoes", fontSize = 11.sp, color = TextSecondary)
                            Text("${media.ratingRottenTomatoes}%", fontWeight = FontWeight.Bold, color = BrandNetflix)
                        }

                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = DarkBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AI Match Score", fontSize = 11.sp, color = GoldPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${media.ratingAiScore}%", fontWeight = FontWeight.Bold, color = GoldPrimary)
                            }
                        }
                    }
                }
            }

            // Where To Watch (Universal Legal Platform Badges & Deep-Links)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "WHERE TO WATCH (PLATFORM AVAILABILITY)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    media.availableProviders.forEach { avail ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            DetailedPlatformAvailabilityBadge(
                                provider = avail.provider,
                                accessTypeLabel = avail.accessType.label,
                                price = avail.price,
                                quality = avail.quality,
                                audio = avail.audio,
                                isExclusive = avail.isExclusive,
                                badgeText = avail.badgeText
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(avail.deepLinkUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(ElectricIndigo, DeepViolet))),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("Watch on ${avail.provider.displayName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Overview & Genres
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (media.tagline.isNotBlank()) {
                        Text(
                            text = "\"${media.tagline}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = media.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(media.genres) { genre ->
                            AssistChip(
                                onClick = {},
                                label = { Text(genre, fontSize = 12.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurface)
                            )
                        }
                    }
                }
            }

            // AI Review Consensus
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepViolet.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary, ElectricIndigo)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI REVIEW CONSENSUS", fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = media.reviewSummary.aiConsensus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PROS", fontWeight = FontWeight.Bold, color = BrandHulu, fontSize = 12.sp)
                                media.reviewSummary.pros.forEach { pro ->
                                    Text("• $pro", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("CONS", fontWeight = FontWeight.Bold, color = BrandNetflix, fontSize = 12.sp)
                                media.reviewSummary.cons.forEach { con ->
                                    Text("• $con", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Director & Cast
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Director: ${media.director}", fontSize = 13.sp, color = TextSecondary)
                    Text("Cast: ${media.cast.joinToString(", ")}", fontSize = 13.sp, color = TextSecondary)
                    Text("Subtitles: ${media.subtitles.joinToString(", ")}", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}
