package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextPrimary

@Composable
fun TrailerPlayerDialog(
    trailerUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Convert standard YouTube watch link to embed link
    val embedUrl = if (trailerUrl.contains("youtube.com/watch?v=")) {
        val videoId = trailerUrl.substringAfter("watch?v=").substringBefore("&")
        "https://www.youtube.com/embed/$videoId?autoplay=1"
    } else {
        trailerUrl
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .testTag("trailer_player_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = DarkCanvas
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OFFICIAL TRAILER",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    Row {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open in YouTube", tint = TextPrimary)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                loadUrl(embedUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
