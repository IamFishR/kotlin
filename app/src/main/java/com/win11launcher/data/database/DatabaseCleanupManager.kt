package com.win11launcher.data.database

import com.win11launcher.data.repositories.CommandLineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseCleanupManager @Inject constructor(
    private val commandLineRepository: CommandLineRepository
) {
    
    private val cleanupScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val MAX_COMMAND_HISTORY_DAYS = 30
        private const val MAX_AI_CONVERSATION_DAYS = 7
        private const val MAX_SYSTEM_SNAPSHOTS_DAYS = 3
        private const val MAX_DATABASE_SIZE_MB = 50
        
        private const val CLEANUP_INTERVAL_HOURS = 6
    }
    
    fun startPeriodicCleanup() {
        cleanupScope.launch {
            while (true) {
                try {
                    performCleanup()
                } catch (e: Exception) {
                    // Log error but don't crash
                }
                delay(TimeUnit.HOURS.toMillis(CLEANUP_INTERVAL_HOURS.toLong()))
            }
        }
    }
    
    suspend fun performCleanup() {
        val now = System.currentTimeMillis()
        
        // Clean old command history
        val commandHistoryCutoff = now - TimeUnit.DAYS.toMillis(MAX_COMMAND_HISTORY_DAYS.toLong())
        commandLineRepository.cleanupOldData(commandHistoryCutoff)
        
        // Clean old AI conversations
        val aiConversationCutoff = now - TimeUnit.DAYS.toMillis(MAX_AI_CONVERSATION_DAYS.toLong())
        // Need to add this method to repository
        
        // Clean old system snapshots
        val systemSnapshotCutoff = now - TimeUnit.DAYS.toMillis(MAX_SYSTEM_SNAPSHOTS_DAYS.toLong())
        // Already implemented in cleanupOldData
        
        // If database is still too large, perform aggressive cleanup
        // This would need database size checking implementation
    }
}