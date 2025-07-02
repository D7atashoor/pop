package com.example.iptvhost.featureplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.TrackGroupArray
import com.google.android.exoplayer2.C

@Composable
fun QualitySelectorDialog(
    trackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit
) {
    val mapped = trackSelector.currentMappedTrackInfo
    if (mapped == null) {
        onDismiss(); return
    }
    val videoRendererIndex = (0 until mapped.rendererCount).firstOrNull {
        mapped.getRendererType(it) == C.TRACK_TYPE_VIDEO
    } ?: run {
        onDismiss(); return
    }

    val groupArray: TrackGroupArray = mapped.getTrackGroups(videoRendererIndex)
    if (groupArray.length == 0) {
        onDismiss(); return
    }

    val qualities = remember { mutableStateOf(createQualityList(groupArray)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Select Quality") },
        text = {
            Column {
                qualities.value.forEach { quality ->
                    Text(
                        quality.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                applyTrackSelection(trackSelector, videoRendererIndex, quality.groupIndex, quality.trackIndex)
                                onDismiss()
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "Auto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Clear overrides to auto
                            trackSelector.parameters = trackSelector.parameters
                                .buildUpon()
                                .clearSelectionOverrides(videoRendererIndex)
                                .setForceHighestSupportedBitrate(false)
                                .build()
                            onDismiss()
                        }
                        .padding(8.dp)
                )
            }
        }
    )
}

data class QualityItem(val label: String, val groupIndex: Int, val trackIndex: Int)

private fun createQualityList(groups: TrackGroupArray): List<QualityItem> {
    val list = mutableListOf<QualityItem>()
    for (g in 0 until groups.length) {
        val group = groups.get(g)
        for (t in 0 until group.length) {
            val format = group.getFormat(t)
            val height = format.height
            val width = format.width
            val bitrate = format.bitrate / 1000
            val label = if (height > 0 && width > 0) {
                "${height}p"
            } else {
                "${bitrate}kbps"
            }
            list.add(QualityItem(label, g, t))
        }
    }
    // sort descending by resolution
    return list.distinctBy { it.label }.sortedByDescending { it.label }
}

private fun applyTrackSelection(
    trackSelector: DefaultTrackSelector,
    rendererIndex: Int,
    groupIndex: Int,
    trackIndex: Int
) {
    val override = SelectionOverride(groupIndex, trackIndex)
    val parameters = trackSelector.buildUponParameters()
        .setSelectionOverride(rendererIndex, trackSelector.currentMappedTrackInfo!!.getTrackGroups(rendererIndex), override)
        .build()
    trackSelector.parameters = parameters
}