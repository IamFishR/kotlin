package com.win11launcher.data.repositories

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.data.entities.SettingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val context: Context
) {
    
    // Settings operations
    suspend fun getSetting(key: String, defaultValue: String = ""): String {
        return appSettingDao.getSettingValue(key) ?: defaultValue
    }
    
    suspend fun getSettingBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val value = appSettingDao.getSettingValue(key) ?: return defaultValue
        return value.toBoolean()
    }
    
    suspend fun getSettingInt(key: String, defaultValue: Int = 0): Int {
        val value = appSettingDao.getSettingValue(key) ?: return defaultValue
        return value.toIntOrNull() ?: defaultValue
    }
    
    suspend fun getSettingFloat(key: String, defaultValue: Float = 0f): Float {
        val value = appSettingDao.getSettingValue(key) ?: return defaultValue
        return value.toFloatOrNull() ?: defaultValue
    }
    
    suspend fun setSetting(key: String, value: String, category: String = "general", description: String = "") {
        val setting = AppSetting(
            key = key,
            value = value,
            settingType = SettingType.STRING,
            category = category,
            description = description,
            isUserModified = true
        )
        appSettingDao.insertSetting(setting)
    }
    
    suspend fun setSettingBoolean(key: String, value: Boolean, category: String = "general", description: String = "") {
        val setting = AppSetting(
            key = key,
            value = value.toString(),
            settingType = SettingType.BOOLEAN,
            category = category,
            description = description,
            isUserModified = true
        )
        appSettingDao.insertSetting(setting)
    }
    
    suspend fun setSettingInt(key: String, value: Int, category: String = "general", description: String = "") {
        val setting = AppSetting(
            key = key,
            value = value.toString(),
            settingType = SettingType.INTEGER,
            category = category,
            description = description,
            isUserModified = true
        )
        appSettingDao.insertSetting(setting)
    }
    
    fun getSettingsByCategory(category: String): Flow<List<AppSetting>> {
        return appSettingDao.getSettingsByCategory(category)
    }
    
    fun getAllSettings(): Flow<List<AppSetting>> {
        return appSettingDao.getAllSettings()
    }
    
    // Permission operations
    suspend fun updatePermissionState(permissionName: String, isGranted: Boolean, isRequired: Boolean = false) {
        val existing = appSettingDao.getPermissionState(permissionName)
        val permissionState = if (existing != null) {
            existing.copy(
                isGranted = isGranted,
                isRequired = isRequired,
                lastGrantedTime = if (isGranted) System.currentTimeMillis() else existing.lastGrantedTime,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            PermissionState(
                permissionName = permissionName,
                isGranted = isGranted,
                isRequired = isRequired,
                lastGrantedTime = if (isGranted) System.currentTimeMillis() else null
            )
        }
        appSettingDao.insertPermissionState(permissionState)
    }
    
    suspend fun requestPermission(permissionName: String) {
        appSettingDao.incrementPermissionRequestCount(permissionName)
    }
    
    suspend fun denyPermission(permissionName: String) {
        appSettingDao.updatePermissionDenied(permissionName)
    }
    
    suspend fun updatePermissionNotes(permissionName: String, notes: String) {
        appSettingDao.updatePermissionNotes(permissionName, notes)
    }
    
    fun getAllPermissionStates(): Flow<List<PermissionState>> {
        return appSettingDao.getAllPermissionStates()
    }
    
    fun getRequiredPermissionStates(): Flow<List<PermissionState>> {
        return appSettingDao.getPermissionStatesByRequired(true)
    }
    
    fun getGrantedPermissionStates(): Flow<List<PermissionState>> {
        return appSettingDao.getPermissionStatesByGranted(true)
    }
    
    // Real-time permission checking
    suspend fun checkAndUpdateAllPermissions() {
        val permissionChecks = mapOf(
            "android.permission.ACCESS_WIFI_STATE" to true,
            "android.permission.CHANGE_WIFI_STATE" to true,
            "android.permission.ACCESS_NETWORK_STATE" to false,
            "android.permission.BLUETOOTH" to false,
            "android.permission.BLUETOOTH_ADMIN" to false,
            "android.permission.BLUETOOTH_CONNECT" to false,
            "android.permission.BLUETOOTH_SCAN" to false,
            "android.permission.ACCESS_FINE_LOCATION" to false,
            "android.permission.ACCESS_COARSE_LOCATION" to false,
            "android.permission.SYSTEM_ALERT_WINDOW" to false,
            "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" to true,
            "android.permission.READ_PHONE_STATE" to false,
            "android.permission.CAMERA" to false,
            "android.permission.WRITE_SETTINGS" to false
        )
        
        permissionChecks.forEach { (permission, isRequired) ->
            val isGranted = try {
                when (permission) {
                    "android.permission.SYSTEM_ALERT_WINDOW" -> {
                        android.provider.Settings.canDrawOverlays(context)
                    }
                    "android.permission.WRITE_SETTINGS" -> {
                        android.provider.Settings.System.canWrite(context)
                    }
                    else -> {
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }
                }
            } catch (e: Exception) {
                false
            }
            
            updatePermissionState(permission, isGranted, isRequired)
        }
    }
    
    // Permission analytics
    suspend fun getPermissionAnalytics(): PermissionAnalytics {
        val allPermissions = appSettingDao.getAllPermissionStates().first()
        val grantedCount = appSettingDao.getGrantedPermissionCount()
        val missingRequiredCount = appSettingDao.getMissingRequiredPermissionCount()
        val avgRequestCount = appSettingDao.getAveragePermissionRequestCount() ?: 0.0
        
        return PermissionAnalytics(
            totalPermissions = allPermissions.size,
            grantedPermissions = grantedCount,
            missingRequiredPermissions = missingRequiredCount,
            averageRequestCount = avgRequestCount,
            permissionCompletionRate = if (allPermissions.isNotEmpty()) {
                (grantedCount.toFloat() / allPermissions.size) * 100
            } else 0f
        )
    }
    
    // Helper methods for common settings
    suspend fun isAutoCheckPermissionsEnabled(): Boolean {
        return getSettingBoolean("auto_check_permissions", true)
    }
    
    suspend fun setAutoCheckPermissions(enabled: Boolean) {
        setSettingBoolean("auto_check_permissions", enabled, "permissions", "Automatically check permission status")
    }
    
    suspend fun isPermissionNotificationsEnabled(): Boolean {
        return getSettingBoolean("show_permission_notifications", true)
    }
    
    suspend fun setPermissionNotifications(enabled: Boolean) {
        setSettingBoolean("show_permission_notifications", enabled, "permissions", "Show notifications for permission changes")
    }
    
    suspend fun getThemeMode(): String {
        return getSetting("theme_mode", "DARK")
    }
    
    suspend fun setThemeMode(mode: String) {
        setSetting("theme_mode", mode, "appearance", "Application theme mode")
    }
    
    suspend fun isLauncherAutoStartEnabled(): Boolean {
        return getSettingBoolean("launcher_auto_start", true)
    }
    
    suspend fun setLauncherAutoStart(enabled: Boolean) {
        setSettingBoolean("launcher_auto_start", enabled, "system", "Start launcher automatically on boot")
    }
}

data class PermissionAnalytics(
    val totalPermissions: Int,
    val grantedPermissions: Int,
    val missingRequiredPermissions: Int,
    val averageRequestCount: Double,
    val permissionCompletionRate: Float
)