package com.hackathon2025.deviationwizard.components

import androidx.camera.core.Preview
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * A composable that displays a camera preview using the provided camera controller.
 *
 * @param controller The [LifecycleCameraController] that manages the camera preview
 * @param modifier The [Modifier] to be applied to the camera preview
 */
@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create and configure the preview use case
    val preview = remember {
        Preview.Builder()
            .build()
    }
    
    // Bind the controller to the lifecycle
    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
        }
    }
    
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                // Configure the preview view
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                // Set the surface provider for the preview
                preview.surfaceProvider = surfaceProvider
            }
        },
        modifier = modifier
    )
}