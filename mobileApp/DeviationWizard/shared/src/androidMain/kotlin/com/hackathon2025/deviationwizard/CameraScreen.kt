package com.hackathon2025.deviationwizard

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import com.hackathon2025.deviationwizard.components.CameraView

@OptIn(UnstableApi::class)
@Composable
fun CameraScreen(
    controller: LifecycleCameraController,
    onGoBack: () -> Unit,
    onPhotoTaken: (Bitmap) -> Unit
) {
    CameraView(
        controller = controller,
        onCancel = onGoBack,
        onPhotoTaken = {
            onPhotoTaken(it)
            onGoBack()
        }
    )
}