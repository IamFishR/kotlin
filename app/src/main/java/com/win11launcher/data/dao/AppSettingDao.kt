package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.data.entities.SettingType
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {
    
    // App Settings queries
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSettingByKey(key: String): AppSetting?
    
    @Query("SELECT * FROM app_settings WHERE category = :category ORDER BY key")
    fun getSettingsByCategory(category: String): Flow<List<AppSetting>>
    
    @Query("SELECT * FROM app_settings ORDER BY category, key")
    fun getAllSettings(): Flow<List<AppSetting>>
    
    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<AppSetting>)
    
    @Update
    suspend fun updateSetting(setting: AppSetting)
    
    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
    
    @Query("DELETE FROM app_settings WHERE category = :category")
    suspend fun deleteSettingsByCategory(category: String)
    
    // Helper methods for common setting operations
    @Query("UPDATE app_settings SET value = :value, updatedAt = :timestamp, isUserModified = 1 WHERE key = :key")
    suspend fun updateSettingValue(key: String, value: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT EXISTS(SELECT 1 FROM app_settings WHERE key = :key)")
    suspend fun settingExists(key: String): Boolean
    
    // Permission States queries
    @Query("SELECT * FROM permission_states WHERE permissionName = :permissionName")
    suspend fun getPermissionState(permissionName: String): PermissionState?
    
    @Query("SELECT * FROM permission_states ORDER BY isRequired DESC, permissionName")
    fun getAllPermissionStates(): Flow<List<PermissionState>>
    
    @Query("SELECT * FROM permission_states WHERE isRequired = :isRequired ORDER BY permissionName")
    fun getPermissionStatesByRequired(isRequired: Boolean): Flow<List<PermissionState>>
    
    @Query("SELECT * FROM permission_states WHERE isGranted = :isGranted ORDER BY isRequired DESC, permissionName")
    fun getPermissionStatesByGranted(isGranted: Boolean): Flow<List<PermissionState>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissionState(permissionState: PermissionState)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissionStates(permissionStates: List<PermissionState>)
    
    @Update
    suspend fun updatePermissionState(permissionState: PermissionState)
    
    @Query("DELETE FROM permission_states WHERE permissionName = :permissionName")
    suspend fun deletePermissionState(permissionName: String)
    
    // Helper methods for permission tracking
    @Query("UPDATE permission_states SET isGranted = :isGranted, lastGrantedTime = :timestamp, updatedAt = :currentTime WHERE permissionName = :permissionName")
    suspend fun updatePermissionGranted(
        permissionName: String, 
        isGranted: Boolean, 
        timestamp: Long = System.currentTimeMillis(),
        currentTime: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE permission_states SET requestCount = requestCount + 1, lastRequestTime = :timestamp, updatedAt = :currentTime WHERE permissionName = :permissionName")
    suspend fun incrementPermissionRequestCount(
        permissionName: String, 
        timestamp: Long = System.currentTimeMillis(),
        currentTime: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE permission_states SET lastDeniedTime = :timestamp, updatedAt = :currentTime WHERE permissionName = :permissionName")
    suspend fun updatePermissionDenied(
        permissionName: String, 
        timestamp: Long = System.currentTimeMillis(),
        currentTime: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE permission_states SET userNotes = :notes, updatedAt = :currentTime WHERE permissionName = :permissionName")
    suspend fun updatePermissionNotes(
        permissionName: String, 
        notes: String,
        currentTime: Long = System.currentTimeMillis()
    )
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM permission_states WHERE isGranted = 1")
    suspend fun getGrantedPermissionCount(): Int
    
    @Query("SELECT COUNT(*) FROM permission_states WHERE isRequired = 1 AND isGranted = 0")
    suspend fun getMissingRequiredPermissionCount(): Int
    
    @Query("SELECT AVG(requestCount) FROM permission_states WHERE requestCount > 0")
    suspend fun getAveragePermissionRequestCount(): Double?
}