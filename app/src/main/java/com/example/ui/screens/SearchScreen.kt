package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.MediaItem
import com.example.data.model.StreamingProvider
import com.example.ui.MainViewModel
import com.example.ui.components.MediaCard
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    watchlistIds: Set<String>,
    onSelectMedia: (MediaItem) -> Unit
) {
    val filterState by viewModel.filterState.collectAsState()
    val filteredMedia by viewModel.filteredMedia.collectAsState()

    val genres = listOf("All", "Sci-Fi", "Anime", "Action", "Horror", "Documentary", "Korean Drama", "Nollywood / African", "Fantasy", "Family")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(top = 16.dp)
            .testTag("search_screen")
    ) {
        // Universal Search Input
        OutlinedTextField(
            value = filterState.query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search title, actor, director, genre...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = {
                if (filterState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
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
                .padding(horizontal = 16.dp)
                .testTag("universal_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Free Only Toggle & Media Type Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Free To Watch Only",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (filterState.freeOnly) BrandTubi else TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = filterState.freeOnly,
                    onCheckedChange = { viewModel.toggleFreeOnlyFilter(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandTubi
                    )
                )
            }

            if (filterState.query.isNotEmpty() || filterState.mediaType != null || filterState.freeOnly || filterState.selectedProvider != null || filterState.selectedGenre != null) {
                TextButton(onClick = { viewModel.clearFilters() }) {
                    Text("Clear All", color = GoldPrimary, fontSize = 12.sp)
                }
            }
        }

        // Media Type Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = filterState.mediaType == null,
                    onClick = { viewModel.setMediaTypeFilter(null) },
                    label = { Text("All Types") }
                )
            }
            items(MediaType.values()) { type ->
                FilterChip(
                    selected = filterState.mediaType == type,
                    onClick = { viewModel.setMediaTypeFilter(if (filterState.mediaType == type) null else type) },
                    label = { Text(type.displayName) }
                )
            }
        }

        // Streaming Providers Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(StreamingProvider.values()) { provider ->
                val isSelected = filterState.selectedProvider == provider
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setProviderFilter(if (isSelected) null else provider) },
                    label = { Text(provider.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Genre Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(genres) { genre ->
                val isSelected = if (genre == "All") filterState.selectedGenre == null else filterState.selectedGenre == genre
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setGenreFilter(if (genre == "All" || isSelected) null else genre) },
                    label = { Text(genre) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results Summary Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredMedia.size} TITLES FOUND",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        // Search Results Grid
        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No title matches your selected filters", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearFilters() }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                        Text("Reset Search Filters", color = Color.Black)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { viewModel.toggleWatchlist(media) }
                    )
                }
            }
        }
    }
}
