package com.win11launcher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap

@Composable
fun WallpaperBackground(
    wallpaper: Drawable?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        val wallpaperBitmap = remember(wallpaper) {
            try {
                wallpaper?.toBitmap()?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        
        if (wallpaperBitmap != null) {
            Image(
                bitmap = wallpaperBitmap,
                contentDescription = "Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Use solid background when no wallpaper is available or conversion failed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        
        content()
    }
}