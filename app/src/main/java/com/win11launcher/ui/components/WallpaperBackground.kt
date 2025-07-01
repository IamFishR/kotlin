package com.win11launcher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap

@Composable
fun WallpaperBackground(
    wallpaper: Drawable?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        if (wallpaper != null) {
            try {
                val bitmap = wallpaper.toBitmap()
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } catch (e: Exception) {
                // Fall back to solid background if wallpaper can't be displayed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        } else {
            // Use solid background when no wallpaper is available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        
        content()
    }
}