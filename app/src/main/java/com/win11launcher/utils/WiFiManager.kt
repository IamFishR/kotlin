package com.win11launcher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import java.lang.reflect.Method

data class WiFiInfo(
    val isEnabled: Boolean = false,
    val isConnected: Boolean = false,
    val ssid: String = "",
    val signalStrength: Int = 0, // 0-4 levels
    val ipAddress: String = "",
    val linkSpeed: Int = 0
)

class WiFiManager(private val context: Context) {
    
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _wifiInfo = mutableStateOf(WiFiInfo())
    val wifiInfo: State<WiFiInfo> = _wifiInfo
    
    /**
     * Check if WiFi is enabled
     */
    fun isWiFiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Toggle WiFi on/off
     * Uses direct toggle for all Android versions with fallback methods
     */
    @SuppressLint("MissingPermission")
    fun toggleWiFi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29) and above, direct toggling of Wi-Fi is restricted.
            // Applications should open the Wi-Fi settings panel for the user to make the change.
            openWiFiSettings()
        } else {
            // For older versions, direct toggling is allowed with CHANGE_WIFI_STATE permission.
            wifiManager.isWifiEnabled = !isWiFiEnabled()
        }
    }

    /**
     * Open WiFi settings
     */
    private fun openWiFiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    
    /**
     * Get current WiFi connection info
     */
    @SuppressLint("MissingPermission")
    fun getCurrentWiFiInfo(): WiFiInfo {
        val isEnabled = isWiFiEnabled()
        val isConnected = isWiFiConnected()
        
        if (!isEnabled || !isConnected) {
            return WiFiInfo(
                isEnabled = isEnabled,
                isConnected = false
            )
        }
        
        val connectionInfo = wifiManager.connectionInfo
        val ssid = connectionInfo?.ssid?.removeSurrounding("\"") ?: ""
        val signalStrength = calculateSignalStrength(connectionInfo?.rssi ?: -100)
        val ipAddress = formatIpAddress(connectionInfo?.ipAddress ?: 0)
        val linkSpeed = connectionInfo?.linkSpeed ?: 0
        
        return WiFiInfo(
            isEnabled = isEnabled,
            isConnected = isConnected,
            ssid = ssid,
            signalStrength = signalStrength,
            ipAddress = ipAddress,
            linkSpeed = linkSpeed
        )
    }
    
    /**
     * Check if WiFi is connected
     */
    private fun isWiFiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Calculate signal strength level (0-4)
     */
    private fun calculateSignalStrength(rssi: Int): Int {
        return when {
            rssi >= -50 -> 4  // Excellent
            rssi >= -60 -> 3  // Good
            rssi >= -70 -> 2  // Fair
            rssi >= -80 -> 1  // Poor
            else -> 0         // No signal
        }
    }
    
    /**
     * Format IP address from integer to string
     */
    private fun formatIpAddress(ipAddress: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }
    
    /**
     * Update WiFi info state
     */
    fun updateWiFiInfo() {
        _wifiInfo.value = getCurrentWiFiInfo()
    }
    
    /**
     * Get WiFi signal strength icon resource based on level
     */
    fun getWiFiSignalIcon(signalLevel: Int): String {
        return when (signalLevel) {
            0 -> "wifi_off"
            1 -> "signal_wifi_1_bar"
            2 -> "signal_wifi_2_bar"
            3 -> "signal_wifi_3_bar"
            4 -> "signal_wifi_4_bar"
            else -> "wifi_off"
        }
    }
    
    /**
     * Get WiFi status text
     */
    fun getWiFiStatusText(): String {
        val info = getCurrentWiFiInfo()
        return when {
            !info.isEnabled -> "WiFi is off"
            !info.isConnected -> "WiFi is on but not connected"
            info.ssid.isNotEmpty() -> "Connected to ${info.ssid}"
            else -> "WiFi is connected"
        }
    }
}