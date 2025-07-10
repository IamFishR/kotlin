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
            Manifest.permission.ACCESS_FINE_LOCATION
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
                appendLine("  Device Name: ${bluetoothAdapter.name}")
                appendLine("  Device Address: ${bluetoothAdapter.address}")
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
            device.javaClass.getMethod("removeBond").invoke(device)
            "Unpaired device: ${device.name ?: deviceAddress}"
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