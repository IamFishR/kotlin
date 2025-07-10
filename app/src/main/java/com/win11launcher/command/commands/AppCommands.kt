package com.win11launcher.command.commands

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.app.ActivityManager
import android.net.Uri
import android.provider.Settings
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import android.os.Build
import android.app.AppOpsManager
import android.provider.Settings.Secure
import androidx.annotation.RequiresApi
import com.win11launcher.command.*
import java.util.*
import java.io.File

object AppCommands {
    
    fun getLaunchCommand() = CommandDefinition(
        name = "launch",
        category = CommandCategory.APP,
        description = "Launch an application",
        usage = "launch <package_name>",
        examples = listOf(
            "launch com.android.chrome",
            "launch com.whatsapp",
            "launch com.spotify.music"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application to launch",
                required = true
            )
        ),
        aliases = listOf("start", "open", "run"),
        executor = LaunchCommandExecutor()
    )
    
    fun getKillCommand() = CommandDefinition(
        name = "kill",
        category = CommandCategory.APP,
        description = "Kill a running application",
        usage = "kill <package_name>",
        examples = listOf(
            "kill com.android.chrome",
            "kill com.whatsapp",
            "kill --all"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application to kill",
                required = false
            ),
            CommandParameter(
                name = "all",
                type = ParameterType.BOOLEAN,
                description = "Kill all background apps",
                defaultValue = "false"
            )
        ),
        aliases = listOf("stop", "terminate"),
        requiresPermissions = listOf("android.permission.KILL_BACKGROUND_PROCESSES"),
        executor = KillCommandExecutor()
    )
    
    fun getAppsCommand() = CommandDefinition(
        name = "apps",
        category = CommandCategory.APP,
        description = "List installed applications",
        usage = "apps [--system] [--user] [--running] [--disabled]",
        examples = listOf(
            "apps",
            "apps --system",
            "apps --user",
            "apps --running",
            "apps --disabled"
        ),
        parameters = listOf(
            CommandParameter(
                name = "system",
                type = ParameterType.BOOLEAN,
                description = "Show system apps",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "user",
                type = ParameterType.BOOLEAN,
                description = "Show user apps",
                defaultValue = "true"
            ),
            CommandParameter(
                name = "running",
                type = ParameterType.BOOLEAN,
                description = "Show only running apps",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "disabled",
                type = ParameterType.BOOLEAN,
                description = "Show disabled apps",
                defaultValue = "false"
            )
        ),
        aliases = listOf("list-apps", "packages"),
        executor = AppsCommandExecutor()
    )
    
    fun getAppInfoCommand() = CommandDefinition(
        name = "appinfo",
        category = CommandCategory.APP,
        description = "Show detailed information about an application",
        usage = "appinfo <package_name>",
        examples = listOf(
            "appinfo com.android.chrome",
            "appinfo com.whatsapp",
            "appinfo com.spotify.music"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application",
                required = true
            )
        ),
        aliases = listOf("info", "package-info"),
        executor = AppInfoCommandExecutor()
    )
    
    fun getUninstallCommand() = CommandDefinition(
        name = "uninstall",
        category = CommandCategory.APP,
        description = "Uninstall an application",
        usage = "uninstall <package_name>",
        examples = listOf(
            "uninstall com.example.app",
            "uninstall com.unwanted.app"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application to uninstall",
                required = true
            )
        ),
        aliases = listOf("remove", "delete"),
        executor = UninstallCommandExecutor()
    )
    
    fun getInstallCommand() = CommandDefinition(
        name = "install",
        category = CommandCategory.APP,
        description = "Install an APK file",
        usage = "install <apk_path>",
        examples = listOf(
            "install /sdcard/Download/app.apk",
            "install /storage/emulated/0/app.apk"
        ),
        parameters = listOf(
            CommandParameter(
                name = "apk_path",
                type = ParameterType.PATH,
                description = "Path to the APK file to install",
                required = true
            )
        ),
        executor = InstallCommandExecutor()
    )
    
    fun getClearCommand() = CommandDefinition(
        name = "clear-data",
        category = CommandCategory.APP,
        description = "Clear application data and cache",
        usage = "clear-data <package_name> [--cache-only]",
        examples = listOf(
            "clear-data com.android.chrome",
            "clear-data com.whatsapp --cache-only"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application",
                required = true
            ),
            CommandParameter(
                name = "cache-only",
                type = ParameterType.BOOLEAN,
                description = "Clear only cache, not user data",
                defaultValue = "false"
            )
        ),
        aliases = listOf("clear-cache", "clean"),
        executor = ClearDataCommandExecutor()
    )
    
    fun getPermissionsCommand() = CommandDefinition(
        name = "permissions",
        category = CommandCategory.APP,
        description = "Show application permissions",
        usage = "permissions <package_name>",
        examples = listOf(
            "permissions com.android.chrome",
            "permissions com.whatsapp"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name of the application",
                required = true
            )
        ),
        aliases = listOf("perms", "app-permissions"),
        executor = PermissionsCommandExecutor()
    )
    
    // Advanced App Management Commands for Phase 4
    
    fun getAppMonCommand() = CommandDefinition(
        name = "appmon",
        category = CommandCategory.APP,
        description = "Real-time application monitoring and analysis",
        usage = "appmon <package_name> [--time=<minutes>] [--detailed]",
        examples = listOf(
            "appmon com.android.chrome",
            "appmon com.whatsapp --time=5 --detailed",
            "appmon com.spotify.music --time=10"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name to monitor",
                required = true
            ),
            CommandParameter(
                name = "time",
                type = ParameterType.INTEGER,
                description = "Monitoring duration in minutes",
                defaultValue = "1"
            ),
            CommandParameter(
                name = "detailed",
                type = ParameterType.BOOLEAN,
                description = "Show detailed monitoring information",
                defaultValue = "false"
            )
        ),
        aliases = listOf("monitor-app", "app-monitor"),
        executor = AppMonCommandExecutor()
    )
    
    fun getAppStatsCommand() = CommandDefinition(
        name = "appstats",
        category = CommandCategory.APP,
        description = "Application usage statistics and analytics",
        usage = "appstats [--usage] [--performance] [--days=<days>] [--package=<name>]",
        examples = listOf(
            "appstats --usage --days=7",
            "appstats --performance --package=com.android.chrome",
            "appstats --usage --performance --days=30"
        ),
        parameters = listOf(
            CommandParameter(
                name = "usage",
                type = ParameterType.BOOLEAN,
                description = "Show usage statistics",
                defaultValue = "true"
            ),
            CommandParameter(
                name = "performance",
                type = ParameterType.BOOLEAN,
                description = "Show performance statistics",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "days",
                type = ParameterType.INTEGER,
                description = "Number of days to analyze",
                defaultValue = "7"
            ),
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Specific package to analyze",
                required = false
            )
        ),
        aliases = listOf("app-stats", "usage-stats"),
        requiresPermissions = listOf("android.permission.PACKAGE_USAGE_STATS"),
        executor = AppStatsCommandExecutor()
    )
    
    fun getPermCheckCommand() = CommandDefinition(
        name = "permcheck",
        category = CommandCategory.APP,
        description = "Advanced permission auditing and analysis",
        usage = "permcheck [--dangerous] [--unused] [--system] [--package=<name>]",
        examples = listOf(
            "permcheck --dangerous",
            "permcheck --unused --package=com.example.app",
            "permcheck --system --dangerous"
        ),
        parameters = listOf(
            CommandParameter(
                name = "dangerous",
                type = ParameterType.BOOLEAN,
                description = "Show only dangerous permissions",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "unused",
                type = ParameterType.BOOLEAN,
                description = "Show unused permissions",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "system",
                type = ParameterType.BOOLEAN,
                description = "Include system apps",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Specific package to check",
                required = false
            )
        ),
        aliases = listOf("perm-audit", "permission-check"),
        executor = PermCheckCommandExecutor()
    )
    
    fun getPermGrantCommand() = CommandDefinition(
        name = "permgrant",
        category = CommandCategory.APP,
        description = "Grant runtime permission to application",
        usage = "permgrant <package_name> <permission>",
        examples = listOf(
            "permgrant com.example.app android.permission.CAMERA",
            "permgrant com.whatsapp android.permission.READ_CONTACTS"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name",
                required = true
            ),
            CommandParameter(
                name = "permission",
                type = ParameterType.STRING,
                description = "Permission name to grant",
                required = true
            )
        ),
        aliases = listOf("grant-permission", "perm-grant"),
        requiresPermissions = listOf("android.permission.GRANT_RUNTIME_PERMISSIONS"),
        executor = PermGrantCommandExecutor()
    )
    
    fun getPermRevokeCommand() = CommandDefinition(
        name = "permrevoke",
        category = CommandCategory.APP,
        description = "Revoke runtime permission from application",
        usage = "permrevoke <package_name> <permission>",
        examples = listOf(
            "permrevoke com.example.app android.permission.CAMERA",
            "permrevoke com.whatsapp android.permission.LOCATION"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name",
                required = true
            ),
            CommandParameter(
                name = "permission",
                type = ParameterType.STRING,
                description = "Permission name to revoke",
                required = true
            )
        ),
        aliases = listOf("revoke-permission", "perm-revoke"),
        requiresPermissions = listOf("android.permission.REVOKE_RUNTIME_PERMISSIONS"),
        executor = PermRevokeCommandExecutor()
    )
    
    fun getAppCleanupCommand() = CommandDefinition(
        name = "appcleanup",
        category = CommandCategory.APP,
        description = "Smart application cleanup and optimization",
        usage = "appcleanup [--aggressive] [--dry-run] [--size-threshold=<mb>]",
        examples = listOf(
            "appcleanup --dry-run",
            "appcleanup --aggressive --size-threshold=100",
            "appcleanup"
        ),
        parameters = listOf(
            CommandParameter(
                name = "aggressive",
                type = ParameterType.BOOLEAN,
                description = "Enable aggressive cleanup mode",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "dry-run",
                type = ParameterType.BOOLEAN,
                description = "Show what would be cleaned without doing it",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "size-threshold",
                type = ParameterType.INTEGER,
                description = "Size threshold in MB for cleanup",
                defaultValue = "50"
            )
        ),
        aliases = listOf("app-cleanup", "cleanup-apps"),
        executor = AppCleanupCommandExecutor()
    )
    
    fun getAppDepsCommand() = CommandDefinition(
        name = "appdeps",
        category = CommandCategory.APP,
        description = "Application dependency analysis",
        usage = "appdeps <package_name> [--reverse] [--detailed]",
        examples = listOf(
            "appdeps com.android.chrome",
            "appdeps com.whatsapp --reverse --detailed"
        ),
        parameters = listOf(
            CommandParameter(
                name = "package",
                type = ParameterType.PACKAGE_NAME,
                description = "Package name to analyze",
                required = true
            ),
            CommandParameter(
                name = "reverse",
                type = ParameterType.BOOLEAN,
                description = "Show reverse dependencies",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "detailed",
                type = ParameterType.BOOLEAN,
                description = "Show detailed dependency information",
                defaultValue = "false"
            )
        ),
        aliases = listOf("app-dependencies", "dependencies"),
        executor = AppDepsCommandExecutor()
    )
}

class LaunchCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        return try {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                
                CommandResult(
                    success = true,
                    output = "Successfully launched $packageName",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Could not find launch intent for $packageName",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error launching $packageName: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class KillCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull()
        val killAll = parameters["all"]?.toBoolean() ?: false
        
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            if (killAll) {
                // Note: This requires system-level permissions
                activityManager.killBackgroundProcesses(context.packageName)
                CommandResult(
                    success = true,
                    output = "Attempted to kill background processes",
                    executionTimeMs = 0
                )
            } else if (packageName != null) {
                activityManager.killBackgroundProcesses(packageName)
                CommandResult(
                    success = true,
                    output = "Attempted to kill $packageName",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Package name is required (or use --all)",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error killing process: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class AppsCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val showSystem = parameters["system"]?.toBoolean() ?: false
        val showUser = parameters["user"]?.toBoolean() ?: true
        val showRunning = parameters["running"]?.toBoolean() ?: false
        val showDisabled = parameters["disabled"]?.toBoolean() ?: false
        
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val filteredApps = installedApps.filter { app ->
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isEnabled = app.enabled
                
                when {
                    showRunning -> {
                        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val runningProcesses = activityManager.runningAppProcesses
                        runningProcesses?.any { it.processName == app.packageName } == true
                    }
                    showDisabled -> !isEnabled
                    showSystem -> isSystem
                    showUser -> !isSystem
                    else -> true
                }
            }
            
            val output = buildString {
                appendLine("Installed Applications (${filteredApps.size}):")
                appendLine()
                
                filteredApps.sortedBy { it.loadLabel(packageManager).toString() }.forEach { app ->
                    val appName = app.loadLabel(packageManager).toString()
                    val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val status = if (app.enabled) "Enabled" else "Disabled"
                    
                    appendLine("  $appName")
                    appendLine("    Package: ${app.packageName}")
                    appendLine("    Type: ${if (isSystem) "System" else "User"}")
                    appendLine("    Status: $status")
                    appendLine("    Target SDK: ${app.targetSdkVersion}")
                    appendLine()
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error listing apps: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class AppInfoCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val applicationInfo = packageInfo.applicationInfo
            
            val output = buildString {
                appendLine("Application Information:")
                appendLine("  Name: ${applicationInfo?.loadLabel(packageManager) ?: "Unknown"}")
                appendLine("  Package: ${packageInfo.packageName}")
                appendLine("  Version: ${packageInfo.versionName ?: "Unknown"}")
                appendLine("  Version Code: ${packageInfo.versionCode}")
                appendLine("  Target SDK: ${applicationInfo?.targetSdkVersion ?: "Unknown"}")
                appendLine("  Min SDK: ${applicationInfo?.minSdkVersion ?: "Unknown"}")
                appendLine("  Install Location: ${packageInfo.installLocation}")
                appendLine("  First Install: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(packageInfo.firstInstallTime)}")
                appendLine("  Last Update: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(packageInfo.lastUpdateTime)}")
                appendLine("  Data Directory: ${applicationInfo?.dataDir ?: "Unknown"}")
                appendLine("  Public Source Directory: ${applicationInfo?.publicSourceDir ?: "Unknown"}")
                appendLine("  Enabled: ${applicationInfo?.enabled ?: false}")
                appendLine("  System App: ${applicationInfo?.let { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 } ?: false}")
                appendLine("  Debuggable: ${applicationInfo?.let { (it.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 } ?: false}")
                
                // File size information
                try {
                    val publicSourceDir = applicationInfo?.publicSourceDir
                    if (publicSourceDir != null) {
                        val file = java.io.File(publicSourceDir)
                        appendLine("  APK Size: ${formatFileSize(file.length())}")
                    } else {
                        appendLine("  APK Size: Unable to determine")
                    }
                } catch (e: Exception) {
                    appendLine("  APK Size: Unable to determine")
                }
                
                // Permissions
                val permissions = packageInfo.requestedPermissions
                if (permissions != null && permissions.isNotEmpty()) {
                    appendLine("  Permissions (${permissions.size}):")
                    permissions.take(10).forEach { permission ->
                        appendLine("    $permission")
                    }
                    if (permissions.size > 10) {
                        appendLine("    ... and ${permissions.size - 10} more")
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: PackageManager.NameNotFoundException) {
            CommandResult(
                success = false,
                output = "Package not found: $packageName",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error getting app info: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = size.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
}

class UninstallCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            
            CommandResult(
                success = true,
                output = "Opened uninstall dialog for $packageName",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error opening uninstall dialog: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class InstallCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val apkPath = parameters["apk_path"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "APK path is required",
            executionTimeMs = 0
        )
        
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://$apkPath"), "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            
            CommandResult(
                success = true,
                output = "Opened install dialog for $apkPath",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error opening install dialog: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class ClearDataCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        val cacheOnly = parameters["cache-only"]?.toBoolean() ?: false
        
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            
            val action = if (cacheOnly) "cache clearing" else "data clearing"
            CommandResult(
                success = true,
                output = "Opened app settings for $packageName. Use the $action option in the settings.",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error opening app settings: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class PermissionsCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions
            
            val output = buildString {
                appendLine("Permissions for $packageName:")
                appendLine()
                
                if (permissions == null || permissions.isEmpty()) {
                    appendLine("  No permissions requested")
                } else {
                    permissions.forEachIndexed { index, permission ->
                        val permissionInfo = try {
                            packageManager.getPermissionInfo(permission, 0)
                        } catch (e: Exception) {
                            null
                        }
                        
                        appendLine("  ${index + 1}. $permission")
                        
                        if (permissionInfo != null) {
                            val protectionLevel = when (permissionInfo.protectionLevel) {
                                android.content.pm.PermissionInfo.PROTECTION_NORMAL -> "Normal"
                                android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> "Dangerous"
                                android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> "Signature"
                                else -> "System"
                            }
                            appendLine("     Protection Level: $protectionLevel")
                            
                            val description = permissionInfo.loadDescription(packageManager)
                            if (description != null) {
                                appendLine("     Description: $description")
                            }
                        }
                        
                        // Check if permission is granted
                        val granted = packageManager.checkPermission(permission, packageName) == PackageManager.PERMISSION_GRANTED
                        appendLine("     Status: ${if (granted) "Granted" else "Not Granted"}")
                        appendLine()
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: PackageManager.NameNotFoundException) {
            CommandResult(
                success = false,
                output = "Package not found: $packageName",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error getting permissions: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

// Advanced App Management Command Executors for Phase 4

class AppMonCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.firstOrNull() ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        val timeMinutes = parameters["time"]?.toIntOrNull() ?: 1
        val detailed = parameters["detailed"]?.toBoolean() ?: false
        
        return try {
            val packageManager = context.packageManager
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            // Check if app exists
            val packageInfo = try {
                packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Package not found: $packageName",
                    executionTimeMs = 0
                )
            }
            
            val output = buildString {
                appendLine("📱 Real-time App Monitoring: ${packageInfo.applicationInfo?.loadLabel(packageManager)}")
                appendLine("Package: $packageName")
                appendLine("Monitoring Duration: $timeMinutes minute(s)")
                appendLine("=".repeat(50))
                appendLine()
                
                // Get current process information
                val runningProcesses = activityManager.runningAppProcesses
                val targetProcess = runningProcesses?.find { it.processName == packageName }
                
                if (targetProcess != null) {
                    appendLine("🟢 Process Status: RUNNING")
                    appendLine("Process ID: ${targetProcess.pid}")
                    appendLine("Importance: ${getProcessImportance(targetProcess.importance)}")
                    appendLine()
                    
                    if (detailed) {
                        // Memory information
                        val memoryInfo = ActivityManager.MemoryInfo()
                        activityManager.getMemoryInfo(memoryInfo)
                        
                        appendLine("💾 Memory Information:")
                        appendLine("  Total RAM: ${formatMemory(memoryInfo.totalMem)}")
                        appendLine("  Available RAM: ${formatMemory(memoryInfo.availMem)}")
                        appendLine("  Low Memory: ${if (memoryInfo.lowMemory) "Yes" else "No"}")
                        appendLine("  Memory Threshold: ${formatMemory(memoryInfo.threshold)}")
                        appendLine()
                        
                        // App-specific memory (requires API 23+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(targetProcess.pid))
                                if (processMemoryInfo.isNotEmpty()) {
                                    val memInfo = processMemoryInfo[0]
                                    appendLine("📊 Process Memory Usage:")
                                    appendLine("  PSS (Proportional Set Size): ${memInfo.totalPss} KB")
                                    appendLine("  Private Dirty: ${memInfo.totalPrivateDirty} KB")
                                    appendLine("  Shared Dirty: ${memInfo.totalSharedDirty} KB")
                                    appendLine()
                                }
                            } catch (e: Exception) {
                                appendLine("📊 Process Memory Usage: Unable to retrieve")
                                appendLine()
                            }
                        }
                    }
                    
                    // Usage statistics (requires PACKAGE_USAGE_STATS permission)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                            val endTime = System.currentTimeMillis()
                            val startTime = endTime - (24 * 60 * 60 * 1000) // Last 24 hours
                            
                            val usageStats = usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_DAILY,
                                startTime,
                                endTime
                            )
                            
                            val appUsage = usageStats.find { it.packageName == packageName }
                            if (appUsage != null) {
                                appendLine("📈 Usage Statistics (Last 24h):")
                                appendLine("  Total Time in Foreground: ${formatDuration(appUsage.totalTimeInForeground)}")
                                appendLine("  Last Time Used: ${formatTimestamp(appUsage.lastTimeUsed)}")
                                appendLine("  First Time Stamp: ${formatTimestamp(appUsage.firstTimeStamp)}")
                                appendLine()
                            }
                        } catch (e: Exception) {
                            if (detailed) {
                                appendLine("📈 Usage Statistics: Permission required (PACKAGE_USAGE_STATS)")
                                appendLine()
                            }
                        }
                    }
                    
                } else {
                    appendLine("🔴 Process Status: NOT RUNNING")
                    appendLine()
                    
                    // Try to get last known usage
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && detailed) {
                        try {
                            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                            val endTime = System.currentTimeMillis()
                            val startTime = endTime - (7 * 24 * 60 * 60 * 1000) // Last 7 days
                            
                            val usageStats = usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_WEEKLY,
                                startTime,
                                endTime
                            )
                            
                            val appUsage = usageStats.find { it.packageName == packageName }
                            if (appUsage != null) {
                                appendLine("📈 Recent Usage (Last 7 days):")
                                appendLine("  Total Time: ${formatDuration(appUsage.totalTimeInForeground)}")
                                appendLine("  Last Used: ${formatTimestamp(appUsage.lastTimeUsed)}")
                                appendLine()
                            }
                        } catch (e: Exception) {
                            // Ignore permission errors for non-detailed mode
                        }
                    }
                }
                
                // App information
                val appInfo = packageInfo.applicationInfo
                appendLine("ℹ️ Application Information:")
                appendLine("  Version: ${packageInfo.versionName} (${packageInfo.versionCode})")
                appendLine("  Target SDK: ${appInfo?.targetSdkVersion}")
                appendLine("  Enabled: ${appInfo?.enabled}")
                appendLine("  System App: ${appInfo?.let { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 }}")
                
                if (detailed) {
                    appendLine("  Data Directory: ${appInfo?.dataDir}")
                    try {
                        val apkFile = File(appInfo?.publicSourceDir ?: "")
                        appendLine("  APK Size: ${formatFileSize(apkFile.length())}")
                    } catch (e: Exception) {
                        appendLine("  APK Size: Unable to determine")
                    }
                }
                
                appendLine()
                appendLine("✅ Monitoring completed successfully")
                if (timeMinutes > 1) {
                    appendLine("Note: Extended monitoring would track changes over ${timeMinutes} minutes")
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error monitoring app: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun getProcessImportance(importance: Int): String {
        return when (importance) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "Foreground"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE -> "Foreground Service"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING -> "Top Sleeping"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "Visible"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE -> "Perceptible"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE -> "Can't Save State"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "Service"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "Cached"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE -> "Gone"
            else -> "Unknown ($importance)"
        }
    }
    
    private fun formatMemory(bytes: Long): String {
        val mb = bytes / (1024 * 1024)
        val gb = mb / 1024.0
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            "$mb MB"
        }
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
    }
    
    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = size.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", value, units[unitIndex])
    }
}