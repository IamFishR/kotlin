package com.win11launcher.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager as AndroidBluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.reflect.Method

data class BluetoothInfo(
    val isEnabled: Boolean = false,
    val isSupported: Boolean = false,
    val connectedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val isConnecting: Boolean = false,
    val adapterState: Int = BluetoothAdapter.STATE_OFF
)

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean,
    val deviceType: Int,
    val bondState: Int
)

class BluetoothManager(private val context: Context) {
    
    private val androidBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as AndroidBluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = androidBluetoothManager.adapter
    
    private val _bluetoothInfo = mutableStateOf(BluetoothInfo())
    val bluetoothInfo: State<BluetoothInfo> = _bluetoothInfo
    
    private var isReceiverRegistered = false
    
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                    updateBluetoothInfo()
                }
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    updateBluetoothInfo()
                }
            }
        }
    }
    
    init {
        registerReceiver()
        updateBluetoothInfo()
    }
    
    /**
     * Check if Bluetooth is supported on this device
     */
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null && 
               context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Toggle Bluetooth on/off
     * Uses direct toggle for all Android versions with fallback methods
     */
    @SuppressLint("MissingPermission")
    fun toggleBluetooth() {
        if (!isBluetoothSupported()) {
            return
        }

        // Check permissions first
        if (!hasBluetoothPermissions()) {
            openBluetoothSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29) and above, direct toggling of Bluetooth is restricted.
            // Applications should open the Bluetooth settings panel for the user to make the change.
            openBluetoothSettings()
        } else {
            // For older versions, direct toggling is allowed with BLUETOOTH_ADMIN permission.
            if (isBluetoothEnabled()) {
                bluetoothAdapter?.disable()
            } else {
                bluetoothAdapter?.enable()
            }
        }
    }

    /**
     * Open Bluetooth settings
     */
    private fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Enable Bluetooth
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        if (!isBluetoothSupported()) {
            return
        }

        if (!hasBluetoothPermissions()) {
            openBluetoothSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openBluetoothSettings()
        } else {
            bluetoothAdapter?.enable()
        }
    }

    /**
     * Disable Bluetooth
     */
    @SuppressLint("MissingPermission")
    fun disableBluetooth() {
        if (!isBluetoothSupported()) {
            return
        }

        if (!hasBluetoothPermissions()) {
            openBluetoothSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openBluetoothSettings()
        } else {
            bluetoothAdapter?.disable()
        }
    }
    
    /**
     * Get list of connected Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun getConnectedDevices(): List<BluetoothDeviceInfo> {
        if (!isBluetoothSupported() || !isBluetoothEnabled() || !hasBluetoothPermissions()) {
            return emptyList()
        }
        
        val connectedDevices = mutableListOf<BluetoothDeviceInfo>()
        
        try {
            // Get bonded devices
            val bondedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
            
            for (device in bondedDevices) {
                val deviceInfo = BluetoothDeviceInfo(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    isConnected = isDeviceConnected(device),
                    deviceType = device.type,
                    bondState = device.bondState
                )
                connectedDevices.add(deviceInfo)
            }
        } catch (e: SecurityException) {
            // Handle permission issues
        }
        
        return connectedDevices
    }
    
    /**
     * Check if a specific device is connected
     */
    @SuppressLint("MissingPermission")
    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return try {
            // This is a simplified check - actual implementation would need
            // to check specific profiles (A2DP, HFP, etc.)
            device.bondState == BluetoothDevice.BOND_BONDED
        } catch (e: SecurityException) {
            false
        }
    }
    
    /**
     * Get current Bluetooth adapter state
     */
    fun getAdapterState(): Int {
        return bluetoothAdapter?.state ?: BluetoothAdapter.STATE_OFF
    }
    
    /**
     * Get Bluetooth status text
     */
    fun getBluetoothStatusText(): String {
        return when {
            !isBluetoothSupported() -> "Bluetooth not supported"
            !isBluetoothEnabled() -> "Off"
            else -> {
                val connectedCount = getConnectedDevices().count { it.isConnected }
                when (connectedCount) {
                    0 -> "On"
                    1 -> "1 device connected"
                    else -> "$connectedCount devices connected"
                }
            }
        }
    }
    
    /**
     * Update Bluetooth info state
     */
    private fun updateBluetoothInfo() {
        val isEnabled = isBluetoothEnabled()
        val isSupported = isBluetoothSupported()
        val connectedDevices = if (isEnabled) getConnectedDevices() else emptyList()
        val adapterState = getAdapterState()
        
        _bluetoothInfo.value = BluetoothInfo(
            isEnabled = isEnabled,
            isSupported = isSupported,
            connectedDevices = connectedDevices,
            isConnecting = adapterState == BluetoothAdapter.STATE_TURNING_ON,
            adapterState = adapterState
        )
    }
    
    /**
     * Check if we have the required Bluetooth permissions
     */
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ permissions
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Legacy permissions
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Register broadcast receiver for Bluetooth events
     */
    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            try {
                context.registerReceiver(bluetoothReceiver, filter)
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
                context.unregisterReceiver(bluetoothReceiver)
            } catch (e: Exception) {
                // Handle unregister failure
            } finally {
                isReceiverRegistered = false
            }
        }
    }
    
    /**
     * Force update of Bluetooth info
     */
    fun refreshBluetoothInfo() {
        updateBluetoothInfo()
    }
    
    /**
     * Check if Bluetooth is in a transitional state
     */
    fun isBluetoothTransitioning(): Boolean {
        val state = getAdapterState()
        return state == BluetoothAdapter.STATE_TURNING_ON || 
               state == BluetoothAdapter.STATE_TURNING_OFF
    }
    
    /**
     * Get Bluetooth icon resource name based on state
     */
    fun getBluetoothIconResource(): String {
        return when {
            !isBluetoothSupported() -> "bluetooth_disabled"
            !isBluetoothEnabled() -> "bluetooth_disabled"
            getConnectedDevices().any { it.isConnected } -> "bluetooth_connected"
            else -> "bluetooth"
        }
    }
}