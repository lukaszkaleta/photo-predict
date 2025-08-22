package com.hackathon2025.deviationwizard

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hackathon2025.deviationwizard.api.ApiConfig
import com.hackathon2025.deviationwizard.api.Deviation
import com.hackathon2025.deviationwizard.api.DeviationAnalysis
import com.hackathon2025.deviationwizard.components.RemoteRecordsList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
private fun FullscreenImageView(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        
        // Calculate maximum allowed offset based on current scale
        val maxOffset = if (scale >= 1f) {
            (scale - 1) * 1000f
        } else {
            0f
        }
        
        offsetX = (offsetX + offsetChange.x).coerceIn(-maxOffset, maxOffset)
        offsetY = (offsetY + offsetChange.y).coerceIn(-maxOffset, maxOffset)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Fullscreen image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .transformable(state = state),
                contentScale = ContentScale.Fit,
                error = rememberVectorPainter(Icons.Default.BrokenImage)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    deviation: Deviation?,
    analysis: DeviationAnalysis?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit
) {
    var selectedImageUrl by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Details") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
                deviation != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Timestamp
                        Text(
                            text = "Timestamp",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = try {
                                LocalDateTime.parse(deviation.timestamp)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            } catch (e: DateTimeParseException) {
                                Log.e("DetailsScreen", "Error parsing timestamp: ${e.message}")
                                deviation.timestamp
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Comment
                        Text(
                            text = "Comment",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = deviation.comment,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Images
                        ImagesSection(deviation = deviation, onSelectImage = { selectedImageUrl = it })

                        // Recordings
                        RecordingsSection(deviation)

                        // Analysis
                        analysis?.let { analysis ->
                            Text(
                                text = "Analysis",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Solution
                            analysis.solution?.let { solution ->
                                ExpandableSection(
                                    title = "Solution Details",
                                    initiallyExpanded = true
                                ) {
                                    Column {
                                        Text(
                                            text = "Issue Type",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = solution.issueType,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Summary",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = solution.summary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Priority Level",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = solution.priorityLevel,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Repair Effort",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${solution.repairEffortHours} hours",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // Checklist
                                if (solution.checkList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ExpandableSection(
                                        title = "Checklist",
                                        initiallyExpanded = false
                                    ) {
                                        Column {
                                            solution.checkList.forEachIndexed { index, item ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${index + 1}.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Text(
                                                        text = item,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Transcriptions
                            if (analysis.transcriptions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ExpandableSection(
                                    title = "Transcriptions",
                                    initiallyExpanded = false
                                ) {
                                    Column {
                                        analysis.transcriptions.forEach { (key, value) ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = key,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                    Text(
                                                        text = value,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

                            // Image Analysis
                            if (analysis.images.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ExpandableSection(
                                    title = "Image Analysis",
                                    initiallyExpanded = false
                                ) {
                                    Column {
                                        analysis.images.forEach { (key, value) ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = key,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                    Text(
                                                        text = value,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen image view
            selectedImageUrl?.let { url ->
                FullscreenImageView(
                    imageUrl = url,
                    onDismiss = { selectedImageUrl = null }
                )
            }
        }
    }
}

@Composable
private fun RecordingsSection(deviation: Deviation) {
    if (deviation.recordings.isNotEmpty()) {
        Text(
            text = "Recordings",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        RemoteRecordsList(
            recordIds = deviation.recordings,
            baseUrl = ApiConfig.baseUrl
        )
    }
}

@Composable
private fun ImagesSection(
    deviation: Deviation,
    onSelectImage: (String) -> Unit? = {}
) {
    if (deviation.images.isNotEmpty()) {
        Text(
            text = "Images",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            deviation.images.forEach { imageId ->
                val imageUrl = "${ApiConfig.baseUrl}/api/photos/$imageId"
                Log.d("DetailsScreen", "Loading image from URL: $imageUrl")
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Issue image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectImage(imageUrl) },
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                    onError = { error ->
                        Log.e(
                            "DetailsScreen",
                            "Error loading image: ${error.result.throwable}"
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}