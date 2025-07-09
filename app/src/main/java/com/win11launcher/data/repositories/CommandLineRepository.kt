package com.win11launcher.data.repositories

import com.win11launcher.data.dao.CommandHistoryDao
import com.win11launcher.data.dao.AIConversationDao
import com.win11launcher.data.dao.SystemMonitoringDao
import com.win11launcher.data.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandLineRepository @Inject constructor(
    private val commandHistoryDao: CommandHistoryDao,
    private val aiConversationDao: AIConversationDao,
    private val systemMonitoringDao: SystemMonitoringDao
) {
    
    // Command History Operations
    fun getAllCommandHistory(): Flow<List<CommandHistory>> = commandHistoryDao.getAllCommandHistory()
    
    fun getCommandHistoryBySession(sessionId: String): Flow<List<CommandHistory>> = 
        commandHistoryDao.getCommandHistoryBySession(sessionId)
    
    suspend fun getRecentCommands(limit: Int = 50): List<String> = 
        commandHistoryDao.getRecentCommands(limit)
    
    suspend fun insertCommandHistory(
        command: String,
        commandType: String,
        subCommand: String? = null,
        arguments: String? = null,
        sessionId: String,
        executionTimeMs: Long,
        success: Boolean,
        outputPreview: String,
        fullOutputId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val commandHistory = CommandHistory(
            id = id,
            command = command,
            commandType = commandType,
            subCommand = subCommand,
            arguments = arguments,
            timestamp = System.currentTimeMillis(),
            executionTimeMs = executionTimeMs,
            success = success,
            outputPreview = outputPreview,
            fullOutputId = fullOutputId,
            sessionId = sessionId
        )
        commandHistoryDao.insertCommandHistory(commandHistory)
        return id
    }
    
    suspend fun insertCommandOutput(commandId: String, fullOutput: String, outputType: String): String {
        val id = UUID.randomUUID().toString()
        val commandOutput = CommandOutput(
            id = id,
            commandId = commandId,
            fullOutput = fullOutput,
            outputType = outputType,
            timestamp = System.currentTimeMillis()
        )
        commandHistoryDao.insertCommandOutput(commandOutput)
        return id
    }
    
    suspend fun getCommandHistoryWithOutput(id: String) = 
        commandHistoryDao.getCommandHistoryWithOutput(id)
    
    suspend fun updateCommandUsage(command: String, category: String, executionTimeMs: Long, success: Boolean) {
        val existing = commandHistoryDao.getCommandUsage(command)
        if (existing != null) {
            val newUsageCount = existing.usageCount + 1
            val newSuccessCount = if (success) {
                (existing.successRate * existing.usageCount + 1).toInt()
            } else {
                (existing.successRate * existing.usageCount).toInt()
            }
            val newSuccessRate = newSuccessCount.toFloat() / newUsageCount
            val newTotalTime = existing.totalExecutionTime + executionTimeMs
            val newAverageTime = newTotalTime.toFloat() / newUsageCount
            
            val updated = existing.copy(
                usageCount = newUsageCount,
                lastUsed = System.currentTimeMillis(),
                successRate = newSuccessRate,
                averageExecutionTime = newAverageTime,
                totalExecutionTime = newTotalTime
            )
            commandHistoryDao.updateCommandUsage(updated)
        } else {
            val newUsage = CommandUsage(
                command = command,
                usageCount = 1,
                lastUsed = System.currentTimeMillis(),
                successRate = if (success) 1.0f else 0.0f,
                averageExecutionTime = executionTimeMs.toFloat(),
                totalExecutionTime = executionTimeMs,
                category = category
            )
            commandHistoryDao.insertCommandUsage(newUsage)
        }
    }
    
    suspend fun getCommandSuggestions(pattern: String, limit: Int = 10): List<String> = 
        commandHistoryDao.getCommandSuggestions("%$pattern%", limit)
    
    // AI Conversation Operations
    fun getAllAIConversations(): Flow<List<AIConversation>> = aiConversationDao.getAllAIConversations()
    
    fun getAIConversationsBySession(sessionId: String): Flow<List<AIConversation>> = 
        aiConversationDao.getAIConversationsBySession(sessionId)
    
    suspend fun insertAIConversation(
        sessionId: String,
        prompt: String,
        response: String,
        modelUsed: String,
        processingTimeMs: Long,
        conversationType: String = "CHAT",
        contextData: String? = null,
        generatedCommands: String? = null,
        tokensUsed: Int? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val aiConversation = AIConversation(
            id = id,
            sessionId = sessionId,
            prompt = prompt,
            response = response,
            modelUsed = modelUsed,
            tokensUsed = tokensUsed,
            processingTimeMs = processingTimeMs,
            timestamp = System.currentTimeMillis(),
            conversationType = conversationType,
            contextData = contextData,
            generatedCommands = generatedCommands,
            userRating = null
        )
        aiConversationDao.insertAIConversation(aiConversation)
        return id
    }
    
    suspend fun updateAIConversationRating(id: String, rating: Int) {
        val conversation = aiConversationDao.getAIConversationById(id)
        conversation?.let {
            val updated = it.copy(userRating = rating)
            aiConversationDao.updateAIConversation(updated)
        }
    }
    
    suspend fun searchAIConversations(searchTerm: String, limit: Int = 100): List<AIConversation> = 
        aiConversationDao.searchAIConversations("%$searchTerm%", limit)
    
    // System Monitoring Operations
    suspend fun insertSystemSnapshot(
        batteryLevel: Int,
        batteryHealth: String?,
        batteryTemp: Float?,
        memoryUsage: Long,
        cpuUsage: Float?,
        networkState: String,
        wifiSSID: String?,
        runningApps: String,
        triggerCommand: String? = null,
        customData: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val snapshot = SystemSnapshot(
            id = id,
            timestamp = System.currentTimeMillis(),
            batteryLevel = batteryLevel,
            batteryHealth = batteryHealth,
            batteryTemp = batteryTemp,
            memoryUsage = memoryUsage,
            cpuUsage = cpuUsage,
            networkState = networkState,
            wifiSSID = wifiSSID,
            runningApps = runningApps,
            triggerCommand = triggerCommand,
            customData = customData
        )
        systemMonitoringDao.insertSystemSnapshot(snapshot)
        return id
    }
    
    suspend fun insertNetworkHistory(
        type: String,
        action: String,
        targetIdentifier: String,
        success: Boolean,
        additionalData: String? = null,
        commandId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val networkHistory = NetworkHistory(
            id = id,
            type = type,
            action = action,
            targetIdentifier = targetIdentifier,
            success = success,
            timestamp = System.currentTimeMillis(),
            additionalData = additionalData,
            commandId = commandId
        )
        systemMonitoringDao.insertNetworkHistory(networkHistory)
        return id
    }
    
    suspend fun insertAppAction(
        packageName: String,
        action: String,
        success: Boolean,
        commandId: String? = null,
        additionalData: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val appAction = AppAction(
            id = id,
            packageName = packageName,
            action = action,
            timestamp = System.currentTimeMillis(),
            success = success,
            commandId = commandId,
            additionalData = additionalData
        )
        systemMonitoringDao.insertAppAction(appAction)
        return id
    }
    
    suspend fun insertFileOperation(
        operation: String,
        sourcePath: String,
        destinationPath: String? = null,
        success: Boolean,
        filesAffected: Int,
        bytesTransferred: Long? = null,
        commandId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val fileOperation = FileOperation(
            id = id,
            operation = operation,
            sourcePath = sourcePath,
            destinationPath = destinationPath,
            timestamp = System.currentTimeMillis(),
            success = success,
            filesAffected = filesAffected,
            bytesTransferred = bytesTransferred,
            commandId = commandId
        )
        systemMonitoringDao.insertFileOperation(fileOperation)
        return id
    }
    
    suspend fun insertDatabaseQuery(
        query: String,
        tableName: String?,
        queryType: String,
        resultCount: Int,
        executionTimeMs: Long,
        success: Boolean,
        errorMessage: String? = null,
        commandId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val databaseQuery = DatabaseQuery(
            id = id,
            query = query,
            tableName = tableName,
            queryType = queryType,
            resultCount = resultCount,
            executionTimeMs = executionTimeMs,
            timestamp = System.currentTimeMillis(),
            success = success,
            errorMessage = errorMessage,
            commandId = commandId
        )
        systemMonitoringDao.insertDatabaseQuery(databaseQuery)
        return id
    }
    
    // User Script Operations
    fun getAllUserScripts(): Flow<List<UserScript>> = systemMonitoringDao.getAllUserScripts()
    
    fun getFavoriteUserScripts(): Flow<List<UserScript>> = systemMonitoringDao.getFavoriteUserScripts()
    
    suspend fun getUserScriptByName(name: String): UserScript? = 
        systemMonitoringDao.getUserScriptByName(name)
    
    suspend fun insertUserScript(
        name: String,
        description: String,
        commands: String,
        tags: String? = null,
        scheduleType: String? = null,
        scheduleData: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val userScript = UserScript(
            id = id,
            name = name,
            description = description,
            commands = commands,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
            lastExecuted = null,
            tags = tags,
            scheduleType = scheduleType,
            scheduleData = scheduleData
        )
        systemMonitoringDao.insertUserScript(userScript)
        return id
    }
    
    suspend fun updateUserScript(userScript: UserScript) {
        val updated = userScript.copy(lastModified = System.currentTimeMillis())
        systemMonitoringDao.updateUserScript(updated)
    }
    
    suspend fun executeUserScript(scriptId: String) {
        val script = systemMonitoringDao.getUserScriptById(scriptId)
        script?.let {
            val updated = it.copy(
                lastExecuted = System.currentTimeMillis(),
                executionCount = it.executionCount + 1
            )
            systemMonitoringDao.updateUserScript(updated)
        }
    }
    
    // Analytics and Statistics
    suspend fun getTotalCommandCount(): Int = commandHistoryDao.getTotalCommandCount()
    
    suspend fun getSuccessfulCommandCount(): Int = commandHistoryDao.getSuccessfulCommandCount()
    
    suspend fun getCommandTypeStatistics() = commandHistoryDao.getCommandTypeStatistics()
    
    suspend fun getAverageProcessingTime(type: String): Float? = 
        aiConversationDao.getAverageProcessingTime(type)
    
    suspend fun getAverageUserRating(): Float? = aiConversationDao.getAverageUserRating()
    
    suspend fun getMostUsedApps(limit: Int = 10) = systemMonitoringDao.getMostUsedApps(limit)
    
    // Cleanup Operations
    suspend fun cleanupOldData(cutoffTime: Long) {
        commandHistoryDao.cleanupOldHistory(cutoffTime)
        commandHistoryDao.cleanupOldOutputs(cutoffTime)
        aiConversationDao.cleanupOldConversations(cutoffTime)
        systemMonitoringDao.cleanupOldSnapshots(cutoffTime)
        systemMonitoringDao.cleanupOldNetworkHistory(cutoffTime)
        systemMonitoringDao.cleanupOldAppActions(cutoffTime)
        systemMonitoringDao.cleanupOldFileOperations(cutoffTime)
        systemMonitoringDao.cleanupOldDatabaseQueries(cutoffTime)
    }
    
    // Session Management
    fun generateSessionId(): String = UUID.randomUUID().toString()
    
    suspend fun getAllSessionIds(): List<String> = aiConversationDao.getAllSessionIds()
}