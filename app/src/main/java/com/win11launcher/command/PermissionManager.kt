package com.win11launcher.command

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class PermissionManager(private val activity: ComponentActivity) {
    
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
        pendingCallback?.invoke(permissions)
        pendingCallback = null
    }
    
    private var pendingCallback: ((Map<String, Boolean>) -> Unit)? = null
    
    companion object {
        private val PERMISSION_DESCRIPTIONS = mapOf(
            "android.permission.ACCESS_WIFI_STATE" to "Access WiFi information",
            "android.permission.CHANGE_WIFI_STATE" to "Modify WiFi settings",
            "android.permission.ACCESS_FINE_LOCATION" to "Access precise location for WiFi scanning",
            "android.permission.BLUETOOTH" to "Access Bluetooth functionality",
            "android.permission.BLUETOOTH_ADMIN" to "Manage Bluetooth connections",
            "android.permission.BLUETOOTH_CONNECT" to "Connect to Bluetooth devices",
            "android.permission.WRITE_EXTERNAL_STORAGE" to "Write to external storage",
            "android.permission.READ_EXTERNAL_STORAGE" to "Read from external storage"
        )
    }
    
    fun checkAndRequestPermissions(
        permissions: List<String>,
        onResult: (granted: Boolean, missing: List<String>) -> Unit
    ) {
        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            onResult(true, emptyList())
            return
        }
        
        // Show explanation to user
        val explanations = missingPermissions.mapNotNull { permission ->
            PERMISSION_DESCRIPTIONS[permission]?.let { description ->
                "• $description"
            }
        }
        
        if (explanations.isNotEmpty()) {
            // In a real implementation, you'd show a dialog here
            // For now, we'll just request the permissions
        }
        
        pendingCallback = { results ->
            val stillMissing = results.filter { !it.value }.keys.toList()
            onResult(stillMissing.isEmpty(), stillMissing)
        }
        
        permissionLauncher.launch(missingPermissions.toTypedArray())
    }
    
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
    
    fun getPermissionExplanation(permission: String): String {
        return PERMISSION_DESCRIPTIONS[permission] ?: "System permission required"
    }
}