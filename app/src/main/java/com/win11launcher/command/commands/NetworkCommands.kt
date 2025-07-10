package com.win11launcher.command.commands

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.net.wifi.ScanResult
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.win11launcher.command.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object NetworkCommands {
    
    fun getNetworkCommand() = CommandDefinition(
        name = "network",
        category = CommandCategory.NET,
        description = "Show network information and status",
        usage = "network [status|interfaces|connections]",
        examples = listOf(
            "network",
            "network status",
            "network interfaces",
            "network connections"
        ),
        parameters = listOf(
            CommandParameter(
                name = "info",
                type = ParameterType.ENUM,
                description = "Type of network information to show",
                options = listOf("status", "interfaces", "connections"),
                defaultValue = "status"
            )
        ),
        aliases = listOf("net", "netinfo"),
        executor = NetworkCommandExecutor()
    )
    
    fun getWifiCommand() = CommandDefinition(
        name = "wifi",
        category = CommandCategory.NET,
        description = "Manage WiFi connections",
        usage = "wifi [scan|status|connect|disconnect|list]",
        examples = listOf(
            "wifi scan",
            "wifi status",
            "wifi connect --ssid=MyNetwork --password=mypass",
            "wifi disconnect",
            "wifi list"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "WiFi action to perform",
                options = listOf("scan", "status", "connect", "disconnect", "list"),
                defaultValue = "status"
            ),
            CommandParameter(
                name = "ssid",
                type = ParameterType.STRING,
                description = "WiFi network SSID for connection",
                required = false
            ),
            CommandParameter(
                name = "password",
                type = ParameterType.STRING,
                description = "WiFi network password",
                required = false
            )
        ),
        requiresPermissions = listOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        executor = WifiCommandExecutor()
    )
    
    fun getBluetoothCommand() = CommandDefinition(
        name = "bluetooth",
        category = CommandCategory.NET,
        description = "Manage Bluetooth connections",
        usage = "bluetooth [scan|status|pair|unpair|list]",
        examples = listOf(
            "bluetooth scan",
            "bluetooth status",
            "bluetooth pair --device=00:11:22:33:44:55",
            "bluetooth unpair --device=00:11:22:33:44:55",
            "bluetooth list"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Bluetooth action to perform",
                options = listOf("scan", "status", "pair", "unpair", "list"),
                defaultValue = "status"
            ),
            CommandParameter(
                name = "device",
                type = ParameterType.MAC_ADDRESS,
                description = "Bluetooth device MAC address",
                required = false
            )
        ),
        aliases = listOf("bt", "blue"),
        requiresPermissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT
        ),
        executor = BluetoothCommandExecutor()
    )
    
    fun getPingCommand() = CommandDefinition(
        name = "ping",
        category = CommandCategory.NET,
        description = "Test network connectivity to a host",
        usage = "ping <host> [--count=4] [--timeout=5000]",
        examples = listOf(
            "ping google.com",
            "ping 8.8.8.8 --count=10",
            "ping 192.168.1.1 --timeout=3000"
        ),
        parameters = listOf(
            CommandParameter(
                name = "host",
                type = ParameterType.STRING,
                description = "Host to ping (IP address or domain name)",
                required = true
            ),
            CommandParameter(
                name = "count",
                type = ParameterType.INTEGER,
                description = "Number of ping packets to send",
                defaultValue = "4"
            ),
            CommandParameter(
                name = "timeout",
                type = ParameterType.INTEGER,
                description = "Timeout in milliseconds",
                defaultValue = "5000"
            )
        ),
        executor = PingCommandExecutor()
    )
    
    fun getNetstatCommand() = CommandDefinition(
        name = "netstat",
        category = CommandCategory.NET,
        description = "Show network connections and statistics",
        usage = "netstat [--listening] [--all]",
        examples = listOf(
            "netstat",
            "netstat --listening",
            "netstat --all"
        ),
        parameters = listOf(
            CommandParameter(
                name = "listening",
                type = ParameterType.BOOLEAN,
                description = "Show only listening connections",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "all",
                type = ParameterType.BOOLEAN,
                description = "Show all connections",
                defaultValue = "false"
            )
        ),
        aliases = listOf("netstat", "connections"),
        executor = NetstatCommandExecutor()
    )
    
    fun getWifiAdvancedCommand() = CommandDefinition(
        name = "wificonfig",
        category = CommandCategory.NET,
        description = "Advanced WiFi configuration and management",
        usage = "wificonfig [forget|saved|signal|channel|speed]",
        examples = listOf(
            "wificonfig saved",
            "wificonfig forget --ssid=OldNetwork",
            "wificonfig signal",
            "wificonfig channel",
            "wificonfig speed"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "WiFi configuration action",
                options = listOf("forget", "saved", "signal", "channel", "speed"),
                defaultValue = "saved"
            ),
            CommandParameter(
                name = "ssid",
                type = ParameterType.STRING,
                description = "Network SSID for forget action"
            )
        ),
        aliases = listOf("wconfig", "wifi-advanced"),
        requiresPermissions = listOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        executor = WifiAdvancedCommandExecutor()
    )
    
    fun getNetworkMonitorCommand() = CommandDefinition(
        name = "netmon",
        category = CommandCategory.NET,
        description = "Network monitoring and diagnostics",
        usage = "netmon [traffic|bandwidth|quality|history]",
        examples = listOf(
            "netmon traffic",
            "netmon bandwidth",
            "netmon quality", 
            "netmon history --hours=24"
        ),
        parameters = listOf(
            CommandParameter(
                name = "info",
                type = ParameterType.ENUM,
                description = "Type of network monitoring",
                options = listOf("traffic", "bandwidth", "quality", "history"),
                defaultValue = "traffic"
            ),
            CommandParameter(
                name = "hours",
                type = ParameterType.INTEGER,
                description = "Hours of history to show",
                defaultValue = "1"
            )
        ),
        aliases = listOf("nmon", "monitor"),
        executor = NetworkMonitorCommandExecutor()
    )
    
    fun getNetworkProfileCommand() = CommandDefinition(
        name = "netprofile",
        category = CommandCategory.NET,
        description = "Network profile management",
        usage = "netprofile [list|create|delete|switch|export]",
        examples = listOf(
            "netprofile list",
            "netprofile create --name=Home --wifi=HomeWiFi",
            "netprofile switch --name=Work",
            "netprofile export --name=Home --file=/sdcard/home.json",
            "netprofile delete --name=OldProfile"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Profile action",
                options = listOf("list", "create", "delete", "switch", "export"),
                defaultValue = "list"
            ),
            CommandParameter(
                name = "name",
                type = ParameterType.STRING,
                description = "Profile name"
            ),
            CommandParameter(
                name = "wifi",
                type = ParameterType.STRING,
                description = "WiFi SSID for profile"
            ),
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File path for export/import"
            )
        ),
        aliases = listOf("nprofile", "profiles"),
        executor = NetworkProfileCommandExecutor()
    )
}

class NetworkCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val info = parameters["info"] ?: arguments.firstOrNull() ?: "status"
        
        val output = when (info) {
            "status" -> getNetworkStatus(context)
            "interfaces" -> getNetworkInterfaces(context)
            "connections" -> getNetworkConnections(context)
            else -> "Unknown network info type: $info"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getNetworkStatus(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return buildString {
            appendLine("Network Status:")
            
            if (activeNetwork != null && networkCapabilities != null) {
                appendLine("  Status: Connected")
                
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        appendLine("  Type: WiFi")
                        appendLine("  Transport: Wireless")
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        appendLine("  Type: Mobile Data")
                        appendLine("  Transport: Cellular")
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        appendLine("  Type: Ethernet")
                        appendLine("  Transport: Wired")
                    }
                    else -> {
                        appendLine("  Type: Unknown")
                    }
                }
                
                val capabilities = mutableListOf<String>()
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    capabilities.add("Internet")
                }
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    capabilities.add("Validated")
                }
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                    capabilities.add("Unmetered")
                }
                
                if (capabilities.isNotEmpty()) {
                    appendLine("  Capabilities: ${capabilities.joinToString(", ")}")
                }
            } else {
                appendLine("  Status: Disconnected")
            }
        }
    }
    
    private fun getNetworkInterfaces(context: Context): String {
        return buildString {
            appendLine("Network Interfaces:")
            
            try {
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                interfaces?.toList()?.forEach { networkInterface ->
                    appendLine("  ${networkInterface.name}:")
                    appendLine("    Display Name: ${networkInterface.displayName}")
                    appendLine("    Up: ${networkInterface.isUp}")
                    appendLine("    Loopback: ${networkInterface.isLoopback}")
                    appendLine("    Point-to-Point: ${networkInterface.isPointToPoint}")
                    appendLine("    Virtual: ${networkInterface.isVirtual}")
                    appendLine("    MTU: ${networkInterface.mtu}")
                    
                    networkInterface.inetAddresses?.toList()?.forEach { address ->
                        appendLine("    Address: ${address.hostAddress}")
                    }
                    
                    networkInterface.hardwareAddress?.let { mac ->
                        val macString = mac.joinToString(":") { "%02x".format(it) }
                        appendLine("    MAC: $macString")
                    }
                    
                    appendLine()
                }
            } catch (e: Exception) {
                appendLine("  Error reading network interfaces: ${e.message}")
            }
        }
    }
    
    private fun getNetworkConnections(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return buildString {
            appendLine("Network Connections:")
            
            try {
                val allNetworks = connectivityManager.allNetworks
                allNetworks.forEach { network ->
                    val networkInfo = connectivityManager.getNetworkInfo(network)
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    
                    appendLine("  Network: $network")
                    appendLine("    Type: ${networkInfo?.typeName}")
                    appendLine("    State: ${networkInfo?.state}")
                    appendLine("    Detailed State: ${networkInfo?.detailedState}")
                    
                    networkCapabilities?.let { caps ->
                        val transports = mutableListOf<String>()
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) transports.add("WiFi")
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) transports.add("Cellular")
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) transports.add("Ethernet")
                        
                        if (transports.isNotEmpty()) {
                            appendLine("    Transports: ${transports.joinToString(", ")}")
                        }
                    }
                    
                    appendLine()
                }
            } catch (e: Exception) {
                appendLine("  Error reading network connections: ${e.message}")
            }
        }
    }
}

class WifiCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "status"
        
        val output = when (action) {
            "scan" -> scanWifi(context)
            "status" -> getWifiStatus(context)
            "connect" -> connectWifi(context, parameters)
            "disconnect" -> disconnectWifi(context)
            "list" -> listWifiNetworks(context)
            else -> "Unknown WiFi action: $action"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private suspend fun scanWifi(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            if (!wifiManager.isWifiEnabled) {
                "WiFi is disabled. Enable WiFi to scan for networks."
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return "Location permission (ACCESS_FINE_LOCATION) is required to scan for WiFi networks. Please grant it in app settings."
                }
                val scanResults = wifiManager.scanResults
                
                buildString {
                    appendLine("WiFi Scan Results:")
                    appendLine("Found ${scanResults.size} networks:")
                    appendLine()
                    
                    scanResults.sortedByDescending { it.level }.forEach { result ->
                        val security = getSecurityType(result)
                        val signalStrength = getSignalStrength(result.level)
                        
                        appendLine("  ${result.SSID}")
                        appendLine("    BSSID: ${result.BSSID}")
                        appendLine("    Security: $security")
                        appendLine("    Signal: $signalStrength (${result.level} dBm)")
                        appendLine("    Frequency: ${result.frequency} MHz")
                        appendLine("    Channel: ${getChannel(result.frequency)}")
                        appendLine()
                    }
                }
            }
        } catch (e: Exception) {
            "Error scanning WiFi: ${e.message}"
        }
    }
    
    private fun getWifiStatus(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return buildString {
            appendLine("WiFi Status:")
            appendLine("  WiFi Enabled: ${wifiManager.isWifiEnabled}")
            
            if (wifiManager.isWifiEnabled) {
                val wifiInfo = wifiManager.connectionInfo
                
                if (wifiInfo != null) {
                    appendLine("  Connected Network:")
                    appendLine("    SSID: ${wifiInfo.ssid}")
                    appendLine("    BSSID: ${wifiInfo.bssid}")
                    appendLine("    Signal Strength: ${getSignalStrength(wifiInfo.rssi)} (${wifiInfo.rssi} dBm)")
                    appendLine("    Link Speed: ${wifiInfo.linkSpeed} Mbps")
                    appendLine("    Frequency: ${wifiInfo.frequency} MHz")
                    appendLine("    Network ID: ${wifiInfo.networkId}")
                    appendLine("    IP Address: ${formatIpAddress(wifiInfo.ipAddress)}")
                    appendLine("    MAC Address: ${wifiInfo.macAddress}")
                }
                
                val dhcpInfo = wifiManager.dhcpInfo
                if (dhcpInfo != null) {
                    appendLine("  DHCP Info:")
                    appendLine("    Gateway: ${formatIpAddress(dhcpInfo.gateway)}")
                    appendLine("    Netmask: ${formatIpAddress(dhcpInfo.netmask)}")
                    appendLine("    DNS1: ${formatIpAddress(dhcpInfo.dns1)}")
                    appendLine("    DNS2: ${formatIpAddress(dhcpInfo.dns2)}")
                    appendLine("    Server: ${formatIpAddress(dhcpInfo.serverAddress)}")
                }
            }
        }
    }
    
    private fun connectWifi(context: Context, parameters: Map<String, String>): String {
        val ssid = parameters["ssid"] ?: return "SSID is required for WiFi connection"
        val password = parameters["password"]
        
        return "WiFi connection feature requires implementation of WifiNetworkSpecifier (Android 10+)"
    }
    
    private fun disconnectWifi(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            wifiManager.disconnect()
            "WiFi disconnected successfully"
        } catch (e: Exception) {
            "Error disconnecting WiFi: ${e.message}"
        }
    }
    
    private fun listWifiNetworks(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "Location permission (ACCESS_FINE_LOCATION) is required to list WiFi networks. Please grant it in app settings."
            }
            val configuredNetworks = wifiManager.configuredNetworks
            
            buildString {
                appendLine("Configured WiFi Networks:")
                configuredNetworks?.forEach { config ->
                    appendLine("  ${config.SSID}")
                    appendLine("    Network ID: ${config.networkId}")
                    appendLine("    Status: ${config.status}")
                    appendLine("    Priority: ${config.priority}")
                    appendLine()
                }
            }
        } catch (e: Exception) {
            "Error listing WiFi networks: ${e.message}"
        }
    }
    
    private fun getSecurityType(scanResult: ScanResult): String {
        val capabilities = scanResult.capabilities
        return when {
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") -> "WPA2"
            capabilities.contains("WPA") -> "WPA"
            capabilities.contains("WEP") -> "WEP"
            else -> "Open"
        }
    }
    
    private fun getSignalStrength(level: Int): String {
        return when {
            level >= -50 -> "Excellent"
            level >= -60 -> "Good"
            level >= -70 -> "Fair"
            level >= -80 -> "Weak"
            else -> "Very Weak"
        }
    }
    
    private fun getChannel(frequency: Int): Int {
        return when {
            frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
            frequency in 5170..5825 -> (frequency - 5000) / 5
            else -> 0
        }
    }
    
    private fun formatIpAddress(ip: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )
    }
}

class BluetoothCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "status"
        
        val output = when (action) {
            "scan" -> scanBluetooth(context)
            "status" -> getBluetoothStatus(context)
            "pair" -> pairBluetoothDevice(context, parameters)
            "unpair" -> unpairBluetoothDevice(context, parameters)
            "list" -> listBluetoothDevices(context)
            else -> "Unknown Bluetooth action: $action"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getBluetoothStatus(context: Context): String {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        return buildString {
            appendLine("Bluetooth Status:")
            
            if (bluetoothAdapter == null) {
                appendLine("  Bluetooth not supported on this device")
            } else {
                appendLine("  Bluetooth Enabled: ${bluetoothAdapter.isEnabled}")
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    appendLine("  Device Name: ${bluetoothAdapter.name}")
                    appendLine("  Device Name: ${bluetoothAdapter.name}")
                    appendLine("  Device Address: Not directly accessible due to Android API restrictions.")
                } else {
                    appendLine("  Device Name: Permission denied")
                    appendLine("  Device Address: Permission denied (BLUETOOTH_CONNECT not granted)")
                }
                appendLine("  Scan Mode: ${getScanMode(bluetoothAdapter.scanMode)}")
                appendLine("  State: ${getBluetoothState(bluetoothAdapter.state)}")
                
                if (bluetoothAdapter.isEnabled) {
                    appendLine("  Discovering: ${bluetoothAdapter.isDiscovering}")
                    
                    val bondedDevices = bluetoothAdapter.bondedDevices
                    appendLine("  Paired Devices: ${bondedDevices.size}")
                    
                    bondedDevices.take(3).forEach { device ->
                        appendLine("    ${device.name} (${device.address})")
                    }
                }
            }
        }
    }
    
    private suspend fun scanBluetooth(context: Context): String {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        return if (bluetoothAdapter == null) {
            "Bluetooth not supported on this device"
        } else if (!bluetoothAdapter.isEnabled) {
            "Bluetooth is disabled. Enable Bluetooth to scan for devices."
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return "Bluetooth scan permission (BLUETOOTH_SCAN) is required to scan for Bluetooth devices. Please grant it in app settings."
            }
            try {
                bluetoothAdapter.startDiscovery()
                "Bluetooth discovery started. This feature requires full implementation with BroadcastReceiver."
            } catch (e: Exception) {
                "Error starting Bluetooth discovery: ${e.message}"
            }
        }
    }
    
    private fun pairBluetoothDevice(context: Context, parameters: Map<String, String>): String {
        val deviceAddress = parameters["device"] ?: return "Device MAC address is required"
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        return try {
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return "Bluetooth connect permission (BLUETOOTH_CONNECT) is required to pair devices. Please grant it in app settings."
            }
            device.createBond()
            "Attempting to pair with device: ${device.name ?: deviceAddress}"
        } catch (e: Exception) {
            "Error pairing device: ${e.message}"
        }
    }
    
    private fun unpairBluetoothDevice(context: Context, parameters: Map<String, String>): String {
        val deviceAddress = parameters["device"] ?: return "Device MAC address is required"
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        return try {
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return "Bluetooth connect permission (BLUETOOTH_CONNECT) is required to unpair devices. Please grant it in app settings."
            }
            device.javaClass.getMethod("removeBond").invoke(device)
            val deviceName = if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                device.name
            } else {
                "Permission denied"
            }
            "Unpaired device: ${deviceName ?: deviceAddress}"
        } catch (e: Exception) {
            "Error unpairing device: ${e.message}"
        }
    }
    
    private fun listBluetoothDevices(context: Context): String {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        return if (bluetoothAdapter == null) {
            "Bluetooth not supported on this device"
        } else if (!bluetoothAdapter.isEnabled) {
            "Bluetooth is disabled"
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return "Bluetooth connect permission (BLUETOOTH_CONNECT) is required to list paired devices. Please grant it in app settings."
            }
            val bondedDevices = bluetoothAdapter.bondedDevices
            
            buildString {
                appendLine("Paired Bluetooth Devices:")
                if (bondedDevices.isEmpty()) {
                    appendLine("  No paired devices")
                } else {
                    bondedDevices.forEach { device ->
                        appendLine("  ${device.name ?: "Unknown"}")
                        appendLine("    Address: ${device.address}")
                        appendLine("    Bond State: ${getBondState(device.bondState)}")
                        appendLine("    Type: ${getDeviceType(device.type)}")
                        appendLine()
                    }
                }
            }
        }
    }
    
    private fun getScanMode(scanMode: Int): String {
        return when (scanMode) {
            BluetoothAdapter.SCAN_MODE_NONE -> "None"
            BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "Connectable"
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "Connectable & Discoverable"
            else -> "Unknown"
        }
    }
    
    private fun getBluetoothState(state: Int): String {
        return when (state) {
            BluetoothAdapter.STATE_OFF -> "Off"
            BluetoothAdapter.STATE_TURNING_ON -> "Turning On"
            BluetoothAdapter.STATE_ON -> "On"
            BluetoothAdapter.STATE_TURNING_OFF -> "Turning Off"
            else -> "Unknown"
        }
    }
    
    private fun getBondState(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_NONE -> "Not Bonded"
            BluetoothDevice.BOND_BONDING -> "Bonding"
            BluetoothDevice.BOND_BONDED -> "Bonded"
            else -> "Unknown"
        }
    }
    
    private fun getDeviceType(type: Int): String {
        return when (type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
            else -> "Unknown"
        }
    }
}

class PingCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val host = parameters["host"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Host is required for ping command",
            executionTimeMs = 0
        )
        
        val count = parameters["count"]?.toIntOrNull() ?: 4
        val timeout = parameters["timeout"]?.toIntOrNull() ?: 5000
        
        val output = buildString {
            appendLine("PING $host:")
            
            repeat(count) { i ->
                try {
                    val startTime = System.currentTimeMillis()
                    val address = java.net.InetAddress.getByName(host)
                    val reachable = address.isReachable(timeout)
                    val endTime = System.currentTimeMillis()
                    val time = endTime - startTime
                    
                    if (reachable) {
                        appendLine("Reply from ${address.hostAddress}: time=${time}ms")
                    } else {
                        appendLine("Request timeout for ${address.hostAddress}")
                    }
                } catch (e: Exception) {
                    appendLine("Ping failed: ${e.message}")
                }
                
                if (i < count - 1) {
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
}

class NetstatCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val listening = parameters["listening"]?.toBoolean() ?: false
        val all = parameters["all"]?.toBoolean() ?: false
        
        val output = buildString {
            appendLine("Network Statistics:")
            appendLine("(This feature requires root access or system-level implementation)")
            appendLine()
            
            // Basic network info that we can get
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            
            if (activeNetwork != null) {
                appendLine("Active Network: $activeNetwork")
                val networkInfo = connectivityManager.getNetworkInfo(activeNetwork)
                appendLine("  Type: ${networkInfo?.typeName}")
                appendLine("  State: ${networkInfo?.state}")
                appendLine("  Subtype: ${networkInfo?.subtypeName}")
            }
            
            appendLine()
            appendLine("Note: Full netstat functionality requires system-level access")
            appendLine("Use 'network status' for basic network information")
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
}

class WifiAdvancedCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "saved"
        
        val output = when (action) {
            "saved" -> getSavedNetworks(context)
            "forget" -> forgetNetwork(context, parameters)
            "signal" -> getSignalAnalysis(context)
            "channel" -> getChannelAnalysis(context)
            "speed" -> getSpeedAnalysis(context)
            else -> "Unknown WiFi config action: $action"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getSavedNetworks(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "Location permission (ACCESS_FINE_LOCATION) is required to get saved WiFi networks. Please grant it in app settings."
            }
            val configuredNetworks = wifiManager.configuredNetworks
            
            buildString {
                appendLine("Saved WiFi Networks:")
                if (configuredNetworks.isNullOrEmpty()) {
                    appendLine("  No saved networks found")
                } else {
                    appendLine("Found ${configuredNetworks.size} saved networks:")
                    appendLine()
                    
                    configuredNetworks.sortedBy { it.SSID }.forEach { config ->
                        appendLine("  Network: ${config.SSID}")
                        appendLine("    Network ID: ${config.networkId}")
                        appendLine("    Status: ${getNetworkStatus(config.status)}")
                        appendLine("    Priority: ${config.priority}")
                        appendLine("    Hidden SSID: ${config.hiddenSSID}")
                        
                        // Security type
                        val security = config.allowedKeyManagement.let { keyMgmt ->
                            when {
                                keyMgmt.get(android.net.wifi.WifiConfiguration.KeyMgmt.WPA2_PSK) -> "WPA2-PSK"
                                keyMgmt.get(android.net.wifi.WifiConfiguration.KeyMgmt.WPA_PSK) -> "WPA-PSK"
                                keyMgmt.get(android.net.wifi.WifiConfiguration.KeyMgmt.NONE) -> "Open"
                                else -> "Unknown"
                            }
                        }
                        appendLine("    Security: $security")
                        
                        appendLine()
                    }
                }
            }
        } catch (e: Exception) {
            "Error getting saved networks: ${e.message}"
        }
    }
    
    private fun forgetNetwork(context: Context, parameters: Map<String, String>): String {
        val ssid = parameters["ssid"] ?: return "SSID is required to forget a network"
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "Location permission (ACCESS_FINE_LOCATION) is required to forget WiFi networks. Please grant it in app settings."
            }
            val configuredNetworks = wifiManager.configuredNetworks
            val networkToRemove = configuredNetworks?.find { config ->
                config.SSID == "\"$ssid\"" || config.SSID == ssid
            }
            
            if (networkToRemove != null) {
                val success = wifiManager.removeNetwork(networkToRemove.networkId)
                if (success) {
                    wifiManager.saveConfiguration()
                    "Successfully forgot network: $ssid"
                } else {
                    "Failed to forget network: $ssid"
                }
            } else {
                "Network not found in saved networks: $ssid"
            }
        } catch (e: Exception) {
            "Error forgetting network: ${e.message}"
        }
    }
    
    private fun getSignalAnalysis(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            if (!wifiManager.isWifiEnabled) {
                "WiFi is disabled. Enable WiFi to analyze signal strength."
            } else {
                val wifiInfo = wifiManager.connectionInfo
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return "Location permission (ACCESS_FINE_LOCATION) is required to analyze WiFi signals. Please grant it in app settings."
                }
                val scanResults = wifiManager.scanResults
                
                buildString {
                    appendLine("WiFi Signal Analysis:")
                    appendLine()
                    
                    // Current connection analysis
                    if (wifiInfo != null && wifiInfo.ssid != "<unknown ssid>") {
                        appendLine("Current Connection:")
                        appendLine("  SSID: ${wifiInfo.ssid}")
                        appendLine("  Signal Strength: ${getDetailedSignalStrength(wifiInfo.rssi)}")
                        appendLine("  RSSI: ${wifiInfo.rssi} dBm")
                        appendLine("  Link Speed: ${wifiInfo.linkSpeed} Mbps")
                        appendLine("  Frequency: ${wifiInfo.frequency} MHz")
                        appendLine("  Channel: ${getChannel(wifiInfo.frequency)}")
                        appendLine()
                    }
                    
                    // Signal strength distribution
                    appendLine("Available Networks by Signal Strength:")
                    val signalGroups = scanResults.groupBy { getSignalCategory(it.level) }
                    
                    listOf("Excellent", "Good", "Fair", "Weak", "Very Weak").forEach { category ->
                        val networks = signalGroups[category] ?: emptyList()
                        if (networks.isNotEmpty()) {
                            appendLine("  $category (${networks.size} networks):")
                            networks.take(3).forEach { result ->
                                appendLine("    ${result.SSID} (${result.level} dBm)")
                            }
                            if (networks.size > 3) {
                                appendLine("    ... and ${networks.size - 3} more")
                            }
                            appendLine()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            "Error analyzing signal: ${e.message}"
        }
    }
    
    private fun getChannelAnalysis(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            if (!wifiManager.isWifiEnabled) {
                "WiFi is disabled. Enable WiFi to analyze channels."
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return "Location permission (ACCESS_FINE_LOCATION) is required to analyze WiFi channels. Please grant it in app settings."
                }
                val scanResults = wifiManager.scanResults
                
                buildString {
                    appendLine("WiFi Channel Analysis:")
                    appendLine()
                    
                    // Channel utilization
                    val channelUsage = scanResults.groupBy { getChannel(it.frequency) }
                        .mapValues { it.value.size }
                        .toSortedMap()
                    
                    appendLine("Channel Utilization (2.4GHz):")
                    for (channel in 1..14) {
                        val count = channelUsage[channel] ?: 0
                        val congestion = when {
                            count == 0 -> "Free"
                            count <= 2 -> "Light"
                            count <= 5 -> "Moderate"
                            count <= 10 -> "Heavy"
                            else -> "Congested"
                        }
                        appendLine("  Channel $channel: $count networks ($congestion)")
                    }
                    
                    appendLine()
                    appendLine("Channel Utilization (5GHz):")
                    val fiveGhzChannels = channelUsage.keys.filter { it > 14 }.sorted()
                    if (fiveGhzChannels.isNotEmpty()) {
                        fiveGhzChannels.forEach { channel ->
                            val count = channelUsage[channel] ?: 0
                            appendLine("  Channel $channel: $count networks")
                        }
                    } else {
                        appendLine("  No 5GHz networks detected")
                    }
                    
                    appendLine()
                    appendLine("Recommended Channels:")
                    val leastUsed2_4 = channelUsage.filter { it.key <= 14 }.minByOrNull { it.value }
                    val leastUsed5 = channelUsage.filter { it.key > 14 }.minByOrNull { it.value }
                    
                    leastUsed2_4?.let { 
                        appendLine("  2.4GHz: Channel ${it.key} (${it.value} networks)")
                    }
                    leastUsed5?.let {
                        appendLine("  5GHz: Channel ${it.key} (${it.value} networks)")
                    }
                }
            }
        } catch (e: Exception) {
            "Error analyzing channels: ${e.message}"
        }
    }
    
    private fun getSpeedAnalysis(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            if (!wifiManager.isWifiEnabled) {
                "WiFi is disabled. Enable WiFi for speed analysis."
            } else {
                val wifiInfo = wifiManager.connectionInfo
                
                buildString {
                    appendLine("WiFi Speed Analysis:")
                    appendLine()
                    
                    if (wifiInfo != null && wifiInfo.ssid != "<unknown ssid>") {
                        appendLine("Current Connection Performance:")
                        appendLine("  SSID: ${wifiInfo.ssid}")
                        appendLine("  Link Speed: ${wifiInfo.linkSpeed} Mbps")
                        appendLine("  Signal Strength: ${wifiInfo.rssi} dBm")
                        appendLine("  Frequency: ${wifiInfo.frequency} MHz")
                        
                        // Estimate theoretical max speed based on signal and frequency
                        val theoreticalSpeed = estimateMaxSpeed(wifiInfo.frequency, wifiInfo.rssi)
                        appendLine("  Estimated Max Speed: $theoreticalSpeed Mbps")
                        
                        val efficiency = (wifiInfo.linkSpeed.toDouble() / theoreticalSpeed * 100).toInt()
                        appendLine("  Connection Efficiency: $efficiency%")
                        
                        appendLine()
                        appendLine("Performance Assessment:")
                        when {
                            efficiency >= 80 -> appendLine("  ✓ Excellent connection performance")
                            efficiency >= 60 -> appendLine("  ⚠ Good connection with room for improvement")
                            efficiency >= 40 -> appendLine("  ⚠ Moderate connection quality")
                            else -> appendLine("  ⚠ Poor connection quality - consider moving closer to router")
                        }
                        
                        appendLine()
                        appendLine("Optimization Tips:")
                        if (wifiInfo.frequency < 5000) {
                            appendLine("  • Consider switching to 5GHz band for better speed")
                        }
                        if (wifiInfo.rssi < -70) {
                            appendLine("  • Move closer to the router to improve signal strength")
                        }
                        if (efficiency < 60) {
                            appendLine("  • Check for interference from other devices")
                            appendLine("  • Consider updating router firmware")
                        }
                    } else {
                        appendLine("  Not connected to any WiFi network")
                        appendLine("  Connect to a network to analyze speed performance")
                    }
                }
            }
        } catch (e: Exception) {
            "Error analyzing speed: ${e.message}"
        }
    }
    
    private fun getNetworkStatus(status: Int): String {
        return when (status) {
            android.net.wifi.WifiConfiguration.Status.CURRENT -> "Current"
            android.net.wifi.WifiConfiguration.Status.DISABLED -> "Disabled"
            android.net.wifi.WifiConfiguration.Status.ENABLED -> "Enabled"
            else -> "Unknown"
        }
    }
    
    private fun getDetailedSignalStrength(rssi: Int): String {
        return when {
            rssi >= -30 -> "Excellent (${rssi} dBm)"
            rssi >= -50 -> "Very Good (${rssi} dBm)"
            rssi >= -60 -> "Good (${rssi} dBm)"
            rssi >= -70 -> "Fair (${rssi} dBm)"
            rssi >= -80 -> "Weak (${rssi} dBm)"
            else -> "Very Weak (${rssi} dBm)"
        }
    }
    
    private fun getSignalCategory(rssi: Int): String {
        return when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good" 
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Weak"
            else -> "Very Weak"
        }
    }
    
    private fun getChannel(frequency: Int): Int {
        return when {
            frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
            frequency in 5170..5825 -> (frequency - 5000) / 5
            else -> 0
        }
    }
    
    private fun estimateMaxSpeed(frequency: Int, rssi: Int): Int {
        val baseSpeed = if (frequency > 5000) 866 else 150 // 5GHz vs 2.4GHz theoretical max
        val signalFactor = when {
            rssi >= -50 -> 1.0
            rssi >= -60 -> 0.8
            rssi >= -70 -> 0.6
            rssi >= -80 -> 0.4
            else -> 0.2
        }
        return (baseSpeed * signalFactor).toInt()
    }
}

class NetworkMonitorCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val info = parameters["info"] ?: arguments.firstOrNull() ?: "traffic"
        val hours = parameters["hours"]?.toIntOrNull() ?: 1
        
        val output = when (info) {
            "traffic" -> getNetworkTraffic(context)
            "bandwidth" -> getBandwidthInfo(context)
            "quality" -> getNetworkQuality(context)
            "history" -> getNetworkHistory(context, hours)
            else -> "Unknown network monitoring type: $info"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getNetworkTraffic(context: Context): String {
        return buildString {
            appendLine("Network Traffic Monitoring:")
            appendLine()
            
            try {
                val totalRxBytes = android.net.TrafficStats.getTotalRxBytes()
                val totalTxBytes = android.net.TrafficStats.getTotalTxBytes()
                val mobileRxBytes = android.net.TrafficStats.getMobileRxBytes()
                val mobileTxBytes = android.net.TrafficStats.getMobileTxBytes()
                
                appendLine("Total Data Usage:")
                appendLine("  Downloaded: ${formatBytes(totalRxBytes)}")
                appendLine("  Uploaded: ${formatBytes(totalTxBytes)}")
                appendLine("  Total: ${formatBytes(totalRxBytes + totalTxBytes)}")
                appendLine()
                
                appendLine("Mobile Data Usage:")
                appendLine("  Downloaded: ${formatBytes(mobileRxBytes)}")
                appendLine("  Uploaded: ${formatBytes(mobileTxBytes)}")
                appendLine("  Total: ${formatBytes(mobileRxBytes + mobileTxBytes)}")
                appendLine()
                
                val wifiRxBytes = totalRxBytes - mobileRxBytes
                val wifiTxBytes = totalTxBytes - mobileTxBytes
                appendLine("WiFi Data Usage:")
                appendLine("  Downloaded: ${formatBytes(wifiRxBytes)}")
                appendLine("  Uploaded: ${formatBytes(wifiTxBytes)}")
                appendLine("  Total: ${formatBytes(wifiRxBytes + wifiTxBytes)}")
                
            } catch (e: Exception) {
                appendLine("Error reading traffic statistics: ${e.message}")
                appendLine("Note: This feature requires network usage access")
            }
        }
    }
    
    private fun getBandwidthInfo(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return buildString {
            appendLine("Network Bandwidth Information:")
            appendLine()
            
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (networkCapabilities != null) {
                appendLine("Current Connection:")
                
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        appendLine("  Type: WiFi")
                        val wifiInfo = wifiManager.connectionInfo
                        if (wifiInfo != null) {
                            appendLine("  Link Speed: ${wifiInfo.linkSpeed} Mbps")
                            appendLine("  Frequency: ${wifiInfo.frequency} MHz")
                            appendLine("  Signal: ${wifiInfo.rssi} dBm")
                            
                            // Estimate available bandwidth based on signal quality
                            val estimatedBandwidth = estimateBandwidth(wifiInfo.linkSpeed, wifiInfo.rssi)
                            appendLine("  Estimated Available: ${estimatedBandwidth} Mbps")
                        }
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        appendLine("  Type: Mobile Data")
                        appendLine("  Estimated Speed: Variable (depends on signal and network)")
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        appendLine("  Type: Ethernet")
                        appendLine("  Typical Speed: 100-1000 Mbps")
                    }
                }
                
                // Bandwidth utilization estimates
                appendLine()
                appendLine("Bandwidth Usage Guidelines:")
                appendLine("  Video Streaming (4K): ~25 Mbps")
                appendLine("  Video Streaming (HD): ~5 Mbps")
                appendLine("  Video Calling: ~1-3 Mbps")
                appendLine("  Web Browsing: ~1-5 Mbps")
                appendLine("  File Downloads: Uses available bandwidth")
                
            } else {
                appendLine("No active network connection")
            }
        }
    }
    
    private fun getNetworkQuality(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return buildString {
            appendLine("Network Quality Assessment:")
            appendLine()
            
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (activeNetwork != null && networkCapabilities != null) {
                var qualityScore = 0
                val qualityFactors = mutableListOf<String>()
                
                // Connection type assessment
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        appendLine("Connection Type: WiFi")
                        val wifiInfo = wifiManager.connectionInfo
                        
                        if (wifiInfo != null) {
                            // Signal strength assessment
                            val signalScore = when {
                                wifiInfo.rssi >= -50 -> { qualityFactors.add("Excellent signal strength"); 25 }
                                wifiInfo.rssi >= -60 -> { qualityFactors.add("Good signal strength"); 20 }
                                wifiInfo.rssi >= -70 -> { qualityFactors.add("Fair signal strength"); 15 }
                                wifiInfo.rssi >= -80 -> { qualityFactors.add("Weak signal strength"); 10 }
                                else -> { qualityFactors.add("Very weak signal strength"); 5 }
                            }
                            qualityScore += signalScore
                            
                            // Speed assessment
                            val speedScore = when {
                                wifiInfo.linkSpeed >= 100 -> { qualityFactors.add("High speed connection"); 25 }
                                wifiInfo.linkSpeed >= 50 -> { qualityFactors.add("Good speed connection"); 20 }
                                wifiInfo.linkSpeed >= 25 -> { qualityFactors.add("Moderate speed connection"); 15 }
                                wifiInfo.linkSpeed >= 10 -> { qualityFactors.add("Basic speed connection"); 10 }
                                else -> { qualityFactors.add("Low speed connection"); 5 }
                            }
                            qualityScore += speedScore
                            
                            // Frequency band assessment
                            val freqScore = if (wifiInfo.frequency > 5000) {
                                qualityFactors.add("5GHz band (less congested)")
                                25
                            } else {
                                qualityFactors.add("2.4GHz band (more congested)")
                                15
                            }
                            qualityScore += freqScore
                        }
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        appendLine("Connection Type: Mobile Data")
                        qualityFactors.add("Mobile data connection")
                        qualityScore += 50 // Base score for mobile
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        appendLine("Connection Type: Ethernet")
                        qualityFactors.add("Wired ethernet connection")
                        qualityScore += 75 // High score for wired
                    }
                }
                
                // Capabilities assessment
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    qualityFactors.add("Internet connectivity validated")
                    qualityScore += 25
                }
                
                // Overall quality rating
                val qualityRating = when {
                    qualityScore >= 80 -> "Excellent"
                    qualityScore >= 60 -> "Good"
                    qualityScore >= 40 -> "Fair"
                    qualityScore >= 20 -> "Poor"
                    else -> "Very Poor"
                }
                
                appendLine("Overall Quality: $qualityRating ($qualityScore/100)")
                appendLine()
                appendLine("Quality Factors:")
                qualityFactors.forEach { factor ->
                    appendLine("  • $factor")
                }
                
                appendLine()
                appendLine("Recommendations:")
                when {
                    qualityScore >= 80 -> appendLine("  ✓ Your connection quality is excellent")
                    qualityScore >= 60 -> appendLine("  • Consider optimizing for even better performance")
                    qualityScore >= 40 -> appendLine("  • Try moving closer to WiFi router or switching networks")
                    else -> appendLine("  • Check network settings and consider alternative connections")
                }
                
            } else {
                appendLine("No active network connection")
                appendLine("Quality: Not Available")
            }
        }
    }
    
    private fun getNetworkHistory(context: Context, hours: Int): String {
        return buildString {
            appendLine("Network History (Last $hours hour${if (hours != 1) "s" else ""}):")
            appendLine()
            appendLine("Note: Detailed network history requires background monitoring.")
            appendLine("This feature would track:")
            appendLine("  • Connection changes and durations")
            appendLine("  • Speed variations over time")
            appendLine("  • Data usage patterns")
            appendLine("  • Network quality metrics")
            appendLine("  • Disconnection events")
            appendLine()
            appendLine("Current session summary:")
            
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connectivityManager.activeNetwork
                
                if (activeNetwork != null) {
                    val networkInfo = connectivityManager.getNetworkInfo(activeNetwork)
                    appendLine("  Active since: Session start")
                    appendLine("  Connection type: ${networkInfo?.typeName}")
                    appendLine("  Status: Connected")
                } else {
                    appendLine("  Status: Disconnected")
                }
            } catch (e: Exception) {
                appendLine("  Unable to read network status: ${e.message}")
            }
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
    
    private fun estimateBandwidth(linkSpeed: Int, rssi: Int): Int {
        val signalFactor = when {
            rssi >= -50 -> 0.9
            rssi >= -60 -> 0.7
            rssi >= -70 -> 0.5
            rssi >= -80 -> 0.3
            else -> 0.1
        }
        return (linkSpeed * signalFactor).toInt()
    }
}

class NetworkProfileCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "list"
        
        val output = when (action) {
            "list" -> listNetworkProfiles(context)
            "create" -> createNetworkProfile(context, parameters)
            "delete" -> deleteNetworkProfile(context, parameters)
            "switch" -> switchNetworkProfile(context, parameters)
            "export" -> exportNetworkProfile(context, parameters)
            else -> "Unknown network profile action: $action"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun listNetworkProfiles(context: Context): String {
        return buildString {
            appendLine("Network Profiles:")
            appendLine()
            appendLine("Note: Network profiles are a planned feature that would allow:")
            appendLine("  • Saving complete network configurations")
            appendLine("  • Quick switching between home/work/travel setups")
            appendLine("  • Automatic network selection based on location")
            appendLine("  • Backup and restore of network settings")
            appendLine()
            appendLine("Current Implementation Status:")
            appendLine("  • Profile storage: Planned")
            appendLine("  • Auto-switching: Planned") 
            appendLine("  • Cloud sync: Planned")
            appendLine()
            appendLine("Available network configurations:")
            
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    appendLine("  Location permission (ACCESS_FINE_LOCATION) is required to list network profiles. Please grant it in app settings.")
                    return@buildString
                }
                val configuredNetworks = wifiManager.configuredNetworks
                
                if (!configuredNetworks.isNullOrEmpty()) {
                    appendLine("  Saved WiFi networks: ${configuredNetworks.size}")
                    configuredNetworks.take(3).forEach { config ->
                        appendLine("    • ${config.SSID}")
                    }
                    if (configuredNetworks.size > 3) {
                        appendLine("    • ... and ${configuredNetworks.size - 3} more")
                    }
                } else {
                    appendLine("  No saved WiFi networks")
                }
            } catch (e: Exception) {
                appendLine("  Unable to read network configurations")
            }
        }
    }
    
    private fun createNetworkProfile(context: Context, parameters: Map<String, String>): String {
        val name = parameters["name"] ?: return "Profile name is required"
        val wifi = parameters["wifi"]
        
        return buildString {
            appendLine("Creating Network Profile: $name")
            appendLine()
            appendLine("Configuration:")
            appendLine("  Profile Name: $name")
            if (wifi != null) {
                appendLine("  WiFi Network: $wifi")
            }
            appendLine()
            appendLine("Note: This feature requires implementation of:")
            appendLine("  • Profile storage database")
            appendLine("  • Configuration management")
            appendLine("  • Network state persistence")
            appendLine()
            appendLine("Profile creation is simulated - use 'wificonfig saved' to see actual saved networks")
        }
    }
    
    private fun deleteNetworkProfile(context: Context, parameters: Map<String, String>): String {
        val name = parameters["name"] ?: return "Profile name is required"
        
        return "Profile deletion simulated for: $name\nUse 'wificonfig forget --ssid=NetworkName' to remove actual saved networks"
    }
    
    private fun switchNetworkProfile(context: Context, parameters: Map<String, String>): String {
        val name = parameters["name"] ?: return "Profile name is required"
        
        return buildString {
            appendLine("Switching to Network Profile: $name")
            appendLine()
            appendLine("This feature would:")
            appendLine("  • Disconnect from current networks")
            appendLine("  • Apply saved profile settings")
            appendLine("  • Connect to profile's preferred networks")
            appendLine("  • Configure network preferences")
            appendLine()
            appendLine("Current implementation: Use 'wifi connect --ssid=NetworkName' for manual connection")
        }
    }
    
    private fun exportNetworkProfile(context: Context, parameters: Map<String, String>): String {
        val name = parameters["name"] ?: return "Profile name is required"
        val file = parameters["file"] ?: "/sdcard/network_profile_$name.json"
        
        return buildString {
            appendLine("Exporting Network Profile: $name")
            appendLine("Export Location: $file")
            appendLine()
            appendLine("Export would include:")
            appendLine("  • WiFi network configurations")
            appendLine("  • Network preferences")
            appendLine("  • Connection priorities")
            appendLine("  • Security settings")
            appendLine()
            appendLine("Note: This is a simulated export.")
            appendLine("Use 'wificonfig saved' to see current network configurations that would be exported.")
        }
    }
}