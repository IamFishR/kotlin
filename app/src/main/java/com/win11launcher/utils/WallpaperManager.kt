package com.win11launcher.utils

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class SystemWallpaperManager(private val context: Context) {
    private val wallpaperManager = WallpaperManager.getInstance(context)
    private val _wallpaper = mutableStateOf<Drawable?>(null)
    
    init {
        loadWallpaper()
    }
    
    private fun loadWallpaper() {
        try {
            // Check if we have permission to read external storage
            val hasPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d("WallpaperManager", "READ_EXTERNAL_STORAGE permission: $hasPermission")
            
            // Try different methods to get the wallpaper
            var drawable: Drawable? = null
            
            // Method 1: Try built-in drawable
            try {
                drawable = wallpaperManager.builtInDrawable
                Log.d("WallpaperManager", "Built-in wallpaper drawable: $drawable")
            } catch (e: Exception) {
                Log.w("WallpaperManager", "Failed to get built-in wallpaper", e)
            }
            
            // Method 2: Try regular drawable if built-in failed
            if (drawable == null) {
                try {
                    drawable = wallpaperManager.drawable
                    Log.d("WallpaperManager", "Regular wallpaper drawable: $drawable")
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to get regular wallpaper", e)
                }
            }
            
            // Method 3: Try peekDrawable as fallback
            if (drawable == null) {
                try {
                    drawable = wallpaperManager.peekDrawable()
                    Log.d("WallpaperManager", "Peek wallpaper drawable: $drawable")
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to peek wallpaper", e)
                }
            }
            
            if (drawable != null) {
                Log.d("WallpaperManager", "Final wallpaper size: ${drawable.intrinsicWidth}x${drawable.intrinsicHeight}")
            } else {
                Log.w("WallpaperManager", "No wallpaper drawable found")
            }
            
            _wallpaper.value = drawable
        } catch (e: Exception) {
            Log.e("WallpaperManager", "Failed to load wallpaper", e)
            _wallpaper.value = null
        }
    }
    
    fun getWallpaper(): Drawable? = _wallpaper.value
    
    fun refreshWallpaper() {
        loadWallpaper()
    }
}

@Composable
fun rememberWallpaperManager(): SystemWallpaperManager {
    val context = LocalContext.current
    return remember { SystemWallpaperManager(context) }
}