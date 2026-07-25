package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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

import com.example.ui.components.RoomDatabaseSearchableMovieList

@Composable
fun WatchlistScreen(
    viewModel: MainViewModel,
    onSelectMedia: (MediaItem) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val customListNames by viewModel.customLists.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: DB Search & Filter, 1: Want to Watch, 2: Watched, 3: Custom Lists

    val tabTitles = listOf("Room DB Search", "Want to Watch", "Watched", "Custom Lists")

    val filteredList = when (selectedTab) {
        1 -> watchlist.filter { it.watchStatus == "WANT_TO_WATCH" }
        2 -> watchlist.filter { it.watchStatus == "WATCHED" }
        else -> watchlist
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(top = 16.dp)
            .testTag("watchlist_screen")
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "GLOBAL WATCHLIST & ROOM DB",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Persistent Room SQLite database with genre & platform filtering",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = GoldPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // Room Database Searchable Movie List Component
            RoomDatabaseSearchableMovieList(
                viewModel = viewModel,
                onSelectMedia = onSelectMedia
            )
        } else {
            // Custom Lists Filter Row if tab 3
            if (selectedTab == 3 && customListNames.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(customListNames) { listName ->
                        AssistChip(
                            onClick = {},
                            label = { Text(listName, color = GoldPrimary) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurface)
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 1) "Your Watchlist is empty!" else "No watched titles recorded yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Browse movies or ask CineVerse AI to save titles across any platform.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
                        WatchlistItemRow(
                            item = item,
                            onItemClick = {
                                val media = viewModel.repository.getAllMedia().find { it.id == item.mediaId }
                                if (media != null) onSelectMedia(media)
                            },
                            onMarkWatched = {
                                val newStatus = if (item.watchStatus == "WATCHED") "WANT_TO_WATCH" else "WATCHED"
                                viewModel.updateWatchStatus(item.mediaId, newStatus)
                            },
                            onDelete = {
                                val media = viewModel.repository.getAllMedia().find { it.id == item.mediaId }
                                if (media != null) viewModel.toggleWatchlist(media)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistItemRow(
    item: WatchlistItemEntity,
    onItemClick: () -> Unit,
    onMarkWatched: () -> Unit,
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
                    .size(width = 60.dp, height = 85.dp)
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
                    Surface(
                        color = DeepViolet,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.mediaType,
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${item.ratingImdb}", fontSize = 11.sp, color = TextPrimary)
                }
            }

            IconButton(onClick = onMarkWatched) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Mark Watched",
                    tint = if (item.watchStatus == "WATCHED") BrandHulu else TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = TextSecondary
                )
            }
        }
    }
}
