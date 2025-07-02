package com.example.iptvhost.featureplayer.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun MobilePlayerScreen(
    streamUrl: String,
    title: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
    }

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                val mediaItem = MediaItem.fromUri(streamUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    var showQualitySelector by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            StyledPlayerView(it).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_ALWAYS)
                controllerShowTimeoutMs = 3_000
                setControllerAutoShow(true)
            }
        }, modifier = Modifier.fillMaxSize())

        // Top bar overlay
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { showQualitySelector = true }) {
                Icon(Icons.Default.HighQuality, contentDescription = "Quality", tint = Color.White)
            }
        }

        title?.let {
            Text(
                text = it,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    if (showQualitySelector) {
        QualitySelectorDialog(trackSelector = trackSelector, onDismiss = { showQualitySelector = false })
    }
}