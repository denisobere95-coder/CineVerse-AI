package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.WatchlistItemEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun RoomDatabaseSearchableMovieList(
    viewModel: MainViewModel,
    onSelectMedia: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.dbSearchQuery.collectAsState()
    val selectedPlatform by viewModel.dbSelectedPlatform.collectAsState()
    val selectedGenre by viewModel.dbSelectedGenre.collectAsState()
    val favoritesOnly by viewModel.dbFavoritesOnly.collectAsState()
    val roomItems by viewModel.roomSearchResults.collectAsState()
    val totalWatchlist by viewModel.watchlist.collectAsState()

    val isGeneratingRecs by viewModel.isGeneratingWatchlistRecs.collectAsState()
    val watchlistRecsResult by viewModel.watchlistRecsResult.collectAsState()
    var showRecsCard by remember { mutableStateOf(false) }

    val availablePlatforms = listOf("Netflix", "Tubi", "Disney+", "Hulu", "Prime Video", "Crunchyroll", "HBO Max", "Apple TV+")
    val availableGenres = listOf("Sci-Fi", "Anime", "Action", "Drama", "Horror", "Comedy", "Nollywood", "Fantasy")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("room_database_movie_list_component")
    ) {
        // Room DB Status Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrandHulu)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ROOM DATABASE INDEX",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "${totalWatchlist.size} saved titles persistent in local SQLite",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = DeepViolet,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${roomItems.size} MATCHES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini AI Recommendations Button
                Button(
                    onClick = {
                        showRecsCard = true
                        viewModel.generateWatchlistRecommendations()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_generate_watchlist_recs_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGeneratingRecs) "Gemini AI Analyzing Watchlist..." else "Get Gemini AI Recommendations for My Watchlist",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Gemini Watchlist Recommendations Result Card
        if (showRecsCard && (isGeneratingRecs || watchlistRecsResult != null)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(GoldPrimary, ElectricIndigo)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini AI Recommendations",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = { showRecsCard = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isGeneratingRecs) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GoldPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Analyzing your Room database items with Gemini 3.5 Flash...", color = TextSecondary, fontSize = 12.sp)
                        }
                    } else if (watchlistRecsResult != null) {
                        Text(
                            text = watchlistRecsResult!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Realtime Title Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateDbSearchQuery(it) },
            placeholder = { Text("Search database titles...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateDbSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextPrimary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("room_search_input_field")
        )

        // Favorite Switch & Clear Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Favorites Only",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (favoritesOnly) GoldPrimary else TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = favoritesOnly,
                    onCheckedChange = { viewModel.toggleDbFavoritesOnly(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = GoldPrimary
                    ),
                    modifier = Modifier.testTag("favorites_only_switch")
                )
            }

            if (searchQuery.isNotEmpty() || selectedPlatform.isNotEmpty() || selectedGenre.isNotEmpty() || favoritesOnly) {
                TextButton(onClick = { viewModel.clearDbFilters() }) {
                    Text("Clear Filters", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Platform Filter Chips
        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            Text(
                text = "STREAMING PLATFORM",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPlatform.isEmpty(),
                        onClick = { viewModel.setDbPlatformFilter("") },
                        label = { Text("All Platforms") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(availablePlatforms) { platform ->
                    val isSelected = selectedPlatform.equals(platform, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDbPlatformFilter(platform) },
                        label = { Text(platform) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Genre Filter Chips
        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            Text(
                text = "GENRE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGenre.isEmpty(),
                        onClick = { viewModel.setDbGenreFilter("") },
                        label = { Text("All Genres") }
                    )
                }
                items(availableGenres) { genre ->
                    val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDbGenreFilter(genre) },
                        label = { Text(genre) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Room Database Items List / Grid
        if (roomItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Room Database records match filter",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting query, platform, or genre filters above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.clearDbFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Reset All Filters", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = roomItems,
                    key = { it.mediaId }
                ) { item ->
                    RoomMovieListItemCard(
                        item = item,
                        onItemClick = {
                            val media = viewModel.repository.getAllMedia().find { m -> m.id == item.mediaId }
                            if (media != null) onSelectMedia(media)
                        },
                        onToggleFavorite = {
                            viewModel.toggleRoomItemFavorite(item)
                        },
                        onToggleWatched = {
                            val newStatus = if (item.watchStatus == "WATCHED") "WANT_TO_WATCH" else "WATCHED"
                            viewModel.updateWatchStatus(item.mediaId, newStatus)
                        },
                        onDelete = {
                            viewModel.deleteRoomItem(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomMovieListItemCard(
    item: WatchlistItemEntity,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatched: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 65.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.platform.isNotEmpty()) {
                        Surface(
                            color = when (item.platform.lowercase()) {
                                "netflix" -> BrandNetflix
                                "tubi" -> BrandTubi
                                "hulu" -> BrandHulu
                                "disney+" -> BrandDisney
                                "prime video" -> BrandPrime
                                else -> DeepViolet
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.platform,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (item.genre.isNotEmpty()) {
                        Surface(
                            color = DarkBorder,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.genre,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (item.year > 0) {
                        Text(
                            text = "${item.year}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.ratingImdb}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = if (item.watchStatus == "WATCHED") BrandHulu.copy(alpha = 0.2f) else DarkBorder,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = if (item.watchStatus == "WATCHED") BrandHulu else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                    ) {
                        Text(
                            text = if (item.watchStatus == "WATCHED") "WATCHED" else "WANT TO WATCH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.watchStatus == "WATCHED") BrandHulu else TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Action Buttons: Favorite, Watched Check, Delete
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (item.isFavorite) GoldPrimary else TextSecondary
                    )
                }

                IconButton(
                    onClick = onToggleWatched,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Toggle Watched",
                        tint = if (item.watchStatus == "WATCHED") BrandHulu else TextSecondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
