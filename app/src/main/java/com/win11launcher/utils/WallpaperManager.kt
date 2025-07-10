package com.win11launcher.utils

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import java.io.InputStream
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
            // Check if we have permission to read storage/media
            val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            
            val hasPermission = ContextCompat.checkSelfPermission(
                context, 
                storagePermission
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d("WallpaperManager", "Storage permission ($storagePermission): $hasPermission")
            
            if (!hasPermission) {
                Log.w("WallpaperManager", "READ_EXTERNAL_STORAGE permission not granted, cannot load wallpaper")
                _wallpaper.value = null
                return
            }
            
            // Try different methods to get the wallpaper
            var drawable: Drawable? = null
            
            // Method 1: Try peekDrawable first (works well for launchers)
            try {
                drawable = wallpaperManager.peekDrawable()
                Log.d("WallpaperManager", "Peek wallpaper drawable: $drawable")
                if (drawable != null) {
                    // Ensure the drawable has proper bounds
                    if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        Log.d("WallpaperManager", "Set bounds for peek drawable: ${drawable.bounds}")
                    }
                }
            } catch (e: Exception) {
                Log.w("WallpaperManager", "Failed to peek wallpaper", e)
            }
            
            // Method 2: Try regular drawable (user-selected wallpaper)
            if (drawable == null) {
                try {
                    drawable = wallpaperManager.drawable
                    Log.d("WallpaperManager", "Regular wallpaper drawable: $drawable")
                    if (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        Log.d("WallpaperManager", "Set bounds for regular drawable: ${drawable.bounds}")
                    }
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to get regular wallpaper", e)
                }
            }
            
            // Method 3: Try with FLAG_SYSTEM for home screen wallpaper
            if (drawable == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
                try {
                    drawable = wallpaperManager.getDrawable(WallpaperManager.FLAG_SYSTEM)
                    Log.d("WallpaperManager", "System flag wallpaper drawable: $drawable")
                    if (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        Log.d("WallpaperManager", "Set bounds for system flag drawable: ${drawable.bounds}")
                    }
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to get system flag wallpaper", e)
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
    
    fun hasPermission(): Boolean {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        return ContextCompat.checkSelfPermission(
            context, 
            storagePermission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun rememberWallpaperManager(): SystemWallpaperManager {
    val context = LocalContext.current
    return remember { SystemWallpaperManager(context) }
}