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

// Additional App Management Command Executors for Phase 4

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AppStatsCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val showUsage = parameters["usage"]?.toBoolean() ?: true
        val showPerformance = parameters["performance"]?.toBoolean() ?: false
        val days = parameters["days"]?.toIntOrNull() ?: 7
        val specificPackage = parameters["package"]
        
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
            
            val interval = when {
                days <= 1 -> UsageStatsManager.INTERVAL_DAILY
                days <= 7 -> UsageStatsManager.INTERVAL_WEEKLY
                days <= 30 -> UsageStatsManager.INTERVAL_MONTHLY
                else -> UsageStatsManager.INTERVAL_YEARLY
            }
            
            val usageStats = usageStatsManager.queryUsageStats(interval, startTime, endTime)
            
            val filteredStats = if (specificPackage != null) {
                usageStats.filter { it.packageName == specificPackage }
            } else {
                usageStats.filter { it.totalTimeInForeground > 0 }
            }
            
            val output = buildString {
                appendLine("📊 Application Usage Statistics")
                appendLine("Period: Last $days day(s)")
                if (specificPackage != null) {
                    appendLine("Package: $specificPackage")
                }
                appendLine("=".repeat(50))
                appendLine()
                
                if (filteredStats.isEmpty()) {
                    appendLine("No usage data available for the specified period.")
                    appendLine("Note: Usage stats permission may be required.")
                    return@buildString
                }
                
                if (showUsage) {
                    appendLine("📱 Usage Summary:")
                    appendLine()
                    
                    val sortedStats = filteredStats.sortedByDescending { it.totalTimeInForeground }
                    
                    sortedStats.take(10).forEachIndexed { index, stats ->
                        val appName = try {
                            val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
                            appInfo.loadLabel(packageManager).toString()
                        } catch (e: Exception) {
                            stats.packageName
                        }
                        
                        val timeInForeground = formatDuration(stats.totalTimeInForeground)
                        val lastUsed = formatTimestamp(stats.lastTimeUsed)
                        
                        appendLine("  ${index + 1}. $appName")
                        appendLine("     Package: ${stats.packageName}")
                        appendLine("     Usage Time: $timeInForeground")
                        appendLine("     Last Used: $lastUsed")
                        appendLine("     Launch Count: ${stats.totalTimeInForeground / 60000}+ times")
                        appendLine()
                    }
                    
                    if (sortedStats.size > 10) {
                        appendLine("  ... and ${sortedStats.size - 10} more apps")
                        appendLine()
                    }
                    
                    // Usage insights
                    val totalUsageTime = sortedStats.sumOf { it.totalTimeInForeground }
                    val avgUsagePerApp = if (sortedStats.isNotEmpty()) totalUsageTime / sortedStats.size else 0
                    
                    appendLine("📈 Usage Insights:")
                    appendLine("  Total Screen Time: ${formatDuration(totalUsageTime)}")
                    appendLine("  Active Apps: ${sortedStats.size}")
                    appendLine("  Average per App: ${formatDuration(avgUsagePerApp)}")
                    
                    val topApp = sortedStats.firstOrNull()
                    if (topApp != null) {
                        val percentage = (topApp.totalTimeInForeground * 100.0 / totalUsageTime)
                        appendLine("  Most Used: ${topApp.packageName} (${String.format("%.1f", percentage)}%)")
                    }
                    appendLine()
                }
                
                if (showPerformance) {
                    appendLine("⚡ Performance Analysis:")
                    appendLine()
                    
                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val memoryInfo = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(memoryInfo)
                    
                    appendLine("  System Memory:")
                    appendLine("    Total RAM: ${formatMemory(memoryInfo.totalMem)}")
                    appendLine("    Available RAM: ${formatMemory(memoryInfo.availMem)}")
                    appendLine("    Used RAM: ${formatMemory(memoryInfo.totalMem - memoryInfo.availMem)}")
                    appendLine("    Low Memory State: ${if (memoryInfo.lowMemory) "Yes" else "No"}")
                    appendLine()
                    
                    // Running processes analysis
                    val runningProcesses = activityManager.runningAppProcesses
                    if (runningProcesses != null) {
                        appendLine("  Currently Running:")
                        appendLine("    Total Processes: ${runningProcesses.size}")
                        
                        val foregroundCount = runningProcesses.count { 
                            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                        }
                        val serviceCount = runningProcesses.count { 
                            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE 
                        }
                        val cachedCount = runningProcesses.count { 
                            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED 
                        }
                        
                        appendLine("    Foreground: $foregroundCount")
                        appendLine("    Services: $serviceCount")
                        appendLine("    Cached: $cachedCount")
                        appendLine()
                    }
                    
                    // Performance recommendations
                    appendLine("  💡 Recommendations:")
                    if (memoryInfo.lowMemory) {
                        appendLine("    ⚠️ System is low on memory - consider closing unused apps")
                    }
                    if (runningProcesses != null && runningProcesses.size > 50) {
                        appendLine("    ⚠️ Many processes running - performance may be affected")
                    }
                    if (filteredStats.size > 20) {
                        appendLine("    💡 Consider reviewing app usage - many apps used recently")
                    }
                    appendLine("    ✅ Regular usage pattern analysis helps optimize performance")
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: SecurityException) {
            CommandResult(
                success = false,
                output = "Permission required: PACKAGE_USAGE_STATS. Enable usage access in Settings > Apps > Special access > Usage access",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error retrieving usage statistics: ${e.message}",
                executionTimeMs = 0
            )
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
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        return java.text.SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date)
    }
    
    private fun formatMemory(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            String.format("%.0f MB", bytes / (1024.0 * 1024.0))
        }
    }
}

class PermCheckCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val showDangerous = parameters["dangerous"]?.toBoolean() ?: false
        val showUnused = parameters["unused"]?.toBoolean() ?: false
        val includeSystem = parameters["system"]?.toBoolean() ?: false
        val specificPackage = parameters["package"]
        
        return try {
            val packageManager = context.packageManager
            
            val packages = if (specificPackage != null) {
                try {
                    listOf(packageManager.getPackageInfo(specificPackage, PackageManager.GET_PERMISSIONS))
                } catch (e: PackageManager.NameNotFoundException) {
                    return CommandResult(
                        success = false,
                        output = "Package not found: $specificPackage",
                        executionTimeMs = 0
                    )
                }
            } else {
                val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                if (includeSystem) {
                    allPackages
                } else {
                    allPackages.filter { pkg ->
                        (pkg.applicationInfo?.flags ?: 0 and ApplicationInfo.FLAG_SYSTEM) == 0
                    }
                }
            }
            
            val output = buildString {
                appendLine("🔐 Permission Security Audit")
                if (specificPackage != null) {
                    appendLine("Package: $specificPackage")
                } else {
                    appendLine("Scope: ${if (includeSystem) "All apps" else "User apps only"}")
                }
                appendLine("Filters: ${buildList {
                    if (showDangerous) add("Dangerous permissions")
                    if (showUnused) add("Unused permissions")
                    if (isEmpty()) add("All permissions")
                }.joinToString(", ")}")
                appendLine("=".repeat(60))
                appendLine()
                
                var totalPermissions = 0
                var dangerousPermissions = 0
                var grantedPermissions = 0
                val permissionUsageMap = mutableMapOf<String, Int>()
                
                packages.forEach { packageInfo ->
                    val permissions = packageInfo.requestedPermissions
                    if (permissions != null && permissions.isNotEmpty()) {
                        val appName = try {
                            packageInfo.applicationInfo?.loadLabel(packageManager)?.toString() 
                                ?: packageInfo.packageName
                        } catch (e: Exception) {
                            packageInfo.packageName
                        }
                        
                        val relevantPermissions = permissions.filter { permission ->
                            val permissionInfo = try {
                                packageManager.getPermissionInfo(permission, 0)
                            } catch (e: Exception) {
                                null
                            }
                            
                            val isDangerous = permissionInfo?.protectionLevel == 
                                android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
                            val isGranted = packageManager.checkPermission(
                                permission, 
                                packageInfo.packageName
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            totalPermissions++
                            if (isDangerous) dangerousPermissions++
                            if (isGranted) grantedPermissions++
                            
                            permissionUsageMap[permission] = (permissionUsageMap[permission] ?: 0) + 1
                            
                            when {
                                showDangerous && showUnused -> isDangerous && !isGranted
                                showDangerous -> isDangerous
                                showUnused -> !isGranted
                                else -> true
                            }
                        }
                        
                        if (relevantPermissions.isNotEmpty()) {
                            appendLine("📱 $appName")
                            appendLine("   Package: ${packageInfo.packageName}")
                            appendLine("   Permissions (${relevantPermissions.size}):")
                            
                            relevantPermissions.forEach { permission ->
                                val permissionInfo = try {
                                    packageManager.getPermissionInfo(permission, 0)
                                } catch (e: Exception) {
                                    null
                                }
                                
                                val protectionLevel = permissionInfo?.let {
                                    when (it.protectionLevel) {
                                        android.content.pm.PermissionInfo.PROTECTION_NORMAL -> "Normal"
                                        android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> "⚠️ Dangerous"
                                        android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> "Signature"
                                        else -> "System"
                                    }
                                } ?: "Unknown"
                                
                                val isGranted = packageManager.checkPermission(
                                    permission, 
                                    packageInfo.packageName
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                val status = if (isGranted) "✅ Granted" else "❌ Not Granted"
                                
                                appendLine("     • ${permission.split('.').lastOrNull() ?: permission}")
                                appendLine("       Full: $permission")
                                appendLine("       Level: $protectionLevel")
                                appendLine("       Status: $status")
                                
                                if (permissionInfo?.loadDescription(packageManager) != null) {
                                    val description = permissionInfo.loadDescription(packageManager).toString()
                                    if (description.length <= 100) {
                                        appendLine("       Purpose: $description")
                                    }
                                }
                                appendLine()
                            }
                            appendLine()
                        }
                    }
                }
                
                // Summary statistics
                appendLine("📊 Permission Audit Summary:")
                appendLine("   Total Apps Analyzed: ${packages.size}")
                appendLine("   Total Permissions: $totalPermissions")
                appendLine("   Dangerous Permissions: $dangerousPermissions")
                appendLine("   Granted Permissions: $grantedPermissions")
                appendLine("   Denied Permissions: ${totalPermissions - grantedPermissions}")
                appendLine()
                
                // Most common permissions
                val topPermissions = permissionUsageMap.entries
                    .sortedByDescending { it.value }
                    .take(10)
                
                if (topPermissions.isNotEmpty()) {
                    appendLine("🔝 Most Requested Permissions:")
                    topPermissions.forEachIndexed { index, (permission, count) ->
                        val shortName = permission.split('.').lastOrNull() ?: permission
                        appendLine("   ${index + 1}. $shortName (requested by $count apps)")
                    }
                    appendLine()
                }
                
                // Security recommendations
                appendLine("🛡️ Security Recommendations:")
                if (dangerousPermissions > grantedPermissions * 0.3) {
                    appendLine("   ⚠️ High ratio of dangerous permissions detected")
                }
                if (grantedPermissions == totalPermissions) {
                    appendLine("   💡 Consider reviewing granted permissions for privacy")
                }
                appendLine("   ✅ Regular permission audits help maintain security")
                appendLine("   ✅ Revoke permissions for unused apps")
                appendLine("   ✅ Be cautious with dangerous permissions")
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error performing permission audit: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class PermGrantCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.getOrNull(0) ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        val permission = parameters["permission"] ?: arguments.getOrNull(1) ?: return CommandResult(
            success = false,
            output = "Permission name is required",
            executionTimeMs = 0
        )
        
        return try {
            val packageManager = context.packageManager
            
            // Check if package exists
            try {
                packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Package not found: $packageName",
                    executionTimeMs = 0
                )
            }
            
            // Check if permission exists
            val permissionInfo = try {
                packageManager.getPermissionInfo(permission, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Permission not found: $permission",
                    executionTimeMs = 0
                )
            }
            
            // Check current permission status
            val currentStatus = packageManager.checkPermission(permission, packageName)
            if (currentStatus == PackageManager.PERMISSION_GRANTED) {
                return CommandResult(
                    success = true,
                    output = "Permission '$permission' is already granted to $packageName",
                    executionTimeMs = 0
                )
            }
            
            // Check if it's a dangerous permission that requires user interaction
            val isDangerous = permissionInfo.protectionLevel == android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
            
            val output = buildString {
                appendLine("🔐 Permission Grant Request")
                appendLine("Package: $packageName")
                appendLine("Permission: $permission")
                appendLine("Protection Level: ${getProtectionLevelName(permissionInfo.protectionLevel)}")
                appendLine()
                
                if (isDangerous) {
                    appendLine("⚠️ This is a dangerous permission that requires user approval.")
                    appendLine("Opening system permission settings...")
                    
                    // Open permission settings for the app
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    try {
                        context.startActivity(intent)
                        appendLine("✅ Opened app settings. Please grant the permission manually.")
                        appendLine()
                        appendLine("Alternative: Use 'adb shell pm grant $packageName $permission' from a computer")
                    } catch (e: Exception) {
                        appendLine("❌ Unable to open app settings: ${e.message}")
                    }
                } else {
                    // For normal permissions, they should be granted automatically at install time
                    appendLine("ℹ️ This permission should be granted automatically.")
                    appendLine("If not granted, the app may not have declared it properly.")
                }
                
                // Permission description
                val description = permissionInfo.loadDescription(packageManager)
                if (description != null) {
                    appendLine("Description: $description")
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
                output = "Error granting permission: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun getProtectionLevelName(protectionLevel: Int): String {
        return when (protectionLevel) {
            android.content.pm.PermissionInfo.PROTECTION_NORMAL -> "Normal"
            android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> "Dangerous"
            android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> "Signature"
            else -> "System/Unknown"
        }
    }
}

class PermRevokeCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val packageName = parameters["package"] ?: arguments.getOrNull(0) ?: return CommandResult(
            success = false,
            output = "Package name is required",
            executionTimeMs = 0
        )
        
        val permission = parameters["permission"] ?: arguments.getOrNull(1) ?: return CommandResult(
            success = false,
            output = "Permission name is required",
            executionTimeMs = 0
        )
        
        return try {
            val packageManager = context.packageManager
            
            // Check if package exists
            try {
                packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Package not found: $packageName",
                    executionTimeMs = 0
                )
            }
            
            // Check if permission exists
            val permissionInfo = try {
                packageManager.getPermissionInfo(permission, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Permission not found: $permission",
                    executionTimeMs = 0
                )
            }
            
            // Check current permission status
            val currentStatus = packageManager.checkPermission(permission, packageName)
            if (currentStatus != PackageManager.PERMISSION_GRANTED) {
                return CommandResult(
                    success = true,
                    output = "Permission '$permission' is not granted to $packageName (already revoked)",
                    executionTimeMs = 0
                )
            }
            
            val output = buildString {
                appendLine("🔐 Permission Revocation Request")
                appendLine("Package: $packageName")
                appendLine("Permission: $permission")
                appendLine("Protection Level: ${getProtectionLevelName(permissionInfo.protectionLevel)}")
                appendLine()
                
                appendLine("⚠️ Revoking permissions requires user interaction.")
                appendLine("Opening system permission settings...")
                
                // Open permission settings for the app
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                try {
                    context.startActivity(intent)
                    appendLine("✅ Opened app settings. Please revoke the permission manually.")
                    appendLine()
                    appendLine("Alternative: Use 'adb shell pm revoke $packageName $permission' from a computer")
                } catch (e: Exception) {
                    appendLine("❌ Unable to open app settings: ${e.message}")
                }
                
                // Permission description
                val description = permissionInfo.loadDescription(packageManager)
                if (description != null) {
                    appendLine("Description: $description")
                }
                
                appendLine()
                appendLine("⚠️ Note: Revoking permissions may affect app functionality.")
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error revoking permission: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun getProtectionLevelName(protectionLevel: Int): String {
        return when (protectionLevel) {
            android.content.pm.PermissionInfo.PROTECTION_NORMAL -> "Normal"
            android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> "Dangerous"
            android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> "Signature"
            else -> "System/Unknown"
        }
    }
}

class AppCleanupCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val aggressive = parameters["aggressive"]?.toBoolean() ?: false
        val dryRun = parameters["dry-run"]?.toBoolean() ?: false
        val sizeThresholdMB = parameters["size-threshold"]?.toIntOrNull() ?: 50
        
        return try {
            val packageManager = context.packageManager
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            val output = buildString {
                appendLine("🧹 Smart App Cleanup Analysis")
                appendLine("Mode: ${if (aggressive) "Aggressive" else "Conservative"}")
                appendLine("Size Threshold: ${sizeThresholdMB}MB")
                appendLine("Execution: ${if (dryRun) "Dry Run (Analysis Only)" else "Active Cleanup"}")
                appendLine("=".repeat(50))
                appendLine()
                
                // Get all installed apps
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val userApps = installedApps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                
                var totalAppsAnalyzed = 0
                var appsToClean = 0
                var potentialSpaceSaved = 0L
                val cleanupRecommendations = mutableListOf<String>()
                
                appendLine("📊 Analysis Results:")
                appendLine()
                
                // Analyze each user app
                userApps.forEach { app ->
                    totalAppsAnalyzed++
                    
                    val appName = app.loadLabel(packageManager).toString()
                    val packageName = app.packageName
                    
                    // Check app size
                    val apkSize = try {
                        val apkFile = File(app.publicSourceDir)
                        apkFile.length()
                    } catch (e: Exception) {
                        0L
                    }
                    
                    val apkSizeMB = apkSize / (1024 * 1024)
                    
                    // Check if app is running
                    val runningProcesses = activityManager.runningAppProcesses
                    val isRunning = runningProcesses?.any { it.processName == packageName } == true
                    
                    // Check last usage (if we have permission)
                    var lastUsed = "Unknown"
                    var unusedDays = 0
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                            val endTime = System.currentTimeMillis()
                            val startTime = endTime - (30 * 24 * 60 * 60 * 1000L) // Last 30 days
                            
                            val usageStats = usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_MONTHLY,
                                startTime,
                                endTime
                            )
                            
                            val appUsage = usageStats.find { it.packageName == packageName }
                            if (appUsage != null && appUsage.lastTimeUsed > 0) {
                                unusedDays = ((endTime - appUsage.lastTimeUsed) / (24 * 60 * 60 * 1000)).toInt()
                                lastUsed = "${unusedDays} days ago"
                            }
                        } catch (e: Exception) {
                            // Permission not granted or other error
                        }
                    }
                    
                    // Determine if app should be cleaned
                    val shouldClean = when {
                        aggressive -> {
                            // Aggressive: clean if large OR unused for >7 days
                            apkSizeMB >= sizeThresholdMB || unusedDays > 7
                        }
                        else -> {
                            // Conservative: clean if large AND unused for >14 days
                            apkSizeMB >= sizeThresholdMB && unusedDays > 14
                        }
                    }
                    
                    if (shouldClean && !isRunning) {
                        appsToClean++
                        potentialSpaceSaved += apkSize
                        
                        val reason = buildList {
                            if (apkSizeMB >= sizeThresholdMB) add("Large size (${apkSizeMB}MB)")
                            if (unusedDays > 7) add("Unused for $unusedDays days")
                        }.joinToString(", ")
                        
                        cleanupRecommendations.add("  🗑️ $appName")
                        cleanupRecommendations.add("     Package: $packageName")
                        cleanupRecommendations.add("     Size: ${apkSizeMB}MB")
                        cleanupRecommendations.add("     Last Used: $lastUsed")
                        cleanupRecommendations.add("     Reason: $reason")
                        cleanupRecommendations.add("")
                    }
                }
                
                // Display recommendations
                if (cleanupRecommendations.isNotEmpty()) {
                    appendLine("📱 Apps Recommended for Cleanup (${appsToClean}):")
                    appendLine()
                    cleanupRecommendations.forEach { appendLine(it) }
                } else {
                    appendLine("✅ No apps need cleanup based on current criteria")
                    appendLine()
                }
                
                // Summary
                appendLine("📈 Cleanup Summary:")
                appendLine("  Total Apps Analyzed: $totalAppsAnalyzed")
                appendLine("  Apps Recommended for Cleanup: $appsToClean")
                appendLine("  Potential Space Saved: ${formatFileSize(potentialSpaceSaved)}")
                appendLine()
                
                // Additional cleanup suggestions
                appendLine("💡 Additional Cleanup Suggestions:")
                
                // Check cache directories
                try {
                    val cacheDir = context.cacheDir
                    val cacheSizeBytes = calculateDirectorySize(cacheDir)
                    if (cacheSizeBytes > 10 * 1024 * 1024) { // > 10MB
                        appendLine("  📁 App cache: ${formatFileSize(cacheSizeBytes)} - Consider clearing")
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                
                // System recommendations
                appendLine("  🔄 Run 'storage' command for detailed storage analysis")
                appendLine("  🗂️ Consider clearing app caches with 'clear-data --cache-only'")
                appendLine("  📱 Review downloaded files in /sdcard/Download/")
                appendLine()
                
                if (dryRun) {
                    appendLine("ℹ️ This was a dry run. Use 'appcleanup' without --dry-run to proceed with cleanup.")
                } else if (appsToClean > 0) {
                    appendLine("⚠️ To clean up apps, you'll need to manually uninstall them:")
                    appendLine("   Use: uninstall <package_name> for each app")
                    appendLine("   Or: Go to Settings > Apps and uninstall manually")
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
                output = "Error performing app cleanup analysis: ${e.message}",
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
    
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        try {
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    size += if (file.isDirectory) {
                        calculateDirectorySize(file)
                    } else {
                        file.length()
                    }
                }
            }
        } catch (e: Exception) {
            // Permission denied or other error
        }
        return size
    }
}

class AppDepsCommandExecutor : CommandExecutor {
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
        
        val showReverse = parameters["reverse"]?.toBoolean() ?: false
        val detailed = parameters["detailed"]?.toBoolean() ?: false
        
        return try {
            val packageManager = context.packageManager
            
            // Check if package exists
            val packageInfo = try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            } catch (e: PackageManager.NameNotFoundException) {
                return CommandResult(
                    success = false,
                    output = "Package not found: $packageName",
                    executionTimeMs = 0
                )
            }
            
            val output = buildString {
                appendLine("🔗 Application Dependency Analysis")
                appendLine("Package: $packageName")
                appendLine("App: ${packageInfo.applicationInfo?.loadLabel(packageManager)}")
                appendLine("Analysis Type: ${if (showReverse) "Reverse Dependencies" else "Direct Dependencies"}")
                appendLine("=".repeat(60))
                appendLine()
                
                if (showReverse) {
                    // Find apps that depend on this package
                    appendLine("🔍 Apps that may depend on this package:")
                    appendLine()
                    
                    val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                    val dependentApps = mutableListOf<PackageInfo>()
                    
                    allPackages.forEach { pkg ->
                        if (pkg.packageName != packageName) {
                            // Check if this package has any relationship with our target
                            val hasRelationship = checkPackageRelationship(pkg, packageName, packageManager)
                            if (hasRelationship) {
                                dependentApps.add(pkg)
                            }
                        }
                    }
                    
                    if (dependentApps.isNotEmpty()) {
                        dependentApps.forEach { depPkg ->
                            val appName = depPkg.applicationInfo?.loadLabel(packageManager)?.toString() ?: "Unknown"
                            appendLine("  📱 $appName")
                            appendLine("     Package: ${depPkg.packageName}")
                            appendLine("     Version: ${depPkg.versionName}")
                            if (detailed) {
                                appendLine("     Install Date: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(depPkg.firstInstallTime)}")
                                appendLine("     Target SDK: ${depPkg.applicationInfo?.targetSdkVersion}")
                            }
                            appendLine()
                        }
                        appendLine("Total dependent apps found: ${dependentApps.size}")
                    } else {
                        appendLine("  ✅ No obvious dependencies found")
                        appendLine("     This app appears to be safe to remove")
                    }
                    
                } else {
                    // Analyze direct dependencies
                    appendLine("🔍 Dependencies and Requirements:")
                    appendLine()
                    
                    // Permissions as dependencies
                    val permissions = packageInfo.requestedPermissions
                    if (permissions != null && permissions.isNotEmpty()) {
                        appendLine("📋 Permission Dependencies (${permissions.size}):")
                        
                        val criticalPermissions = mutableListOf<String>()
                        val normalPermissions = mutableListOf<String>()
                        
                        permissions.forEach { permission ->
                            val permissionInfo = try {
                                packageManager.getPermissionInfo(permission, 0)
                            } catch (e: Exception) {
                                null
                            }
                            
                            val isDangerous = permissionInfo?.protectionLevel == 
                                android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
                            
                            if (isDangerous) {
                                criticalPermissions.add(permission)
                            } else {
                                normalPermissions.add(permission)
                            }
                        }
                        
                        if (criticalPermissions.isNotEmpty()) {
                            appendLine("  ⚠️ Critical Permissions:")
                            criticalPermissions.take(10).forEach { permission ->
                                val shortName = permission.split('.').lastOrNull() ?: permission
                                val granted = packageManager.checkPermission(permission, packageName) == PackageManager.PERMISSION_GRANTED
                                appendLine("     • $shortName ${if (granted) "✅" else "❌"}")
                                if (detailed) {
                                    appendLine("       Full: $permission")
                                }
                            }
                            if (criticalPermissions.size > 10) {
                                appendLine("     ... and ${criticalPermissions.size - 10} more")
                            }
                            appendLine()
                        }
                        
                        if (detailed && normalPermissions.isNotEmpty()) {
                            appendLine("  ℹ️ Normal Permissions:")
                            normalPermissions.take(5).forEach { permission ->
                                val shortName = permission.split('.').lastOrNull() ?: permission
                                appendLine("     • $shortName")
                            }
                            if (normalPermissions.size > 5) {
                                appendLine("     ... and ${normalPermissions.size - 5} more")
                            }
                            appendLine()
                        }
                    }
                    
                    // System requirements
                    val appInfo = packageInfo.applicationInfo
                    appendLine("⚙️ System Requirements:")
                    appendLine("  Target SDK: ${appInfo?.targetSdkVersion}")
                    appendLine("  Min SDK: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo?.minSdkVersion else "Unknown"}")
                    appendLine("  System App: ${appInfo?.let { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 } ?: false}")
                    appendLine("  Hardware Acceleration: ${appInfo?.let { (it.flags and ApplicationInfo.FLAG_HARDWARE_ACCELERATED) != 0 } ?: false}")
                    appendLine()
                    
                    // Shared libraries (if available)
                    if (detailed) {
                        appendLine("📚 Potential Library Dependencies:")
                        
                        val commonLibraries = listOf(
                            "Google Play Services" to "com.google.android.gms",
                            "Google Play Store" to "com.android.vending",
                            "WebView" to "com.google.android.webview",
                            "Chrome" to "com.android.chrome"
                        )
                        
                        commonLibraries.forEach { (name, libPackage) ->
                            val isInstalled = try {
                                packageManager.getPackageInfo(libPackage, 0)
                                true
                            } catch (e: PackageManager.NameNotFoundException) {
                                false
                            }
                            
                            if (isInstalled) {
                                appendLine("  ✅ $name")
                            }
                        }
                        appendLine()
                    }
                    
                    // Data dependencies
                    appendLine("💾 Data and Storage:")
                    appendLine("  Data Directory: ${appInfo?.dataDir}")
                    if (detailed) {
                        try {
                            val dataDir = File(appInfo?.dataDir ?: "")
                            if (dataDir.exists()) {
                                val dataSizeBytes = calculateDirectorySize(dataDir)
                                appendLine("  Data Size: ${formatFileSize(dataSizeBytes)}")
                            }
                        } catch (e: Exception) {
                            appendLine("  Data Size: Unable to calculate")
                        }
                        
                        appendLine("  External Storage Access: ${permissions?.any { 
                            it.contains("WRITE_EXTERNAL_STORAGE") || it.contains("READ_EXTERNAL_STORAGE") 
                        } ?: false}")
                    }
                }
                
                appendLine()
                appendLine("💡 Dependency Analysis Summary:")
                if (showReverse) {
                    appendLine("  Use this information to understand impact before uninstalling")
                    appendLine("  Apps listed may lose functionality if this package is removed")
                } else {
                    appendLine("  Critical permissions indicate core device functions used")
                    appendLine("  Higher target SDK usually means better security and compatibility")
                    appendLine("  System app dependencies are typically essential")
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
                output = "Error analyzing app dependencies: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun checkPackageRelationship(pkg: PackageInfo, targetPackage: String, packageManager: PackageManager): Boolean {
        // This is a simplified check - in a real implementation, you might check:
        // 1. Shared user IDs
        // 2. Similar package names (same developer)
        // 3. Intent filters that might interact
        // 4. Content providers
        
        return try {
            // Check if packages have similar names (same developer)
            val pkgParts = pkg.packageName.split('.')
            val targetParts = targetPackage.split('.')
            
            // Check for same developer (first 2-3 parts of package name)
            if (pkgParts.size >= 2 && targetParts.size >= 2) {
                pkgParts.take(2) == targetParts.take(2)
            } else {
                false
            }
        } catch (e: Exception) {
            false
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
    
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        try {
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    size += if (file.isDirectory) {
                        calculateDirectorySize(file)
                    } else {
                        file.length()
                    }
                }
            }
        } catch (e: Exception) {
            // Permission denied or other error
        }
        return size
    }
}