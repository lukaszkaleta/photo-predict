package com.hackathon2025.deviationwizard.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

@Composable
fun CameraView(
    controller: LifecycleCameraController,
    onCancel: () -> Unit,
    onPhotoTaken: (Bitmap) -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CameraPreview(
                controller = controller, modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        modifier = Modifier.size(50.dp),
                        onClick = onCancel,
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(3.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                        ),
                        shape = CircleShape,
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Take a photo",
                            tint = Color.White
                        )
                    }
                    val context = LocalContext.current
                    OutlinedButton(
                        modifier = Modifier.size(70.dp),
                        onClick = {
                            takePhoto(
                                controller = controller,
                                onPhotoTaken = onPhotoTaken,
                                context = context
                            )
                        },
                        border = BorderStroke(2.dp, Color.White),
                        contentPadding = PaddingValues(3.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                        ),
                        shape = CircleShape,
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Filled.Circle,
                            contentDescription = "Take a photo",
                            tint = Color.White
                        )
                    }

                    var toggleCamera by remember { mutableStateOf(false) }

                    // Define the animation values
                    val transition = updateTransition(
                        targetState = toggleCamera,
                        label = "ToggleButtonTransition"
                    )

                    // Define the start and end values for the animation
                    val rotation by transition.animateFloat(
                        label = "CameraToggleRotation",
                        transitionSpec = {
                            tween(durationMillis = 500, easing = FastOutSlowInEasing)
                        }
                    ) { toggled ->
                        if (toggled) 360f else 0f
                    }

                    OutlinedButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            controller.cameraSelector =
                                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                            toggleCamera = !toggleCamera
                        },
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(3.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                        ),
                        shape = CircleShape,
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotation),
                            imageVector = Icons.Default.Cached,
                            contentDescription = "Take a photo",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("RestrictedApi")
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                    if(controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) postScale(-1f, 1f)
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                onPhotoTaken(rotatedBitmap)
            }

            @OptIn(UnstableApi::class)
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Error taking photo", exception)
            }
        })
}