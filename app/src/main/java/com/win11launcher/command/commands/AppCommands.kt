package com.win11launcher.command.commands

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.app.ActivityManager
import android.net.Uri
import android.provider.Settings
import com.win11launcher.command.*

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