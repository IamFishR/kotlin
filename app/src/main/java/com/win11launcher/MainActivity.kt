package com.win11launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.win11launcher.ui.LauncherScreen
import com.win11launcher.ui.components.PermissionsScreen
import com.win11launcher.ui.theme.Win11LauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var permissionsGranted by mutableStateOf(false)
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, isGranted) ->
            if (isGranted) {
                android.util.Log.d("MainActivity", "Permission granted: $permission")
            } else {
                android.util.Log.w("MainActivity", "Permission denied: $permission")
            }
        }
        checkAllPermissions()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Check permissions on app start
        checkAllPermissions()
        
        setContent {
            Win11LauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (permissionsGranted) {
                        LauncherScreen()
                    } else {
                        PermissionsScreen(
                            onAllPermissionsGranted = {
                                permissionsGranted = true
                            },
                            onRequestPermissions = { permissionsList ->
                                permissionLauncher.launch(permissionsList.toTypedArray())
                            },
                            onOpenSettings = {
                                openAppSettings()
                            }
                        )
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions when returning from settings
        if (!permissionsGranted) {
            checkAllPermissions()
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Hide system UI when window has focus (safe time to access window)
            hideSystemUI()
        }
    }
    
    private fun checkAllPermissions() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        val requiredPermissions = listOf(
            storagePermission,
            Manifest.permission.READ_PHONE_STATE
        )
        
        val normalPermissionsGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        val specialPermissionsGranted = Settings.canDrawOverlays(this) && Settings.System.canWrite(this)
        
        permissionsGranted = normalPermissionsGranted && specialPermissionsGranted
        
        android.util.Log.d("MainActivity", "Normal permissions granted: $normalPermissionsGranted")
        android.util.Log.d("MainActivity", "Special permissions granted: $specialPermissionsGranted")
        android.util.Log.d("MainActivity", "All permissions granted: $permissionsGranted")
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
    
    private fun hideSystemUI() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
        } catch (e: Exception) {
            // Handle any errors with hiding system UI gracefully
        }
    }
    
}