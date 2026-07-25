package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
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
import com.example.data.model.MediaItem
import com.example.ui.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.components.MediaCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    watchlistIds: Set<String>,
    onSelectMedia: (MediaItem) -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val suggestedPrompts = listOf(
        "Funny action movie under 2 hours on Tubi or Prime",
        "Sci-fi space movies like Interstellar",
        "Best romance anime with English sub/dub",
        "Popular documentaries free with ads",
        "Family movies for tonight"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("ai_assistant_screen")
    ) {
        // AI Header
        Surface(
            color = DarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(GoldPrimary, ElectricIndigo)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CineVerse AI Recommendation Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Powered by Gemini 3.5 Flash • Multi-Platform Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldSecondary
                    )
                }
            }
        }

        // Suggested Prompts Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                SuggestionChip(
                    onClick = { viewModel.sendAiPrompt(prompt) },
                    label = { Text(prompt, fontSize = 11.sp, color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = GoldPrimary)
                )
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    watchlistIds = watchlistIds,
                    onSelectMedia = onSelectMedia,
                    onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                )
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = GoldPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("CineVerse AI is reasoning across streaming platforms...", color = GoldSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Input Field Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, ElectricIndigo)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask for recommendations or movies by mood...", fontSize = 13.sp, color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.sendAiPrompt(inputQuery)
                            inputQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(GoldPrimary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    watchlistIds: Set<String>,
    onSelectMedia: (MediaItem) -> Unit,
    onToggleWatchlist: (MediaItem) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) ElectricIndigo else DarkSurfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CineVerse AI", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    color = if (isUser) Color.White else TextPrimary,
                    fontSize = 13.sp
                )
            }
        }

        // Attached Recommended Media Cards
        if (message.recommendedMedia.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(message.recommendedMedia) { media ->
                    MediaCard(
                        media = media,
                        isInWatchlist = watchlistIds.contains(media.id),
                        onCardClick = { onSelectMedia(media) },
                        onWatchlistClick = { onToggleWatchlist(media) }
                    )
                }
            }
        }
    }
}
