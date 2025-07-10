package com.win11launcher.command.commands

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.win11launcher.command.*
import java.text.SimpleDateFormat
import java.util.*

object PowerCommands {
    
    fun getPowerCommand() = CommandDefinition(
        name = "power",
        category = CommandCategory.SYSTEM,
        description = "Power management and device control",
        usage = "power [battery|lock|sleep|reboot|shutdown|optimize]",
        examples = listOf(
            "power battery",
            "power lock",
            "power sleep",
            "power optimize",
            "power reboot --confirm",
            "power shutdown --delay=30"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Power action to perform",
                options = listOf("battery", "lock", "sleep", "reboot", "shutdown", "optimize"),
                defaultValue = "battery"
            ),
            CommandParameter(
                name = "confirm",
                type = ParameterType.BOOLEAN,
                description = "Confirm dangerous actions",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "delay",
                type = ParameterType.INTEGER,
                description = "Delay in seconds for shutdown/reboot",
                defaultValue = "0"
            )
        ),
        aliases = listOf("pwr"),
        requiresPermissions = listOf(
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.REBOOT
        ),
        executor = PowerCommandExecutor()
    )
    
    fun getBatteryCommand() = CommandDefinition(
        name = "battery",
        category = CommandCategory.SYSTEM,
        description = "Detailed battery information and optimization",
        usage = "battery [status|health|usage|stats|optimization|saver]",
        examples = listOf(
            "battery",
            "battery status",
            "battery health",
            "battery usage",
            "battery stats --detailed",
            "battery saver --enable",
            "battery optimization --app=com.example.app"
        ),
        parameters = listOf(
            CommandParameter(
                name = "info",
                type = ParameterType.ENUM,
                description = "Type of battery information",
                options = listOf("status", "health", "usage", "stats", "optimization", "saver"),
                defaultValue = "status"
            ),
            CommandParameter(
                name = "detailed",
                type = ParameterType.BOOLEAN,
                description = "Show detailed information",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "enable",
                type = ParameterType.BOOLEAN,
                description = "Enable battery saver mode",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "app",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name for optimization settings"
            )
        ),
        aliases = listOf("bat", "batt"),
        executor = BatteryCommandExecutor()
    )
    
    fun getThermalCommand() = CommandDefinition(
        name = "thermal",
        category = CommandCategory.SYSTEM,
        description = "Device thermal monitoring and management",
        usage = "thermal [status|throttle|sensors]",
        examples = listOf(
            "thermal",
            "thermal status",
            "thermal throttle",
            "thermal sensors"
        ),
        parameters = listOf(
            CommandParameter(
                name = "info",
                type = ParameterType.ENUM,
                description = "Type of thermal information",
                options = listOf("status", "throttle", "sensors"),
                defaultValue = "status"
            )
        ),
        aliases = listOf("temp", "heat"),
        minApiLevel = Build.VERSION_CODES.Q,
        executor = ThermalCommandExecutor()
    )
    
    fun getScreenCommand() = CommandDefinition(
        name = "screen",
        category = CommandCategory.SYSTEM,
        description = "Screen and display power management",
        usage = "screen [on|off|brightness|timeout|rotate]",
        examples = listOf(
            "screen brightness --level=50",
            "screen timeout --minutes=5",
            "screen rotate --enable=false",
            "screen on",
            "screen off"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Screen action to perform",
                options = listOf("on", "off", "brightness", "timeout", "rotate"),
                defaultValue = "brightness"
            ),
            CommandParameter(
                name = "level",
                type = ParameterType.INTEGER,
                description = "Brightness level (0-100)",
                defaultValue = "50"
            ),
            CommandParameter(
                name = "minutes",
                type = ParameterType.INTEGER,
                description = "Screen timeout in minutes",
                defaultValue = "2"
            ),
            CommandParameter(
                name = "enable",
                type = ParameterType.BOOLEAN,
                description = "Enable/disable auto-rotate",
                defaultValue = "true"
            )
        ),
        aliases = listOf("display"),
        requiresPermissions = listOf(
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.WAKE_LOCK
        ),
        executor = ScreenCommandExecutor()
    )
}

class PowerCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "battery"
        val confirm = parameters["confirm"]?.toBoolean() ?: false
        val delay = parameters["delay"]?.toIntOrNull() ?: 0
        
        val output = when (action) {
            "battery" -> getBatteryOverview(context)
            "lock" -> lockDevice(context)
            "sleep" -> sleepDevice(context)
            "reboot" -> rebootDevice(context, confirm, delay)
            "shutdown" -> shutdownDevice(context, confirm, delay)
            "optimize" -> optimizePower(context)
            else -> "Unknown power action: $action\nAvailable actions: battery, lock, sleep, reboot, shutdown, optimize"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getBatteryOverview(context: Context): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        return buildString {
            appendLine("Power Overview:")
            
            // Battery Level
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else -1
            
            appendLine("  Battery Level: ${if (batteryPct >= 0) "$batteryPct%" else "Unknown"}")
            
            // Charging Status
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL
            appendLine("  Charging: ${if (isCharging) "Yes" else "No"}")
            
            // Power Source
            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val powerSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Battery"
            }
            appendLine("  Power Source: $powerSource")
            
            // Battery Saver
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appendLine("  Battery Saver: ${if (powerManager.isPowerSaveMode) "Enabled" else "Disabled"}")
            }
            
            // Device Idle Mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                appendLine("  Device Idle: ${if (powerManager.isDeviceIdleMode) "Yes" else "No"}")
            }
        }
    }
    
    private fun lockDevice(context: Context): String {
        return "Device locking requires Device Admin permissions, which are not supported for regular apps."
    }
    
    private fun sleepDevice(context: Context): String {
        return "Device sleep (screen off) requires Device Admin permissions, which are not supported for regular apps."
    }
    
    private fun rebootDevice(context: Context, confirm: Boolean, delay: Int): String {
        if (!confirm) {
            return "Reboot requires confirmation. Use --confirm flag to proceed.\nWarning: This will restart your device!"
        }
        
        return try {
            if (delay > 0) {
                "Reboot scheduled in $delay seconds (feature requires root access)"
            } else {
                "Rebooting device... (Requires root access or system app)"
            }
        } catch (e: SecurityException) {
            "Unable to reboot: REBOOT permission or root access required"
        } catch (e: Exception) {
            "Failed to reboot device: ${e.message}"
        }
    }
    
    private fun shutdownDevice(context: Context, confirm: Boolean, delay: Int): String {
        if (!confirm) {
            return "Shutdown requires confirmation. Use --confirm flag to proceed.\nWarning: This will turn off your device!"
        }
        
        return try {
            if (delay > 0) {
                "Shutdown scheduled in $delay seconds (feature requires root access)"
            } else {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                "Shutdown requires Android N+ or root access"
            }
        } catch (e: SecurityException) {
            "Unable to shutdown: REBOOT permission or root access required"
        } catch (e: Exception) {
            "Failed to shutdown device: ${e.message}"
        }
    }
    
    private fun optimizePower(context: Context): String {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        return buildString {
            appendLine("Power Optimization Recommendations:")
            
            // Battery Saver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!powerManager.isPowerSaveMode) {
                    appendLine("  • Enable Battery Saver mode")
                } else {
                    appendLine("  ✓ Battery Saver is already enabled")
                }
            }
            
            // Screen brightness suggestions
            appendLine("  • Reduce screen brightness")
            appendLine("  • Shorten screen timeout")
            appendLine("  • Disable unnecessary background apps")
            appendLine("  • Turn off location services when not needed")
            appendLine("  • Disable Wi-Fi and Bluetooth when not in use")
            
            appendLine("\nUse 'battery saver --enable' to enable battery saver mode")
            appendLine("Use 'screen brightness --level=30' to reduce brightness")
        }
    }
}

class BatteryCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val info = parameters["info"] ?: arguments.firstOrNull() ?: "status"
        val detailed = parameters["detailed"]?.toBoolean() ?: false
        val enable = parameters["enable"]?.toBoolean() ?: false
        val appPackage = parameters["app"]
        
        val output = when (info) {
            "status" -> getBatteryStatus(context, detailed)
            "health" -> getBatteryHealth(context)
            "usage" -> getBatteryUsage(context)
            "stats" -> getBatteryStats(context, detailed)
            "optimization" -> getBatteryOptimization(context, appPackage)
            "saver" -> manageBatterySaver(context, enable)
            else -> "Unknown battery info type: $info"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getBatteryStatus(context: Context, detailed: Boolean): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        return buildString {
            appendLine("Battery Status:")
            
            // Basic Status
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else -1
            
            appendLine("  Level: ${if (batteryPct >= 0) "$batteryPct%" else "Unknown"}")
            
            // Charging Status
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                else -> "Unknown"
            }
            appendLine("  Status: $statusText")
            
            // Power Source
            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val powerSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                0 -> "Battery"
                else -> "Unknown"
            }
            appendLine("  Power Source: $powerSource")
            
            if (detailed) {
                // Voltage
                val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
                if (voltage > 0) {
                    appendLine("  Voltage: ${voltage / 1000.0}V")
                }
                
                // Temperature
                val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
                if (temp > 0) {
                    appendLine("  Temperature: ${temp / 10.0}°C")
                }
                
                // Technology
                val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
                appendLine("  Technology: $technology")
                
                // Capacity (if available)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    if (capacity >= 0) {
                        appendLine("  Capacity: $capacity%")
                    }
                    
                    val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                    if (chargeCounter >= 0) {
                        appendLine("  Charge Counter: ${chargeCounter / 1000}mAh")
                    }
                    
                    val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    if (currentNow != Int.MIN_VALUE) {
                        appendLine("  Current: ${currentNow / 1000}mA")
                    }
                }
            }
        }
    }
    
    private fun getBatteryHealth(context: Context): String {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        return buildString {
            appendLine("Battery Health:")
            
            val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            val healthText = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Too Cold"
                else -> "Unknown"
            }
            appendLine("  Overall Health: $healthText")
            
            // Temperature check
            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (temp > 0) {
                val tempC = temp / 10.0
                appendLine("  Temperature: $tempC°C")
                when {
                    tempC > 45 -> appendLine("  ⚠️  Temperature is high - consider cooling device")
                    tempC < 0 -> appendLine("  ❄️  Temperature is very low")
                    else -> appendLine("  ✓ Temperature is normal")
                }
            }
            
            // Voltage check
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
            if (voltage > 0) {
                val voltageV = voltage / 1000.0
                appendLine("  Voltage: ${voltageV}V")
                when {
                    voltageV < 3.0 -> appendLine("  ⚠️  Low voltage - battery may be degraded")
                    voltageV > 4.5 -> appendLine("  ⚠️  High voltage - check charging system")
                    else -> appendLine("  ✓ Voltage is normal")
                }
            }
        }
    }
    
    private fun getBatteryUsage(context: Context): String {
        return buildString {
            appendLine("Battery Usage Analysis:")
            appendLine("  Recent battery usage data requires system-level access")
            appendLine("  Use Android Settings > Battery > Battery Usage for detailed analysis")
            appendLine()
            appendLine("General Usage Tips:")
            appendLine("  • Screen brightness is typically the largest battery consumer")
            appendLine("  • Background apps and location services consume significant power")
            appendLine("  • Cellular data usage (especially poor signal) drains battery")
            appendLine("  • Frequent wake-ups from notifications impact battery life")
            appendLine()
            appendLine("Use 'battery optimization' to see app-specific power management")
        }
    }
    
    private fun getBatteryStats(context: Context, detailed: Boolean): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        return buildString {
            appendLine("Battery Statistics:")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (capacity >= 0) {
                    appendLine("  Current Capacity: $capacity%")
                }
                
                val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (chargeCounter >= 0) {
                    appendLine("  Charge Counter: ${chargeCounter / 1000}mAh")
                }
                
                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (currentNow != Int.MIN_VALUE) {
                    val currentMA = currentNow / 1000
                    appendLine("  Current Draw: ${currentMA}mA")
                    
                    if (detailed && chargeCounter > 0 && currentMA > 0) {
                        val hoursRemaining = (chargeCounter / 1000.0) / currentMA
                        appendLine("  Estimated Time Remaining: ${String.format("%.1f", hoursRemaining)} hours")
                    }
                }
                
                val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                if (energyCounter >= 0) {
                    appendLine("  Energy Counter: ${energyCounter / 1000000}mWh")
                }
            }
            
            if (detailed) {
                appendLine()
                appendLine("Performance Impact:")
                appendLine("  • High current draw indicates power-hungry apps")
                appendLine("  • Rapid capacity drops suggest battery degradation")
                appendLine("  • Temperature spikes during charging are normal")
                appendLine("  • Consistent voltage drops may indicate hardware issues")
            }
        }
    }
    
    private fun getBatteryOptimization(context: Context, appPackage: String?): String {
        return buildString {
            appendLine("Battery Optimization:")
            
            if (appPackage != null) {
                appendLine("  App: $appPackage")
                appendLine("  Battery optimization settings require system-level access")
                appendLine("  Use Settings > Apps > $appPackage > Battery to modify")
            } else {
                appendLine("  Battery optimization helps extend battery life by limiting")
                appendLine("  background activity for apps that aren't frequently used.")
                appendLine()
                appendLine("  To check/modify optimization for a specific app:")
                appendLine("  battery optimization --app=com.example.app")
                appendLine()
                appendLine("  Common optimization strategies:")
                appendLine("  • Restrict background app refresh")
                appendLine("  • Limit location access to 'While Using App'")
                appendLine("  • Disable push notifications for non-essential apps")
                appendLine("  • Use adaptive battery mode")
                appendLine("  • Enable battery saver when low")
            }
        }
    }
    
    private fun manageBatterySaver(context: Context, enable: Boolean): String {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val isCurrentlyEnabled = powerManager.isPowerSaveMode
            
            if (enable && !isCurrentlyEnabled) {
                "Battery Saver mode requires user permission to enable.\nPlease enable manually in Settings > Battery > Battery Saver"
            } else if (!enable && isCurrentlyEnabled) {
                "Battery Saver mode requires user permission to disable.\nPlease disable manually in Settings > Battery > Battery Saver"
            } else if (enable && isCurrentlyEnabled) {
                "Battery Saver mode is already enabled"
            } else {
                "Battery Saver mode is already disabled"
            }
        } else {
            "Battery Saver mode requires Android 5.0+"
        }
    }
}

class ThermalCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return CommandResult(
                success = false,
                output = "Thermal monitoring requires Android 10+",
                executionTimeMs = 0
            )
        }
        
        val info = parameters["info"] ?: arguments.firstOrNull() ?: "status"
        
        val output = when (info) {
            "status" -> getThermalStatus(context)
            "throttle" -> getThrottleInfo(context)
            "sensors" -> getSensorInfo(context)
            else -> "Unknown thermal info type: $info"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun getThermalStatus(context: Context): String {
        return buildString {
            appendLine("Thermal Status:")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                
                try {
                    val thermalStatus = powerManager.currentThermalStatus
                    val statusText = when (thermalStatus) {
                        PowerManager.THERMAL_STATUS_NONE -> "Normal"
                        PowerManager.THERMAL_STATUS_LIGHT -> "Light throttling"
                        PowerManager.THERMAL_STATUS_MODERATE -> "Moderate throttling"
                        PowerManager.THERMAL_STATUS_SEVERE -> "Severe throttling"
                        PowerManager.THERMAL_STATUS_CRITICAL -> "Critical - Emergency throttling"
                        PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency - Shutdown imminent"
                        PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
                        else -> "Unknown ($thermalStatus)"
                    }
                    
                    appendLine("  Current Status: $statusText")
                    
                    when (thermalStatus) {
                        PowerManager.THERMAL_STATUS_NONE -> {
                            appendLine("  🟢 Device temperature is normal")
                        }
                        PowerManager.THERMAL_STATUS_LIGHT -> {
                            appendLine("  🟡 Light thermal throttling active")
                            appendLine("     CPU/GPU performance may be slightly reduced")
                        }
                        PowerManager.THERMAL_STATUS_MODERATE -> {
                            appendLine("  🟠 Moderate thermal throttling active")
                            appendLine("     Noticeable performance reduction")
                        }
                        PowerManager.THERMAL_STATUS_SEVERE -> {
                            appendLine("  🔴 Severe thermal throttling active")
                            appendLine("     Significant performance reduction")
                        }
                        else -> {
                            appendLine("  🚨 Critical thermal state")
                            appendLine("     Allow device to cool down immediately")
                        }
                    }
                } catch (e: Exception) {
                    appendLine("  Unable to read thermal status: ${e.message}")
                }
            }
            
            appendLine()
            appendLine("Cooling Recommendations:")
            appendLine("  • Close resource-intensive apps")
            appendLine("  • Reduce screen brightness")
            appendLine("  • Remove device from direct sunlight")
            appendLine("  • Stop charging if device is hot")
            appendLine("  • Ensure device vents are not blocked")
        }
    }
    
    private fun getThrottleInfo(context: Context): String {
        return buildString {
            appendLine("Thermal Throttling Information:")
            appendLine()
            appendLine("Throttling Levels:")
            appendLine("  NONE (0)     - Normal operation, no throttling")
            appendLine("  LIGHT (1)    - Very minor throttling, barely noticeable")
            appendLine("  MODERATE (2) - Moderate throttling, performance impact")
            appendLine("  SEVERE (3)   - Significant throttling, major performance impact")
            appendLine("  CRITICAL (4) - Critical throttling, emergency measures")
            appendLine("  EMERGENCY (5)- Emergency throttling, shutdown preparation")
            appendLine("  SHUTDOWN (6) - Thermal shutdown imminent")
            appendLine()
            appendLine("Effects of Throttling:")
            appendLine("  • CPU frequency reduction")
            appendLine("  • GPU performance scaling")
            appendLine("  • Display brightness limitation")
            appendLine("  • Charging speed reduction")
            appendLine("  • Background app restrictions")
        }
    }
    
    private fun getSensorInfo(context: Context): String {
        return buildString {
            appendLine("Thermal Sensor Information:")
            appendLine()
            
            // Get battery temperature (most commonly available)
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            
            if (temp > 0) {
                val tempC = temp / 10.0
                appendLine("  Battery Temperature: $tempC°C")
                
                when {
                    tempC > 45 -> appendLine("    🔴 High - Allow device to cool")
                    tempC > 35 -> appendLine("    🟡 Warm - Monitor temperature")
                    tempC < 0 -> appendLine("    ❄️ Very cold - May affect battery performance")
                    else -> appendLine("    🟢 Normal operating temperature")
                }
            }
            
            appendLine()
            appendLine("Note: Additional thermal sensors require system-level access")
            appendLine("      Available sensors vary by device manufacturer")
        }
    }
}

class ScreenCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "brightness"
        
        val output = when (action) {
            "on" -> turnScreenOn(context)
            "off" -> turnScreenOff(context)
            "brightness" -> adjustBrightness(context, parameters)
            "timeout" -> adjustTimeout(context, parameters)
            "rotate" -> adjustRotation(context, parameters)
            else -> "Unknown screen action: $action"
        }
        
        return CommandResult(
            success = true,
            output = output,
            executionTimeMs = 0
        )
    }
    
    private fun turnScreenOn(context: Context): String {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "CommandLine::ScreenOn"
            )
            wakeLock.acquire(5000)
            wakeLock.release()
            "Screen turned on"
        } catch (e: Exception) {
            "Failed to turn on screen: ${e.message}"
        }
    }
    
    private fun turnScreenOff(context: Context): String {
        return try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()
            "Screen turned off (device locked)"
        } catch (e: SecurityException) {
            "Unable to turn off screen: Device admin permission required"
        } catch (e: Exception) {
            "Failed to turn off screen: ${e.message}"
        }
    }
    
    private fun adjustBrightness(context: Context, parameters: Map<String, String>): String {
        val level = parameters["level"]?.toIntOrNull() ?: 50
        
        if (level < 0 || level > 100) {
            return "Brightness level must be between 0 and 100"
        }
        
        return try {
            // Check if we can modify system settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return "Unable to adjust brightness: WRITE_SETTINGS permission required\n" +
                           "Please enable 'Modify system settings' for this app in Android Settings"
                }
            }
            
            // Convert percentage to system brightness value (0-255)
            val brightnessValue = (level * 255 / 100).coerceIn(0, 255)
            
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            
            "Screen brightness set to $level%"
        } catch (e: SecurityException) {
            "Unable to adjust brightness: WRITE_SETTINGS permission required"
        } catch (e: Exception) {
            "Failed to adjust brightness: ${e.message}"
        }
    }
    
    private fun adjustTimeout(context: Context, parameters: Map<String, String>): String {
        val minutes = parameters["minutes"]?.toIntOrNull() ?: 2
        
        if (minutes < 1 || minutes > 30) {
            return "Screen timeout must be between 1 and 30 minutes"
        }
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return "Unable to adjust timeout: WRITE_SETTINGS permission required\n" +
                           "Please enable 'Modify system settings' for this app in Android Settings"
                }
            }
            
            val timeoutMs = minutes * 60 * 1000
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutMs
            )
            
            "Screen timeout set to $minutes minute${if (minutes != 1) "s" else ""}"
        } catch (e: SecurityException) {
            "Unable to adjust timeout: WRITE_SETTINGS permission required"
        } catch (e: Exception) {
            "Failed to adjust timeout: ${e.message}"
        }
    }
    
    private fun adjustRotation(context: Context, parameters: Map<String, String>): String {
        val enable = parameters["enable"]?.toBoolean() ?: true
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return "Unable to adjust rotation: WRITE_SETTINGS permission required\n" +
                           "Please enable 'Modify system settings' for this app in Android Settings"
                }
            }
            
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enable) 1 else 0
            )
            
            "Auto-rotation ${if (enable) "enabled" else "disabled"}"
        } catch (e: SecurityException) {
            "Unable to adjust rotation: WRITE_SETTINGS permission required"
        } catch (e: Exception) {
            "Failed to adjust rotation: ${e.message}"
        }
    }
}