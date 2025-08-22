package com.hackathon2025.deviationwizard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hackathon2025.deviationwizard.model.AudioPlayerImpl
import java.io.File

@Composable
fun RecordsList(
    modifier: Modifier = Modifier,
    records: List<File>,
    onRemoveRecord: (File) -> Unit
) {
    val context = LocalContext.current
    val player by lazy { AudioPlayerImpl(context) }
    var currentlyPlayingId  by remember { mutableStateOf("") }

    LazyVerticalStaggeredGrid(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        contentPadding = PaddingValues(16.dp),
        columns = StaggeredGridCells.Fixed(1),
    ) {
        items(records.size) { recordIdx ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val file = records[recordIdx]

                IconButton(
                    onClick = {
                        if (currentlyPlayingId == file.name) {
                            player.stopPlaying()
                            currentlyPlayingId = ""
                        } else {
                            if (currentlyPlayingId.isNotEmpty() && currentlyPlayingId != file.name) {
                                player.stopPlaying()
                            }
                            player.startPlaying(file)
                            currentlyPlayingId = file.name
                        }
                    },
                ) {
                    AnimatedContent(
                        targetState = currentlyPlayingId == file.name,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                        },
                        label = "playStopIcon"
                    ) { playing ->
                        Icon(
                            modifier = Modifier.size(48.dp),
                            imageVector = if (playing) {
                                Icons.Default.StopCircle
                            } else {
                                Icons.Default.PlayCircleOutline
                            },
                            contentDescription = if (playing) "Stop" else "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(text = file.name, modifier = Modifier.weight(1f))
                OutlinedButton(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(5.dp),
                    onClick = {
                        onRemoveRecord(file)
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    ),
                    shape = CircleShape,
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}