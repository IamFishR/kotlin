package com.win11launcher.command.commands

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.provider.Settings
import com.win11launcher.command.*
import java.text.SimpleDateFormat
import java.util.*

object SystemCommands {
    
    fun getDeviceCommand() = CommandDefinition(
        name = "device",
        category = CommandCategory.SYSTEM,
        description = "Show device information",
        usage = "device [info|specs|build]",
        examples = listOf(
            "device",
            "device info",
            "device specs",
            "device build"
        ),
        parameters = listOf(
            CommandParameter(
                name = "type",
                type = ParameterType.ENUM,
                description = "Type of device information to show",
                options = listOf("info", "specs", "build"),
                defaultValue = "info"
            )
        ),
        aliases = listOf("dev", "deviceinfo"),
        executor = DeviceCommandExecutor()
    )
    
    fun getSystemCommand() = CommandDefinition(
        name = "system",
        category = CommandCategory.SYSTEM,
        description = "Show system information and statistics",
        usage = "system [memory|storage|battery|processes]",
        examples = listOf(
            "system",
            "system memory",
            "system storage",
            "system battery",
            "system processes"
        ),
        parameters = listOf(
            CommandParameter(
                name = "info",
                type = ParameterType.ENUM,
                description = "Type of system information to show",
                options = listOf("memory", "storage", "battery", "processes"),
                defaultValue = "memory"
            )
        ),
        aliases = listOf("sys", "sysinfo"),
        executor = SystemInfoCommandExecutor()
    )
    
    fun getMemoryCommand() = CommandDefinition(
        name = "memory",
        category = CommandCategory.SYSTEM,
        description = "Show memory usage information",
        usage = "memory [--detailed]",
        examples = listOf(
            "memory",
            "memory --detailed"
        ),
        parameters = listOf(
            CommandParameter(
                name = "detailed",
                type = ParameterType.BOOLEAN,
                description = "Show detailed memory breakdown",
                defaultValue = "false"
            )
        ),
        aliases = listOf("mem", "ram"),
        executor = MemoryCommandExecutor()
    )
    
    fun getStorageCommand() = CommandDefinition(
        name = "storage",
        category = CommandCategory.SYSTEM,
        description = "Show storage usage information",
        usage = "storage [--path=<path>]",
        examples = listOf(
            "storage",
            "storage --path=/sdcard",
            "storage --path=/data"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "Path to check storage for",
                defaultValue = "/data"
            )
        ),
        aliases = listOf("disk", "df"),
        executor = StorageCommandExecutor()
    )
    
    fun getDateCommand() = CommandDefinition(
        name = "date",
        category = CommandCategory.SYSTEM,
        description = "Show current date and time",
        usage = "date [--format=<format>]",
        examples = listOf(
            "date",
            "date --format=yyyy-MM-dd",
            "date --format=HH:mm:ss"
        ),
        parameters = listOf(
            CommandParameter(
                name = "format",
                type = ParameterType.STRING,
                description = "Date format pattern",
                defaultValue = "yyyy-MM-dd HH:mm:ss"
            )
        ),
        aliases = listOf("time", "datetime"),
        executor = DateCommandExecutor()
    )
    
    fun getVersionCommand() = CommandDefinition(
        name = "version",
        category = CommandCategory.SYSTEM,
        description = "Show launcher version information",
        usage = "version",
        examples = listOf("version"),
        aliases = listOf("ver", "v"),
        executor = VersionCommandExecutor()
    )
    
    fun getUptimeCommand() = CommandDefinition(
        name = "uptime",
        category = CommandCategory.SYSTEM,
        description = "Show system uptime",
        usage = "uptime",
        examples = listOf("uptime"),
        executor = UptimeCommandExecutor()
    )
    
    fun getSettingsCommand() = CommandDefinition(
        name = "settings",
        category = CommandCategory.SYSTEM,
        description = "Open system settings",
        usage = "settings [wifi|bluetooth|apps|display|sound|security]",
        examples = listOf(
            "settings",
            "settings wifi",
            "settings bluetooth",
            "settings apps"
        ),
        parameters = listOf(
            CommandParameter(
                name = "category",
                type = ParameterType.ENUM,
                description = "Settings category to open",
                options = listOf("wifi", "bluetooth", "apps", "display", "sound", "security"),
                required = false
            )
        ),
        executor = SettingsCommandExecutor()
    )
}

class DeviceCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val type = parameters["type"] ?: arguments.firstOrNull() ?: "info"
        
        val output = when (type) {
            "info" -> getDeviceInfo()
            "specs" -> getDeviceSpecs()
            "build" -> getBuildInfo()
            else -> "Unknown device info type: $type"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getDeviceInfo(): String {
        return buildString {
            appendLine("Device Information:")
            appendLine("  Device: ${Build.DEVICE}")
            appendLine("  Model: ${Build.MODEL}")
            appendLine("  Manufacturer: ${Build.MANUFACTURER}")
            appendLine("  Brand: ${Build.BRAND}")
            appendLine("  Product: ${Build.PRODUCT}")
            appendLine("  Hardware: ${Build.HARDWARE}")
            appendLine("  Board: ${Build.BOARD}")
        }
    }
    
    private fun getDeviceSpecs(): String {
        return buildString {
            appendLine("Device Specifications:")
            appendLine("  CPU ABI: ${Build.CPU_ABI}")
            appendLine("  CPU ABI2: ${Build.CPU_ABI2}")
            appendLine("  Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("  Supported 32-bit ABIs: ${Build.SUPPORTED_32_BIT_ABIS.joinToString(", ")}")
            appendLine("  Supported 64-bit ABIs: ${Build.SUPPORTED_64_BIT_ABIS.joinToString(", ")}")
        }
    }
    
    private fun getBuildInfo(): String {
        return buildString {
            appendLine("Build Information:")
            appendLine("  Android Version: ${Build.VERSION.RELEASE}")
            appendLine("  API Level: ${Build.VERSION.SDK_INT}")
            appendLine("  Build ID: ${Build.ID}")
            appendLine("  Build Type: ${Build.TYPE}")
            appendLine("  Build Tags: ${Build.TAGS}")
            appendLine("  Build Time: ${Build.TIME}")
            appendLine("  Build User: ${Build.USER}")
            appendLine("  Build Host: ${Build.HOST}")
            appendLine("  Bootloader: ${Build.BOOTLOADER}")
            appendLine("  Radio: ${Build.RADIO}")
        }
    }
}

class SystemInfoCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val info = parameters["info"] ?: arguments.firstOrNull() ?: "memory"
        
        val output = when (info) {
            "memory" -> getMemoryInfo(context)
            "storage" -> getStorageInfo()
            "battery" -> getBatteryInfo(context)
            "processes" -> getProcessInfo(context)
            else -> "Unknown system info type: $info"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getMemoryInfo(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        return buildString {
            appendLine("Memory Information:")
            appendLine("  Total RAM: ${formatBytes(memInfo.totalMem)}")
            appendLine("  Available RAM: ${formatBytes(memInfo.availMem)}")
            appendLine("  Used RAM: ${formatBytes(memInfo.totalMem - memInfo.availMem)}")
            appendLine("  Low Memory: ${if (memInfo.lowMemory) "Yes" else "No"}")
            appendLine("  Threshold: ${formatBytes(memInfo.threshold)}")
        }
    }
    
    private fun getStorageInfo(): String {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val externalStat = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            StatFs(Environment.getExternalStorageDirectory().path)
        } else null
        
        return buildString {
            appendLine("Storage Information:")
            appendLine("Internal Storage:")
            appendLine("  Total: ${formatBytes(internalStat.totalBytes)}")
            appendLine("  Free: ${formatBytes(internalStat.freeBytes)}")
            appendLine("  Used: ${formatBytes(internalStat.totalBytes - internalStat.freeBytes)}")
            
            if (externalStat != null) {
                appendLine("External Storage:")
                appendLine("  Total: ${formatBytes(externalStat.totalBytes)}")
                appendLine("  Free: ${formatBytes(externalStat.freeBytes)}")
                appendLine("  Used: ${formatBytes(externalStat.totalBytes - externalStat.freeBytes)}")
            }
        }
    }
    
    private fun getBatteryInfo(context: Context): String {
        return "Battery information requires battery manager implementation"
    }
    
    private fun getProcessInfo(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = activityManager.runningAppProcesses
        
        return buildString {
            appendLine("Running Processes: ${processes?.size ?: 0}")
            processes?.take(10)?.forEach { process ->
                appendLine("  ${process.processName} (PID: ${process.pid})")
            }
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
}

class MemoryCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val detailed = parameters["detailed"]?.toBoolean() ?: false
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val output = buildString {
            appendLine("Memory Usage:")
            appendLine("  Total: ${formatBytes(memInfo.totalMem)}")
            appendLine("  Available: ${formatBytes(memInfo.availMem)}")
            appendLine("  Used: ${formatBytes(memInfo.totalMem - memInfo.availMem)}")
            appendLine("  Usage: ${((memInfo.totalMem - memInfo.availMem) * 100.0 / memInfo.totalMem).toInt()}%")
            
            if (detailed) {
                appendLine("  Low Memory: ${if (memInfo.lowMemory) "Yes" else "No"}")
                appendLine("  Threshold: ${formatBytes(memInfo.threshold)}")
                
                val processes = activityManager.runningAppProcesses
                if (processes != null) {
                    appendLine("  Running Processes: ${processes.size}")
                    processes.take(5).forEach { process ->
                        appendLine("    ${process.processName}")
                    }
                }
            }
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
}

class StorageCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: "/data"
        
        val output = try {
            val stat = StatFs(path)
            buildString {
                appendLine("Storage Usage for $path:")
                appendLine("  Total: ${formatBytes(stat.totalBytes)}")
                appendLine("  Free: ${formatBytes(stat.freeBytes)}")
                appendLine("  Used: ${formatBytes(stat.totalBytes - stat.freeBytes)}")
                appendLine("  Usage: ${((stat.totalBytes - stat.freeBytes) * 100.0 / stat.totalBytes).toInt()}%")
            }
        } catch (e: Exception) {
            "Error reading storage info for $path: ${e.message}"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
}

class DateCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val format = parameters["format"] ?: "yyyy-MM-dd HH:mm:ss"
        
        val output = try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.format(Date())
        } catch (e: Exception) {
            "Invalid date format: $format"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
}

class VersionCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val output = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            buildString {
                appendLine("Win11 Launcher")
                appendLine("  Version: ${packageInfo.versionName}")
                appendLine("  Build: ${packageInfo.versionCode}")
                appendLine("  Package: ${packageInfo.packageName}")
                appendLine("  Target SDK: ${packageInfo.applicationInfo?.targetSdkVersion ?: "Unknown"}")
                appendLine("  Min SDK: ${packageInfo.applicationInfo?.minSdkVersion ?: "Unknown"}")
            }
        } catch (e: Exception) {
            "Error getting version info: ${e.message}"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
}

class UptimeCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val uptimeMillis = android.os.SystemClock.elapsedRealtime()
        val days = uptimeMillis / (1000 * 60 * 60 * 24)
        val hours = (uptimeMillis / (1000 * 60 * 60)) % 24
        val minutes = (uptimeMillis / (1000 * 60)) % 60
        val seconds = (uptimeMillis / 1000) % 60
        
        val output = buildString {
            appendLine("System Uptime:")
            if (days > 0) {
                append("  ${days}d ")
            }
            if (hours > 0) {
                append("${hours}h ")
            }
            if (minutes > 0) {
                append("${minutes}m ")
            }
            appendLine("${seconds}s")
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
}

class SettingsCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val category = parameters["category"] ?: arguments.firstOrNull()
        
        val intent = when (category) {
            "wifi" -> android.content.Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> android.content.Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "apps" -> android.content.Intent(Settings.ACTION_APPLICATION_SETTINGS)
            "display" -> android.content.Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "sound" -> android.content.Intent(Settings.ACTION_SOUND_SETTINGS)
            "security" -> android.content.Intent(Settings.ACTION_SECURITY_SETTINGS)
            else -> android.content.Intent(Settings.ACTION_SETTINGS)
        }
        
        return try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            CommandResult(
                success = true,
                output = "Opened ${category ?: "main"} settings",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Failed to open settings: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}