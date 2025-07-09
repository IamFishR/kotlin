package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.SystemSnapshot
import com.win11launcher.data.entities.NetworkHistory
import com.win11launcher.data.entities.AppAction
import com.win11launcher.data.entities.FileOperation
import com.win11launcher.data.entities.DatabaseQuery
import com.win11launcher.data.entities.UserScript
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemMonitoringDao {
    
    // System Snapshot Operations
    @Query("SELECT * FROM system_snapshots ORDER BY timestamp DESC")
    fun getAllSystemSnapshots(): Flow<List<SystemSnapshot>>
    
    @Query("SELECT * FROM system_snapshots WHERE trigger_command = :command ORDER BY timestamp DESC")
    fun getSystemSnapshotsByTrigger(command: String): Flow<List<SystemSnapshot>>
    
    @Query("SELECT * FROM system_snapshots WHERE id = :id")
    suspend fun getSystemSnapshotById(id: String): SystemSnapshot?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemSnapshot(systemSnapshot: SystemSnapshot)
    
    @Delete
    suspend fun deleteSystemSnapshot(systemSnapshot: SystemSnapshot)
    
    @Query("DELETE FROM system_snapshots WHERE timestamp < :timestamp")
    suspend fun deleteOldSystemSnapshots(timestamp: Long)
    
    // Network History Operations
    @Query("SELECT * FROM network_history ORDER BY timestamp DESC")
    fun getAllNetworkHistory(): Flow<List<NetworkHistory>>
    
    @Query("SELECT * FROM network_history WHERE type = :type ORDER BY timestamp DESC")
    fun getNetworkHistoryByType(type: String): Flow<List<NetworkHistory>>
    
    @Query("SELECT * FROM network_history WHERE action = :action ORDER BY timestamp DESC")
    fun getNetworkHistoryByAction(action: String): Flow<List<NetworkHistory>>
    
    @Query("SELECT * FROM network_history WHERE target_identifier = :identifier ORDER BY timestamp DESC")
    fun getNetworkHistoryByIdentifier(identifier: String): Flow<List<NetworkHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworkHistory(networkHistory: NetworkHistory)
    
    @Delete
    suspend fun deleteNetworkHistory(networkHistory: NetworkHistory)
    
    @Query("DELETE FROM network_history WHERE timestamp < :timestamp")
    suspend fun deleteOldNetworkHistory(timestamp: Long)
    
    // App Action Operations
    @Query("SELECT * FROM app_actions ORDER BY timestamp DESC")
    fun getAllAppActions(): Flow<List<AppAction>>
    
    @Query("SELECT * FROM app_actions WHERE package_name = :packageName ORDER BY timestamp DESC")
    fun getAppActionsByPackage(packageName: String): Flow<List<AppAction>>
    
    @Query("SELECT * FROM app_actions WHERE action = :action ORDER BY timestamp DESC")
    fun getAppActionsByAction(action: String): Flow<List<AppAction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppAction(appAction: AppAction)
    
    @Delete
    suspend fun deleteAppAction(appAction: AppAction)
    
    @Query("DELETE FROM app_actions WHERE timestamp < :timestamp")
    suspend fun deleteOldAppActions(timestamp: Long)
    
    // File Operation Operations
    @Query("SELECT * FROM file_operations ORDER BY timestamp DESC")
    fun getAllFileOperations(): Flow<List<FileOperation>>
    
    @Query("SELECT * FROM file_operations WHERE operation = :operation ORDER BY timestamp DESC")
    fun getFileOperationsByOperation(operation: String): Flow<List<FileOperation>>
    
    @Query("SELECT * FROM file_operations WHERE source_path LIKE :pathPattern ORDER BY timestamp DESC")
    fun getFileOperationsByPath(pathPattern: String): Flow<List<FileOperation>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileOperation(fileOperation: FileOperation)
    
    @Delete
    suspend fun deleteFileOperation(fileOperation: FileOperation)
    
    @Query("DELETE FROM file_operations WHERE timestamp < :timestamp")
    suspend fun deleteOldFileOperations(timestamp: Long)
    
    // Database Query Operations
    @Query("SELECT * FROM db_queries ORDER BY timestamp DESC")
    fun getAllDatabaseQueries(): Flow<List<DatabaseQuery>>
    
    @Query("SELECT * FROM db_queries WHERE query_type = :queryType ORDER BY timestamp DESC")
    fun getDatabaseQueriesByType(queryType: String): Flow<List<DatabaseQuery>>
    
    @Query("SELECT * FROM db_queries WHERE table_name = :tableName ORDER BY timestamp DESC")
    fun getDatabaseQueriesByTable(tableName: String): Flow<List<DatabaseQuery>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatabaseQuery(databaseQuery: DatabaseQuery)
    
    @Delete
    suspend fun deleteDatabaseQuery(databaseQuery: DatabaseQuery)
    
    @Query("DELETE FROM db_queries WHERE timestamp < :timestamp")
    suspend fun deleteOldDatabaseQueries(timestamp: Long)
    
    // User Script Operations
    @Query("SELECT * FROM user_scripts ORDER BY name ASC")
    fun getAllUserScripts(): Flow<List<UserScript>>
    
    @Query("SELECT * FROM user_scripts WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteUserScripts(): Flow<List<UserScript>>
    
    @Query("SELECT * FROM user_scripts WHERE name = :name")
    suspend fun getUserScriptByName(name: String): UserScript?
    
    @Query("SELECT * FROM user_scripts WHERE id = :id")
    suspend fun getUserScriptById(id: String): UserScript?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserScript(userScript: UserScript)
    
    @Update
    suspend fun updateUserScript(userScript: UserScript)
    
    @Delete
    suspend fun deleteUserScript(userScript: UserScript)
    
    @Query("SELECT * FROM user_scripts WHERE tags LIKE :tag ORDER BY name ASC")
    suspend fun getUserScriptsByTag(tag: String): List<UserScript>
    
    @Query("SELECT * FROM user_scripts WHERE schedule_type IS NOT NULL ORDER BY name ASC")
    fun getScheduledUserScripts(): Flow<List<UserScript>>
    
    // Analytics and Statistics
    @Query("SELECT COUNT(*) FROM network_history WHERE success = 1 AND type = :type")
    suspend fun getSuccessfulNetworkOperations(type: String): Int
    
    @Query("SELECT COUNT(*) FROM app_actions WHERE success = 1 AND action = :action")
    suspend fun getSuccessfulAppActions(action: String): Int
    
    @Query("SELECT COUNT(*) FROM file_operations WHERE success = 1 AND operation = :operation")
    suspend fun getSuccessfulFileOperations(operation: String): Int
    
    @Query("SELECT AVG(execution_time_ms) FROM db_queries WHERE query_type = :queryType")
    suspend fun getAverageQueryExecutionTime(queryType: String): Float?
    
    @Query("SELECT SUM(bytes_transferred) FROM file_operations WHERE bytes_transferred IS NOT NULL")
    suspend fun getTotalBytesTransferred(): Long?
    
    @Query("SELECT package_name, COUNT(*) as count FROM app_actions GROUP BY package_name ORDER BY count DESC LIMIT :limit")
    suspend fun getMostUsedApps(limit: Int = 10): List<AppUsageStats>
    
    @Query("SELECT operation, COUNT(*) as count FROM file_operations GROUP BY operation ORDER BY count DESC")
    suspend fun getFileOperationStatistics(): List<FileOperationStats>
    
    @Query("SELECT type, action, COUNT(*) as count FROM network_history GROUP BY type, action ORDER BY count DESC")
    suspend fun getNetworkActionStatistics(): List<NetworkActionStats>
    
    // Cleanup operations
    @Query("DELETE FROM system_snapshots WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldSnapshots(cutoffTime: Long)
    
    @Query("DELETE FROM network_history WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldNetworkHistory(cutoffTime: Long)
    
    @Query("DELETE FROM app_actions WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldAppActions(cutoffTime: Long)
    
    @Query("DELETE FROM file_operations WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldFileOperations(cutoffTime: Long)
    
    @Query("DELETE FROM db_queries WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldDatabaseQueries(cutoffTime: Long)
}

// Data classes for complex queries
data class AppUsageStats(
    val package_name: String,
    val count: Int
)

data class FileOperationStats(
    val operation: String,
    val count: Int
)

data class NetworkActionStats(
    val type: String,
    val action: String,
    val count: Int
)