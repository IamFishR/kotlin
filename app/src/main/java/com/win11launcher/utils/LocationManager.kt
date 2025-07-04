package com.win11launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidLocationManager
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.core.content.ContextCompat

data class LocationInfo(
    val isEnabled: Boolean = false,
    val isSupported: Boolean = false,
    val hasPermission: Boolean = false,
    val providerStates: Map<String, Boolean> = emptyMap()
)

class LocationManager(private val context: Context) {
    
    private val androidLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    
    private val _locationInfo = mutableStateOf(LocationInfo())
    val locationInfo: State<LocationInfo> = _locationInfo
    
    private var isReceiverRegistered = false
    
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AndroidLocationManager.PROVIDERS_CHANGED_ACTION -> {
                    updateLocationInfo()
                }
            }
        }
    }
    
    init {
        registerReceiver()
        updateLocationInfo()
    }
    
    /**
     * Check if location services are supported on this device
     */
    fun isLocationSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)
    }
    
    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            androidLocationManager.isLocationEnabled
        } else {
            try {
                val mode = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
                mode != Settings.Secure.LOCATION_MODE_OFF
            } catch (e: Settings.SettingNotFoundException) {
                false
            }
        }
    }
    
    /**
     * Check if we have location permissions
     */
    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Toggle location services
     * Note: Location services cannot be directly toggled by apps for security reasons
     * This method will return false to indicate the toggle was not successful
     */
    fun toggleLocation(): Boolean {
        // Location services cannot be directly toggled by third-party apps
        // for security and privacy reasons. This is enforced by Android.
        return false
    }
    
    /**
     * Get location provider states
     */
    fun getProviderStates(): Map<String, Boolean> {
        val providers = mutableMapOf<String, Boolean>()
        
        try {
            providers["gps"] = androidLocationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)
            providers["network"] = androidLocationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                providers["fused"] = androidLocationManager.isProviderEnabled(AndroidLocationManager.FUSED_PROVIDER)
            }
        } catch (e: Exception) {
            // Handle any exceptions accessing providers
        }
        
        return providers
    }
    
    /**
     * Get location status text
     */
    fun getLocationStatusText(): String {
        return when {
            !isLocationSupported() -> "Location not supported"
            !isLocationEnabled() -> "Off"
            !hasLocationPermissions() -> "Permission required"
            else -> {
                val providers = getProviderStates()
                val enabledProviders = providers.values.count { it }
                when (enabledProviders) {
                    0 -> "Off"
                    else -> "On"
                }
            }
        }
    }
    
    /**
     * Update location info state
     */
    private fun updateLocationInfo() {
        val isEnabled = isLocationEnabled()
        val isSupported = isLocationSupported()
        val hasPermission = hasLocationPermissions()
        val providerStates = if (isEnabled) getProviderStates() else emptyMap()
        
        _locationInfo.value = LocationInfo(
            isEnabled = isEnabled,
            isSupported = isSupported,
            hasPermission = hasPermission,
            providerStates = providerStates
        )
    }
    
    /**
     * Open location settings
     */
    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    
    /**
     * Register broadcast receiver for location events
     */
    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(AndroidLocationManager.PROVIDERS_CHANGED_ACTION)
            }
            try {
                context.registerReceiver(locationReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                // Handle registration failure
            }
        }
    }
    
    /**
     * Unregister broadcast receiver
     */
    fun unregisterReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(locationReceiver)
            } catch (e: Exception) {
                // Handle unregister failure
            } finally {
                isReceiverRegistered = false
            }
        }
    }
    
    /**
     * Force update of location info
     */
    fun refreshLocationInfo() {
        updateLocationInfo()
    }
    
    /**
     * Check if high accuracy mode is enabled (GPS + Network)
     */
    fun isHighAccuracyEnabled(): Boolean {
        val providers = getProviderStates()
        return providers["gps"] == true && providers["network"] == true
    }
    
    /**
     * Check if battery saving mode is enabled (Network only)
     */
    fun isBatterySavingEnabled(): Boolean {
        val providers = getProviderStates()
        return providers["gps"] == false && providers["network"] == true
    }
    
    /**
     * Check if device only mode is enabled (GPS only)
     */
    fun isDeviceOnlyEnabled(): Boolean {
        val providers = getProviderStates()
        return providers["gps"] == true && providers["network"] == false
    }
}