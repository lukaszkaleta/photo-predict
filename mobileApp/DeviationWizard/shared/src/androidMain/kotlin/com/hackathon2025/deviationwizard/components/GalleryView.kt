package com.hackathon2025.deviationwizard.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun GalleryView(
    modifier: Modifier = Modifier,
    bitmaps: List<Bitmap>,
    onRemoveBitmap: (Bitmap) -> Unit = {},
    onImageSelected: (Bitmap) -> Unit = {}
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        contentPadding = PaddingValues(16.dp),
        columns = StaggeredGridCells.Fixed(3),
    ) {
        items(bitmaps.size) { bitmap ->
            Box(modifier = Modifier.height(150.dp)) {
                Image(
                    bitmap = bitmaps[bitmap].asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onImageSelected(bitmaps[bitmap]) }
                )
                OutlinedButton(
                    modifier = Modifier
                        .size(32.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(5.dp),
                    onClick = {
                        onRemoveBitmap(bitmaps[bitmap])
                    },
                    border = BorderStroke(1.dp, Color.White),
                    contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    ),
                    shape = CircleShape,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}