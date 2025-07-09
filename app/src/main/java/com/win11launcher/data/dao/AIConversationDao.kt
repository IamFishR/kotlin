package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.AIConversation
import kotlinx.coroutines.flow.Flow

@Dao
interface AIConversationDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC")
    fun getAllAIConversations(): Flow<List<AIConversation>>
    
    @Query("SELECT * FROM ai_conversations WHERE session_id = :sessionId ORDER BY timestamp DESC")
    fun getAIConversationsBySession(sessionId: String): Flow<List<AIConversation>>
    
    @Query("SELECT * FROM ai_conversations WHERE conversation_type = :type ORDER BY timestamp DESC")
    fun getAIConversationsByType(type: String): Flow<List<AIConversation>>
    
    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    suspend fun getAIConversationById(id: String): AIConversation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIConversation(aiConversation: AIConversation)
    
    @Update
    suspend fun updateAIConversation(aiConversation: AIConversation)
    
    @Delete
    suspend fun deleteAIConversation(aiConversation: AIConversation)
    
    @Query("DELETE FROM ai_conversations WHERE session_id = :sessionId")
    suspend fun deleteAIConversationsBySession(sessionId: String)
    
    @Query("DELETE FROM ai_conversations WHERE timestamp < :timestamp")
    suspend fun deleteOldAIConversations(timestamp: Long)
    
    // Search and Filter Operations
    @Query("SELECT * FROM ai_conversations WHERE prompt LIKE :searchTerm OR response LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchAIConversations(searchTerm: String, limit: Int = 100): List<AIConversation>
    
    @Query("SELECT * FROM ai_conversations WHERE user_rating = :rating ORDER BY timestamp DESC")
    fun getAIConversationsByRating(rating: Int): Flow<List<AIConversation>>
    
    @Query("SELECT * FROM ai_conversations WHERE user_rating IS NOT NULL ORDER BY timestamp DESC")
    fun getRatedAIConversations(): Flow<List<AIConversation>>
    
    // Analytics and Statistics
    @Query("SELECT COUNT(*) FROM ai_conversations")
    suspend fun getTotalAIConversationCount(): Int
    
    @Query("SELECT COUNT(*) FROM ai_conversations WHERE conversation_type = :type")
    suspend fun getAIConversationCountByType(type: String): Int
    
    @Query("SELECT AVG(processing_time_ms) FROM ai_conversations WHERE conversation_type = :type")
    suspend fun getAverageProcessingTime(type: String): Float?
    
    @Query("SELECT AVG(CAST(user_rating AS FLOAT)) FROM ai_conversations WHERE user_rating IS NOT NULL")
    suspend fun getAverageUserRating(): Float?
    
    @Query("SELECT SUM(tokens_used) FROM ai_conversations WHERE tokens_used IS NOT NULL")
    suspend fun getTotalTokensUsed(): Int?
    
    @Query("SELECT conversation_type, COUNT(*) as count FROM ai_conversations GROUP BY conversation_type")
    suspend fun getConversationTypeStatistics(): List<ConversationTypeStats>
    
    @Query("SELECT model_used, COUNT(*) as count FROM ai_conversations GROUP BY model_used")
    suspend fun getModelUsageStatistics(): List<ModelUsageStats>
    
    // Context and Command Generation
    @Query("SELECT * FROM ai_conversations WHERE generated_commands IS NOT NULL ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAIConversationsWithGeneratedCommands(limit: Int = 50): List<AIConversation>
    
    @Query("SELECT * FROM ai_conversations WHERE context_data IS NOT NULL ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAIConversationsWithContext(limit: Int = 50): List<AIConversation>
    
    // Session Management
    @Query("SELECT DISTINCT session_id FROM ai_conversations ORDER BY timestamp DESC")
    suspend fun getAllSessionIds(): List<String>
    
    @Query("SELECT COUNT(DISTINCT session_id) FROM ai_conversations")
    suspend fun getTotalSessionCount(): Int
    
    @Query("SELECT * FROM ai_conversations WHERE session_id = :sessionId AND conversation_type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestConversationInSession(sessionId: String, type: String): AIConversation?
    
    // Performance Monitoring
    @Query("SELECT * FROM ai_conversations WHERE processing_time_ms > :threshold ORDER BY processing_time_ms DESC")
    suspend fun getSlowAIConversations(threshold: Long): List<AIConversation>
    
    @Query("SELECT MIN(processing_time_ms) as min_time, MAX(processing_time_ms) as max_time, AVG(processing_time_ms) as avg_time FROM ai_conversations")
    suspend fun getProcessingTimeStatistics(): ProcessingTimeStats?
    
    // Cleanup operations
    @Query("DELETE FROM ai_conversations WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldConversations(cutoffTime: Long)
    
    @Query("DELETE FROM ai_conversations WHERE user_rating < :minRating")
    suspend fun cleanupLowRatedConversations(minRating: Int)
}

// Data classes for complex queries
data class ConversationTypeStats(
    val conversation_type: String,
    val count: Int
)

data class ModelUsageStats(
    val model_used: String,
    val count: Int
)

data class ProcessingTimeStats(
    val min_time: Long,
    val max_time: Long,
    val avg_time: Float
)