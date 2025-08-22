package com.hackathon2025.deviationwizard.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hackathon2025.deviationwizard.model.AudioRecorderImpl
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviationForm(
    onCameraOpen: () -> Unit,
    onImageSelect: (File) -> Unit,
    onRecordTaken: (File) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onCommentChanged: (String) -> Unit,
    comment: String,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val recorder = remember { AudioRecorderImpl(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Create a temporary file to store the bitmap
                val tempFile = File.createTempFile("image_", ".jpg", context.cacheDir)
                tempFile.outputStream().use { outputStream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                onImageSelect(tempFile)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = onCameraOpen) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Camera"
                )
            }
            IconButton(onClick = {
                val permissions =
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

                val allGranted = permissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                
                if (allGranted) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    permissionLauncher.launch(permissions)
                }
            }) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Default.Image,
                    contentDescription = "Select Image"
                )
            }
            IconButton(onClick = {
                if (isRecording) {
                    recorder.stopRecording()
                    audioFile?.let { onRecordTaken(it) }
                    isRecording = false
                } else {
                    val date = Calendar.getInstance().time
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    val formatedDate = formatter.format(date)
                    val sanitizedDate = formatedDate.replace(Regex("[^a-zA-Z0-9]"), "_")
                    File(context.cacheDir, "${sanitizedDate}.ogg").also {
                        recorder.startRecording(it)
                        audioFile = it
                        isRecording = true
                    }
                }
            }) {
                val tintColor = if (isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(if (isRecording) scale else 1f),
                    imageVector = Icons.Default.KeyboardVoice,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = tintColor
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            label = { Text("Description (optional)") },
            value = comment,
            onValueChange = onCommentChanged,
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { showCancelDialog = true },
                enabled = !isLoading && !isRecording
            ) {
                Text("Cancel")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onSubmit,
                enabled = !isLoading && !isRecording
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Form") },
            text = { Text("Are you sure you want to cancel? All entered data will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    }
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("No, Keep Editing")
                }
            }
        )
    }
}
