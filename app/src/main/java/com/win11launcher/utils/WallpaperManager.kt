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
            // Check if we have permission to read external storage
            val hasPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d("WallpaperManager", "READ_EXTERNAL_STORAGE permission: $hasPermission")
            Log.d("WallpaperManager", "Device manufacturer: ${Build.MANUFACTURER}")
            
            // Try different methods to get the wallpaper
            var drawable: Drawable? = null
            
            // Samsung-specific handling
            if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
                drawable = getSamsungWallpaper()
            }
            
            // Method 1: Try regular drawable first (user-selected wallpaper)
            if (drawable == null) {
                try {
                    drawable = wallpaperManager.drawable
                    Log.d("WallpaperManager", "Regular wallpaper drawable: $drawable")
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to get regular wallpaper", e)
                }
            }
            
            // Method 2: Try peekDrawable for live wallpapers
            if (drawable == null) {
                try {
                    drawable = wallpaperManager.peekDrawable()
                    Log.d("WallpaperManager", "Peek wallpaper drawable: $drawable")
                } catch (e: Exception) {
                    Log.w("WallpaperManager", "Failed to peek wallpaper", e)
                }
            }
            
            // Method 3: Try with specific flags for Samsung devices
            if (drawable == null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        drawable = wallpaperManager.getDrawable(WallpaperManager.FLAG_SYSTEM)
                        Log.d("WallpaperManager", "System flag wallpaper drawable: $drawable")
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
    
    private fun getSamsungWallpaper(): Drawable? {
        return try {
            Log.d("WallpaperManager", "Attempting Samsung-specific wallpaper access")
            
            // Try to get Samsung wallpaper using reflection to avoid the SemWallpaperResourcesInfo error
            val semWallpaperManagerClass = Class.forName("android.app.SemWallpaperManager")
            val getInstance = semWallpaperManagerClass.getMethod("getInstance", Context::class.java)
            val semWallpaperManager = getInstance.invoke(null, context)
            
            // Try to get the wallpaper drawable through Samsung's API
            val getDrawableMethod = semWallpaperManagerClass.getMethod("getDrawable")
            val drawable = getDrawableMethod.invoke(semWallpaperManager) as? Drawable
            
            Log.d("WallpaperManager", "Samsung wallpaper drawable: $drawable")
            drawable
        } catch (e: Exception) {
            Log.w("WallpaperManager", "Samsung-specific wallpaper access failed", e)
            
            // Fallback: Try to get wallpaper using different Samsung methods
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Try to get the wallpaper file using getWallpaperFile
                    val wallpaperFileMethod = wallpaperManager.javaClass.getMethod("getWallpaperFile", Int::class.javaPrimitiveType)
                    val inputStream = wallpaperFileMethod.invoke(wallpaperManager, WallpaperManager.FLAG_SYSTEM) as java.io.InputStream?
                    Log.d("WallpaperManager", "Wallpaper input stream: $inputStream")
                    if (inputStream != null) {
                        val drawable = Drawable.createFromStream(inputStream, null)
                        Log.d("WallpaperManager", "File-based wallpaper drawable: $drawable")
                        inputStream.close()
                        drawable
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e2: Exception) {
                Log.w("WallpaperManager", "File-based wallpaper access failed", e2)
                null
            }
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