package com.hackathon2025.deviationwizard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hackathon2025.deviationwizard.model.AudioPlayerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun RemoteRecordsList(
    modifier: Modifier = Modifier,
    recordIds: List<String>,
    baseUrl: String
) {
    val context = LocalContext.current
    val player = remember { AudioPlayerImpl(context) }
    var currentlyPlayingId by remember { mutableStateOf("") }

    // Set up playback completion listener
    LaunchedEffect(Unit) {
        player.setOnPlaybackCompleteListener {
            currentlyPlayingId = ""
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        recordIds.forEach { recordId ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var tempFile by remember { mutableStateOf<File?>(null) }
                
                // Download the file when the component is first created
                LaunchedEffect(recordId) {
                    withContext(Dispatchers.IO) {
                        try {
                            val recordingUrl = "$baseUrl/api/records/$recordId"
                            val file = File(context.cacheDir, "temp_$recordId.mp3")
                            URL(recordingUrl).openStream().use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            tempFile = file
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                IconButton(
                    onClick = {
                        if (currentlyPlayingId == recordId) {
                            player.stopPlaying()
                            currentlyPlayingId = ""
                        } else {
                            // Stop any currently playing audio
                            if (currentlyPlayingId.isNotEmpty() && currentlyPlayingId != recordId) {
                                player.stopPlaying()
                            }
                            tempFile?.let { file ->
                                player.startPlaying(file)
                                currentlyPlayingId = recordId
                            }
                        }
                    },
                    enabled = tempFile != null
                ) {
                    AnimatedContent(
                        targetState = currentlyPlayingId == recordId,
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
                Text(text = "Recording $recordId", modifier = Modifier.weight(1f))
            }
        }
    }
} 