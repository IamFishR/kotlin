package com.win11launcher.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class SystemWallpaperManager(private val context: Context) {
    private val wallpaperManager = WallpaperManager.getInstance(context)
    private val _wallpaper = mutableStateOf<Drawable?>(null)
    
    init {
        loadWallpaper()
    }
    
    private fun loadWallpaper() {
        try {
            val drawable = wallpaperManager.drawable
            _wallpaper.value = drawable
        } catch (e: Exception) {
            // If we can't get the wallpaper, _wallpaper remains null
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