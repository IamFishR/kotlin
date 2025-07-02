package com.win11launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.telephony.TelephonyManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

data class SystemStatus(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val wifiConnected: Boolean = false,
    val wifiSignalStrength: Int = 0, // 0-4 levels
    val mobileDataConnected: Boolean = false,
    val mobileSignalStrength: Int = 0, // 0-4 levels
    val networkOperator: String = "",
    val currentTime: String = "",
    val currentDate: String = ""
)

class SystemStatusManager(private val context: Context) {
    
    private val _systemStatus = mutableStateOf(SystemStatus())
    val systemStatus: State<SystemStatus> = _systemStatus
    
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
    
    private var isReceiverRegistered = false
    private var timeUpdateJob: Job? = null
    private var scope: CoroutineScope? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> updateBatteryStatus(intent)
                Intent.ACTION_TIME_TICK -> updateDateTime()
            }
        }
    }
    
    fun startMonitoring() {
        if (scope == null) {
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
        
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_TIME_TICK)
            }
            try {
                context.registerReceiver(batteryReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                // Handle registration failure
            }
        }
        
        // Register network callback
        registerNetworkCallback()
        
        // Initial updates
        try {
            updateBatteryStatus()
            updateNetworkStatus()
            updateDateTime()
            
            // Start periodic time updates
            startTimeUpdates()
        } catch (e: Exception) {
            // Handle any initialization errors
        }
    }
    
    fun stopMonitoring() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
        
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                // Handle unregister failure
            } finally {
                isReceiverRegistered = false
            }
        }
        
        // Unregister network callback
        unregisterNetworkCallback()
        
        scope?.cancel()
        scope = null
    }
    
    private fun updateBatteryStatus(intent: Intent? = null) {
        val batteryIntent = intent ?: context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            
            val batteryLevel = if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                100
            }
            
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL
            
            _systemStatus.value = _systemStatus.value.copy(
                batteryLevel = batteryLevel,
                isCharging = isCharging
            )
        }
    }
    
    private fun updateNetworkStatus() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        // Check WiFi
        val wifiConnected = isWifiConnected(connectivityManager)
        val wifiSignalStrength = if (wifiConnected) getWifiSignalStrength(wifiManager) else 0
        
        // Check Mobile Data
        val mobileDataConnected = isMobileDataConnected(connectivityManager)
        val mobileSignalStrength = if (mobileDataConnected) getMobileSignalStrength(telephonyManager) else 0
        val networkOperator = telephonyManager.networkOperatorName ?: ""
        
        _systemStatus.value = _systemStatus.value.copy(
            wifiConnected = wifiConnected,
            wifiSignalStrength = wifiSignalStrength,
            mobileDataConnected = mobileDataConnected,
            mobileSignalStrength = mobileSignalStrength,
            networkOperator = networkOperator
        )
    }
    
    private fun updateDateTime() {
        val now = Date()
        val timeString = timeFormat.format(now)
        val dateString = dateFormat.format(now)
        
        _systemStatus.value = _systemStatus.value.copy(
            currentTime = timeString,
            currentDate = dateString
        )
    }
    
    private fun isWifiConnected(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    private fun isMobileDataConnected(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    private fun getWifiSignalStrength(wifiManager: WifiManager): Int {
        val wifiInfo = wifiManager.connectionInfo
        val rssi = wifiInfo.rssi
        return when {
            rssi >= -50 -> 4  // Excellent
            rssi >= -60 -> 3  // Good
            rssi >= -70 -> 2  // Fair
            rssi >= -80 -> 1  // Poor
            else -> 0         // No signal
        }
    }
    
    private fun getMobileSignalStrength(telephonyManager: TelephonyManager): Int {
        // This is a simplified version - actual implementation would need
        // to handle different network types and API levels
        return try {
            // For now, return a mock value of 3 (good signal)
            // In real implementation, you'd use telephonyManager.signalStrength
            3
        } catch (e: Exception) {
            0
        }
    }
    
    private fun registerNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkStatus()
            }
            
            override fun onLost(network: Network) {
                updateNetworkStatus()
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkStatus()
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
        } catch (e: Exception) {
            // Handle registration failure
        }
    }
    
    private fun unregisterNetworkCallback() {
        networkCallback?.let { callback ->
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Handle unregister failure
            }
        }
        networkCallback = null
    }
    
    private fun startTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = scope?.launch {
            while (isActive) {
                try {
                    updateDateTime()
                    delay(1000) // Update every second
                } catch (e: Exception) {
                    // Handle any errors during time updates
                    break
                }
            }
        }
    }
}