package com.hackathon2025.deviationwizard

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.hackathon2025.deviationwizard.components.DeviationForm
import com.hackathon2025.deviationwizard.components.GalleryView
import com.hackathon2025.deviationwizard.components.RecordsList
import java.io.File

@Composable
fun MainView(
    onCameraOpen: () -> Unit = {},
    onRecordTaken: (File) -> Unit = {},
    onGotoList: () -> Unit = {},
    onGotoConfig: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onCancel: () -> Unit = {},
    onRemoveBitmap: (Bitmap) -> Unit = {},
    onRemoveRecord: (File) -> Unit = {},
    onCommentChanged: (String) -> Unit = {},
    onImageSelect: (File) -> Unit = {},
    bitmaps: List<Bitmap> = emptyList(),
    records: List<File> = emptyList(),
    comment: String = "",
    isLoading: Boolean = false,
    error: String? = null
) {
    var selectedImage by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var isGalleryExpanded by rememberSaveable { mutableStateOf(bitmaps.isNotEmpty()) }
    var isRecordsExpanded by rememberSaveable { mutableStateOf(records.isNotEmpty()) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Update expanded states when data changes
    LaunchedEffect(bitmaps) {
        if (bitmaps.isNotEmpty()) {
            isGalleryExpanded = true
        }
    }

    LaunchedEffect(records) {
        if (records.isNotEmpty()) {
            isRecordsExpanded = true
        }
    }

    // Show error dialog when error state changes
    LaunchedEffect(error) {
        if (error != null && error.isNotEmpty() && !showErrorDialog) {
            errorMessage = error
            showErrorDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxWidth(),
            floatingActionButton = {
                FloatingActionButton(onClick = onGotoList) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "List")
                }
            }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Header(onConfig = onGotoConfig)
                    
                    // Gallery Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Images",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = { isGalleryExpanded = !isGalleryExpanded }) {
                                    Icon(
                                        imageVector = if (isGalleryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isGalleryExpanded) "Collapse" else "Expand"
                                    )
                                }
                            }
                            if (isGalleryExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                ) {
                                    GalleryView(
                                        bitmaps = bitmaps,
                                        onRemoveBitmap = onRemoveBitmap,
                                        onImageSelected = { selectedImage = it }
                                    )
                                }
                            }
                        }
                    }

                    // Records Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recordings",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = { isRecordsExpanded = !isRecordsExpanded }) {
                                    Icon(
                                        imageVector = if (isRecordsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isRecordsExpanded) "Collapse" else "Expand"
                                    )
                                }
                            }
                            if (isRecordsExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                ) {
                                    RecordsList(records = records, onRemoveRecord = onRemoveRecord)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        DeviationForm(
                            onCameraOpen = onCameraOpen,
                            onRecordTaken = onRecordTaken,
                            onSubmit = onSubmit,
                            onCancel = {
                                onCancel()
                                // Collapse panels after canceling
                                isGalleryExpanded = false
                                isRecordsExpanded = false
                            },
                            onCommentChanged = onCommentChanged,
                            comment = comment,
                            isLoading = isLoading,
                            onImageSelect = onImageSelect
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Full screen image preview
        selectedImage?.let { bitmap ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            modifier = Modifier.size(32.dp),
                            onClick = { selectedImage = null },
                            border = BorderStroke(1.dp, Color.White),
                            contentPadding = PaddingValues(2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                            ),
                            shape = CircleShape,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun Header(onConfig: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(modifier = Modifier.padding(end = 16.dp))
        Text(
            text = "Issue Wizard",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onConfig) {
            Icon(
                imageVector = Icons.Default.Settings,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
private fun AppIcon(
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val context = LocalContext.current
    val appIcon = context.packageManager.getApplicationIcon(context.packageName)
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    appIcon.setBounds(0, 0, size, size)
    appIcon.draw(canvas)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "App Icon",
        modifier = modifier.size(size.dp)
    )
}