package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MediaDetailSheet
import com.example.ui.components.TrailerPlayerDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CineVerseTheme {
                CineVerseApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineVerseApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val watchlist by viewModel.watchlist.collectAsState()
    val watchlistIds = remember(watchlist) { watchlist.map { it.mediaId }.toSet() }

    val selectedMedia by viewModel.selectedMedia.collectAsState()
    val playingTrailerUrl by viewModel.playingTrailerUrl.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldPrimary,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CineVerse AI",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Legal Hub",
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("top_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Universal Search") },
                    label = { Text("Search", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Engine") },
                    label = { Text("AI Assistant", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watchlist") },
                    label = { Text("Watchlist", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Hub & Stats") },
                    label = { Text("Hub", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        containerColor = DarkCanvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    watchlistIds = watchlistIds,
                    onSelectMedia = { viewModel.selectMedia(it) },
                    onNavigateToAiAssistant = { selectedTab = 2 }
                )
                1 -> SearchScreen(
                    viewModel = viewModel,
                    watchlistIds = watchlistIds,
                    onSelectMedia = { viewModel.selectMedia(it) }
                )
                2 -> AiAssistantScreen(
                    viewModel = viewModel,
                    watchlistIds = watchlistIds,
                    onSelectMedia = { viewModel.selectMedia(it) }
                )
                3 -> WatchlistScreen(
                    viewModel = viewModel,
                    onSelectMedia = { viewModel.selectMedia(it) }
                )
                4 -> HubScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Media Detail Sheet Modal
    selectedMedia?.let { media ->
        MediaDetailSheet(
            media = media,
            isInWatchlist = watchlistIds.contains(media.id),
            onDismiss = { viewModel.selectMedia(null) },
            onToggleWatchlist = { customList ->
                viewModel.toggleWatchlist(media, customListName = customList)
            },
            onPlayTrailer = { trailerUrl ->
                viewModel.playTrailer(trailerUrl)
            }
        )
    }

    // Trailer Player Dialog
    playingTrailerUrl?.let { trailerUrl ->
        TrailerPlayerDialog(
            trailerUrl = trailerUrl,
            onDismiss = { viewModel.playTrailer(null) }
        )
    }
}
