package com.win11launcher.services

import com.win11launcher.data.dao.AIMemoryDao
import com.win11launcher.data.entities.ShortTermMemory
import com.win11launcher.data.entities.LongTermMemory
import com.win11launcher.data.entities.Reflection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIMemoryManager @Inject constructor(
    private val aiMemoryDao: AIMemoryDao,
    private val aiService: AIService
) {
    
    companion object {
        private const val MAX_SHORT_TERM_MESSAGES = 50
        private const val MAX_TOKENS_PER_CONVERSATION = 8000
        private const val MIN_IMPORTANCE_SCORE = 0.3f
        private const val MEMORY_DECAY_DAYS = 30
        private const val REFLECTION_TRIGGER_THRESHOLD = 10
    }
    
    // Short-term Memory Management
    suspend fun saveToShortTermMemory(
        conversationId: String,
        sender: String,
        content: String,
        messageType: String = "TEXT",
        contextData: String? = null
    ): String = withContext(Dispatchers.IO) {
        val messageId = UUID.randomUUID().toString()
        val messageOrder = aiMemoryDao.getShortTermMemoryCount(conversationId)
        val tokenCount = estimateTokenCount(content)
        val importanceScore = calculateImportanceScore(content, sender, messageType)
        
        val memory = ShortTermMemory(
            messageId = messageId,
            conversationId = conversationId,
            messageOrder = messageOrder,
            sender = sender,
            contentText = content,
            tokenCount = tokenCount,
            timestamp = System.currentTimeMillis(),
            messageType = messageType,
            importanceScore = importanceScore,
            contextData = contextData
        )
        
        aiMemoryDao.insertShortTermMemory(memory)
        
        // Manage memory capacity
        manageShortTermMemoryCapacity(conversationId)
        
        // Trigger reflection if enough messages accumulated
        if (messageOrder > 0 && messageOrder % REFLECTION_TRIGGER_THRESHOLD == 0) {
            generateReflection(conversationId, "USER_DEFAULT")
        }
        
        messageId
    }
    
    suspend fun getConversationContext(conversationId: String, maxMessages: Int = 20): String = withContext(Dispatchers.IO) {
        val recentMemories = aiMemoryDao.getRecentShortTermMemory(conversationId, maxMessages)
        
        buildString {
            appendLine("=== Recent Conversation Context ===")
            recentMemories.reversed().forEach { memory ->
                appendLine("${memory.sender.uppercase()}: ${memory.contentText}")
                if (memory.contextData != null) {
                    appendLine("  Context: ${memory.contextData}")
                }
                appendLine()
            }
        }
    }
    
    suspend fun getShortTermMemoryFlow(conversationId: String): Flow<List<ShortTermMemory>> {
        return aiMemoryDao.getShortTermMemoryByConversation(conversationId)
    }
    
    private suspend fun manageShortTermMemoryCapacity(conversationId: String) {
        val messageCount = aiMemoryDao.getShortTermMemoryCount(conversationId)
        val tokenCount = aiMemoryDao.getTotalTokenCount(conversationId) ?: 0
        
        // If we exceed limits, promote important messages to long-term memory
        if (messageCount > MAX_SHORT_TERM_MESSAGES || tokenCount > MAX_TOKENS_PER_CONVERSATION) {
            promoteToLongTermMemory(conversationId)
        }
    }
    
    // Long-term Memory Management
    suspend fun promoteToLongTermMemory(conversationId: String, userId: String = "USER_DEFAULT") = withContext(Dispatchers.IO) {
        val oldMemories = aiMemoryDao.getOldestShortTermMemories(conversationId, REFLECTION_TRIGGER_THRESHOLD)
        val importantMemories = oldMemories.filter { it.importanceScore >= MIN_IMPORTANCE_SCORE }
        
        if (importantMemories.isNotEmpty()) {
            // Synthesize memories into long-term knowledge
            val synthesizedMemory = synthesizeMemories(importantMemories, userId)
            aiMemoryDao.insertLongTermMemory(synthesizedMemory)
            
            // Clean up old short-term memories
            val minOrder = oldMemories.minOfOrNull { it.messageOrder } ?: 0
            aiMemoryDao.deleteOldShortTermMemory(conversationId, minOrder)
        }
    }
    
    suspend fun retrieveRelevantMemories(userId: String, query: String, limit: Int = 10): List<LongTermMemory> = withContext(Dispatchers.IO) {
        val searchQuery = "%$query%"
        val memories = aiMemoryDao.searchLongTermMemory(userId, searchQuery, limit)
        
        // Update access times for retrieved memories
        memories.forEach { memory ->
            aiMemoryDao.updateMemoryAccess(memory.memoryId, System.currentTimeMillis())
        }
        
        memories
    }
    
    suspend fun getLongTermMemoryFlow(userId: String): Flow<List<LongTermMemory>> {
        return aiMemoryDao.getLongTermMemoryByUser(userId)
    }
    
    suspend fun getMemoryByCategory(userId: String, category: String): List<LongTermMemory> = withContext(Dispatchers.IO) {
        aiMemoryDao.getLongTermMemoryByCategory(userId, category)
    }
    
    private suspend fun synthesizeMemories(memories: List<ShortTermMemory>, userId: String): LongTermMemory {
        val conversationIds = memories.map { it.conversationId }.distinct()
        val combinedText = memories.joinToString("\n") { "${it.sender}: ${it.contentText}" }
        
        // Use AI to synthesize the memories into a coherent long-term memory
        val synthesisPrompt = buildString {
            appendLine("Analyze the following conversation fragments and create a concise summary of key information, patterns, or insights:")
            appendLine()
            appendLine(combinedText)
            appendLine()
            appendLine("Focus on:")
            appendLine("- User preferences and behaviors")
            appendLine("- Important facts or decisions")
            appendLine("- Command patterns or workflows")
            appendLine("- System insights or recommendations")
            appendLine()
            appendLine("Provide a clear, actionable summary:")
        }
        
        val synthesizedText = try {
            val aiResponse = aiService.generateResponse(synthesisPrompt)
            if (aiResponse.success) {
                aiResponse.response
            } else {
                "Synthesized memory from ${memories.size} messages: ${combinedText.take(500)}..."
            }
        } catch (e: Exception) {
            "Synthesized memory from ${memories.size} messages: ${combinedText.take(500)}..."
        }
        
        val avgImportance = memories.map { it.importanceScore }.average().toFloat()
        val keywords = extractKeywords(combinedText)
        
        return LongTermMemory(
            memoryId = UUID.randomUUID().toString(),
            userId = userId,
            memoryText = synthesizedText,
            memoryType = "synthesized",
            importanceScore = avgImportance,
            lastAccessed = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            sourceConversations = conversationIds.joinToString(","),
            keywords = keywords,
            category = determineCategory(combinedText)
        )
    }
    
    // Reflection Management
    suspend fun generateReflection(conversationId: String, userId: String): String = withContext(Dispatchers.IO) {
        val recentMemories = aiMemoryDao.getRecentShortTermMemory(conversationId, REFLECTION_TRIGGER_THRESHOLD)
        
        if (recentMemories.isEmpty()) return@withContext ""
        
        val reflectionPrompt = buildString {
            appendLine("Analyze the following conversation pattern and provide insights:")
            appendLine()
            recentMemories.reversed().forEach { memory ->
                appendLine("${memory.sender}: ${memory.contentText}")
            }
            appendLine()
            appendLine("Generate insights about:")
            appendLine("- User behavior patterns")
            appendLine("- Command usage trends")
            appendLine("- Potential optimizations")
            appendLine("- System recommendations")
            appendLine()
            appendLine("Provide actionable insights:")
        }
        
        val reflectionText = try {
            val aiResponse = aiService.generateResponse(reflectionPrompt)
            if (aiResponse.success) {
                aiResponse.response
            } else {
                "Pattern analysis of ${recentMemories.size} recent messages in conversation"
            }
        } catch (e: Exception) {
            "Pattern analysis of ${recentMemories.size} recent messages in conversation"
        }
        
        val reflection = Reflection(
            reflectionId = UUID.randomUUID().toString(),
            userId = userId,
            reflectionText = reflectionText,
            reflectionType = "PATTERN",
            triggeringMessageIds = recentMemories.map { it.messageId }.joinToString(","),
            createdAt = System.currentTimeMillis(),
            confidenceScore = 0.8f,
            actionable = containsActionableInsights(reflectionText),
            contextData = "{\"conversation_id\":\"$conversationId\",\"message_count\":${recentMemories.size}}"
        )
        
        aiMemoryDao.insertReflection(reflection)
        reflection.reflectionId
    }
    
    suspend fun getReflections(userId: String): Flow<List<Reflection>> {
        return aiMemoryDao.getReflectionsByUser(userId)
    }
    
    suspend fun getActionableReflections(userId: String): List<Reflection> = withContext(Dispatchers.IO) {
        aiMemoryDao.getActionableReflections(userId)
    }
    
    suspend fun markReflectionImplemented(reflectionId: String) = withContext(Dispatchers.IO) {
        aiMemoryDao.markReflectionImplemented(reflectionId)
    }
    
    // Memory Maintenance
    suspend fun performMemoryMaintenance(userId: String) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (MEMORY_DECAY_DAYS * 24 * 60 * 60 * 1000L)
        
        // Apply memory decay
        aiMemoryDao.applyMemoryDecay(userId, cutoffTime)
        
        // Clean up low importance memories
        aiMemoryDao.cleanupLowImportanceMemories(userId, MIN_IMPORTANCE_SCORE)
        
        // Clean up old reflections
        aiMemoryDao.cleanupOldReflections(userId, cutoffTime)
    }
    
    suspend fun getMemoryStatistics(userId: String): MemoryStatistics = withContext(Dispatchers.IO) {
        val longTermCount = aiMemoryDao.getLongTermMemoryCount(userId)
        val reflectionCount = aiMemoryDao.getReflectionCount(userId)
        val averageImportance = aiMemoryDao.getAverageMemoryImportance(userId) ?: 0.0f
        val memoryTypes = aiMemoryDao.getMemoryTypeDistribution(userId)
        val reflectionTypes = aiMemoryDao.getReflectionTypeDistribution(userId)
        
        MemoryStatistics(
            longTermMemoryCount = longTermCount,
            reflectionCount = reflectionCount,
            averageImportance = averageImportance,
            memoryTypeDistribution = memoryTypes,
            reflectionTypeDistribution = reflectionTypes
        )
    }
    
    // Utility Methods
    private fun estimateTokenCount(text: String): Int {
        // Simple approximation: ~4 characters per token
        return (text.length / 4).coerceAtLeast(1)
    }
    
    private fun calculateImportanceScore(content: String, sender: String, messageType: String): Float {
        var score = 0.5f
        
        // Boost importance for certain message types
        when (messageType) {
            "COMMAND" -> score += 0.2f
            "SYSTEM_INFO" -> score += 0.1f
            "ERROR" -> score += 0.3f
        }
        
        // Boost for AI responses (they're processed and valuable)
        if (sender == "ai") score += 0.1f
        
        // Boost for longer content (more informative)
        if (content.length > 100) score += 0.1f
        if (content.length > 300) score += 0.1f
        
        // Boost for technical keywords
        val technicalKeywords = listOf("error", "command", "system", "performance", "battery", "network", "memory")
        if (technicalKeywords.any { content.lowercase().contains(it) }) {
            score += 0.1f
        }
        
        return score.coerceIn(0.0f, 1.0f)
    }
    
    private fun extractKeywords(text: String): String {
        val words = text.lowercase().split(Regex("\\W+"))
        val commonWords = setOf("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "a", "an", "is", "was", "are", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "can", "may", "might", "must")
        val keywords = words.filter { it.length > 3 && !commonWords.contains(it) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
        
        return keywords.joinToString(", ")
    }
    
    private fun determineCategory(text: String): String {
        val lowercaseText = text.lowercase()
        
        return when {
            lowercaseText.contains("command") || lowercaseText.contains("execute") -> "COMMAND"
            lowercaseText.contains("system") || lowercaseText.contains("performance") -> "SYSTEM"
            lowercaseText.contains("prefer") || lowercaseText.contains("like") || lowercaseText.contains("want") -> "PREFERENCE"
            lowercaseText.contains("user") || lowercaseText.contains("personal") -> "USER"
            else -> "GENERAL"
        }
    }
    
    private fun containsActionableInsights(text: String): Boolean {
        val actionableKeywords = listOf("recommend", "suggest", "should", "could", "optimize", "improve", "fix", "update", "install", "configure")
        return actionableKeywords.any { text.lowercase().contains(it) }
    }
}

data class MemoryStatistics(
    val longTermMemoryCount: Int,
    val reflectionCount: Int,
    val averageImportance: Float,
    val memoryTypeDistribution: List<com.win11launcher.data.dao.MemoryTypeStats>,
    val reflectionTypeDistribution: List<com.win11launcher.data.dao.ReflectionTypeStats>
)