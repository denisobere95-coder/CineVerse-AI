package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserBadge
import com.example.data.model.WatchPartyRoom
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun HubScreen(viewModel: MainViewModel) {
    val watchedCount by viewModel.watchedCount.collectAsState()
    val badges = viewModel.repository.badges
    val watchPartyRooms = viewModel.repository.watchPartyRooms
    val activeRoom by viewModel.activeWatchParty.collectAsState()

    var selectedCountry by remember { mutableStateOf("Global / United States") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp)
            .testTag("hub_screen")
    ) {
        // Header
        item {
            Text(
                text = "ENTERTAINMENT HUB & ANALYTICS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "AI Stats, Gamification, Watch Together, and Regional Streaming",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // AI Analytics Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, ElectricIndigo)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI VIEWING ANALYTICS",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnalyticsStatBox(number = "${watchedCount + 14}", label = "Titles Watched")
                        AnalyticsStatBox(number = "${(watchedCount + 14) * 2.2.toInt()}h", label = "Hours Streamed")
                        AnalyticsStatBox(number = "4", label = "Free Platforms Used")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("TOP GENRES THIS MONTH", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(progress = 0.45f, color = GoldPrimary, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sci-Fi & Anime (45%)", fontSize = 11.sp, color = TextPrimary)
                        Text("Action & Horror (30%)", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }

        // Live Watch Together Parties
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = BrandHulu)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WATCH TOGETHER (LIVE SYNCHRONIZED ROOMS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text("Chat and synchronize playback with friends on free/licensed services", fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(8.dp))

            watchPartyRooms.forEach { room ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(room.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Media: ${room.mediaTitle} • Host: ${room.hostName}", fontSize = 11.sp, color = TextSecondary)
                            Text("Platform: ${room.providerName} • ${room.participantsCount} watching", fontSize = 11.sp, color = GoldSecondary)
                        }

                        Button(
                            onClick = { viewModel.joinWatchParty(room) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (activeRoom?.id == room.id) BrandHulu else ElectricIndigo)
                        ) {
                            Text(if (activeRoom?.id == room.id) "Joined" else "Join Party")
                        }
                    }
                }
            }
        }

        // Gamification & Badges
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GAMIFICATION & UNLOCKED BADGES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.forEach { badge ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (badge.isUnlocked) DeepViolet else DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (badge.iconName) {
                                    "movie" -> Icons.Default.Movie
                                    "tv" -> Icons.Default.Tv
                                    "rocket_launch" -> Icons.Default.RocketLaunch
                                    "auto_awesome" -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.Star
                                },
                                contentDescription = null,
                                tint = if (badge.isUnlocked) GoldPrimary else TextMuted,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.isUnlocked) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            if (badge.isUnlocked) {
                                Surface(
                                    color = GoldPrimary,
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Unlocked", tint = Color.Black, modifier = Modifier.padding(4.dp).size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Country / Regional Streaming Availability
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("REGIONAL STREAMING COUNTRY", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Select your region to adjust platform availability & local TV streams", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            selectedCountry = if (selectedCountry.contains("Global")) "Nigeria & West Africa" else "Global / United States"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Region: $selectedCountry", color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AnalyticsStatBox(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}
