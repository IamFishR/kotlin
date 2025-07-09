package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.CommandHistory
import com.win11launcher.data.entities.CommandOutput
import com.win11launcher.data.entities.CommandUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {
    
    // Command History Operations
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC")
    fun getAllCommandHistory(): Flow<List<CommandHistory>>
    
    @Query("SELECT * FROM command_history WHERE session_id = :sessionId ORDER BY timestamp DESC")
    fun getCommandHistoryBySession(sessionId: String): Flow<List<CommandHistory>>
    
    @Query("SELECT * FROM command_history WHERE command_type = :commandType ORDER BY timestamp DESC")
    fun getCommandHistoryByType(commandType: String): Flow<List<CommandHistory>>
    
    @Query("SELECT DISTINCT command FROM command_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentCommands(limit: Int = 50): List<String>
    
    @Query("SELECT * FROM command_history WHERE id = :id")
    suspend fun getCommandHistoryById(id: String): CommandHistory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandHistory(commandHistory: CommandHistory)
    
    @Update
    suspend fun updateCommandHistory(commandHistory: CommandHistory)
    
    @Delete
    suspend fun deleteCommandHistory(commandHistory: CommandHistory)
    
    @Query("DELETE FROM command_history WHERE session_id = :sessionId")
    suspend fun deleteCommandHistoryBySession(sessionId: String)
    
    @Query("DELETE FROM command_history WHERE timestamp < :timestamp")
    suspend fun deleteOldCommandHistory(timestamp: Long)
    
    // Command Output Operations
    @Query("SELECT * FROM command_outputs WHERE command_id = :commandId")
    suspend fun getCommandOutput(commandId: String): CommandOutput?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandOutput(commandOutput: CommandOutput)
    
    @Delete
    suspend fun deleteCommandOutput(commandOutput: CommandOutput)
    
    @Query("DELETE FROM command_outputs WHERE timestamp < :timestamp")
    suspend fun deleteOldCommandOutputs(timestamp: Long)
    
    // Command Usage Operations
    @Query("SELECT * FROM command_usage ORDER BY usage_count DESC")
    fun getAllCommandUsage(): Flow<List<CommandUsage>>
    
    @Query("SELECT * FROM command_usage WHERE category = :category ORDER BY usage_count DESC")
    fun getCommandUsageByCategory(category: String): Flow<List<CommandUsage>>
    
    @Query("SELECT * FROM command_usage WHERE command = :command")
    suspend fun getCommandUsage(command: String): CommandUsage?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandUsage(commandUsage: CommandUsage)
    
    @Update
    suspend fun updateCommandUsage(commandUsage: CommandUsage)
    
    @Query("SELECT command FROM command_usage WHERE command LIKE :pattern ORDER BY usage_count DESC LIMIT :limit")
    suspend fun getCommandSuggestions(pattern: String, limit: Int = 10): List<String>
    
    // Analytics and Statistics
    @Query("SELECT COUNT(*) FROM command_history")
    suspend fun getTotalCommandCount(): Int
    
    @Query("SELECT COUNT(*) FROM command_history WHERE success = 1")
    suspend fun getSuccessfulCommandCount(): Int
    
    @Query("SELECT COUNT(*) FROM command_history WHERE command_type = :type")
    suspend fun getCommandCountByType(type: String): Int
    
    @Query("SELECT AVG(execution_time_ms) FROM command_history WHERE command_type = :type")
    suspend fun getAverageExecutionTime(type: String): Float?
    
    @Query("SELECT command_type, COUNT(*) as count FROM command_history GROUP BY command_type")
    suspend fun getCommandTypeStatistics(): List<CommandTypeStats>
    
    @Query("""
        SELECT ch.*, co.full_output 
        FROM command_history ch 
        LEFT JOIN command_outputs co ON ch.full_output_id = co.id 
        WHERE ch.id = :id
    """)
    suspend fun getCommandHistoryWithOutput(id: String): CommandHistoryWithOutput?
    
    // Cleanup operations
    @Query("DELETE FROM command_history WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldHistory(cutoffTime: Long)
    
    @Query("DELETE FROM command_outputs WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldOutputs(cutoffTime: Long)
    
    // Search operations
    @Query("SELECT * FROM command_history WHERE command LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchCommandHistory(searchTerm: String, limit: Int = 100): List<CommandHistory>
}

// Data classes for complex queries
data class CommandTypeStats(
    val command_type: String,
    val count: Int
)

data class CommandHistoryWithOutput(
    val id: String,
    val command: String,
    @ColumnInfo(name = "command_type") val commandType: String,
    @ColumnInfo(name = "sub_command") val subCommand: String?,
    val arguments: String?,
    val timestamp: Long,
    @ColumnInfo(name = "execution_time_ms") val executionTimeMs: Long,
    val success: Boolean,
    @ColumnInfo(name = "output_preview") val outputPreview: String,
    @ColumnInfo(name = "full_output_id") val fullOutputId: String?,
    @ColumnInfo(name = "session_id") val sessionId: String,
    val full_output: String?
)