package com.win11launcher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import android.util.Log
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
            Log.d("WallpaperBackground", "Processing wallpaper: $wallpaper")
            if (wallpaper != null) {
                Log.d("WallpaperBackground", "Wallpaper intrinsic size: ${wallpaper.intrinsicWidth}x${wallpaper.intrinsicHeight}")
                Log.d("WallpaperBackground", "Wallpaper bounds: ${wallpaper.bounds}")
            }
            try {
                if (wallpaper == null) {
                    Log.d("WallpaperBackground", "Wallpaper is null")
                    return@remember null
                }
                
                // First try the standard toBitmap method
                var bitmap = try {
                    wallpaper.toBitmap()
                } catch (e: Exception) {
                    Log.w("WallpaperBackground", "Standard toBitmap failed, trying manual creation", e)
                    null
                }
                
                // If standard method failed, try manual bitmap creation
                if (bitmap == null && wallpaper.intrinsicWidth > 0 && wallpaper.intrinsicHeight > 0) {
                    try {
                        bitmap = android.graphics.Bitmap.createBitmap(
                            wallpaper.intrinsicWidth,
                            wallpaper.intrinsicHeight,
                            android.graphics.Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(bitmap)
                        wallpaper.draw(canvas)
                        Log.d("WallpaperBackground", "Manual bitmap creation succeeded")
                    } catch (e: Exception) {
                        Log.w("WallpaperBackground", "Manual bitmap creation failed", e)
                        bitmap = null
                    }
                }
                
                Log.d("WallpaperBackground", "Final bitmap: $bitmap")
                if (bitmap != null) {
                    Log.d("WallpaperBackground", "Bitmap size: ${bitmap.width}x${bitmap.height}")
                    Log.d("WallpaperBackground", "Bitmap config: ${bitmap.config}")
                    bitmap.asImageBitmap()
                } else {
                    Log.w("WallpaperBackground", "All bitmap conversion methods failed")
                    null
                }
            } catch (e: Exception) {
                Log.e("WallpaperBackground", "Failed to convert wallpaper to bitmap", e)
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