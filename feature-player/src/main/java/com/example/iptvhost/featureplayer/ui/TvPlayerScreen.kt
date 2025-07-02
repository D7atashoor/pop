package com.example.iptvhost.featureplayer.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView

@Composable
fun TvPlayerScreen(
    streamUrl: String,
    onExit: () -> Unit // maybe back key handled by activity
) {
    val context = LocalContext.current
    val trackSelector = remember { DefaultTrackSelector(context) }

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    var showQuality by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = {
            StyledPlayerView(it).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_ALWAYS)
                // Keep controller visible on TV
                controllerShowTimeoutMs = 0
                setControllerAutoShow(true)
            }
        }, modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = { showQuality = true },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.HighQuality, contentDescription = "Quality", tint = Color.White)
        }
    }

    if (showQuality) {
        QualitySelectorDialog(trackSelector = trackSelector, onDismiss = { showQuality = false })
    }
}