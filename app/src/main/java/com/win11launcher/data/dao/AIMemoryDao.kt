package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.ShortTermMemory
import com.win11launcher.data.entities.LongTermMemory
import com.win11launcher.data.entities.Reflection
import kotlinx.coroutines.flow.Flow

@Dao
interface AIMemoryDao {
    
    // Short-term Memory Operations
    @Query("SELECT * FROM short_term_memory WHERE conversation_id = :conversationId ORDER BY message_order ASC")
    fun getShortTermMemoryByConversation(conversationId: String): Flow<List<ShortTermMemory>>
    
    @Query("SELECT * FROM short_term_memory WHERE conversation_id = :conversationId AND message_order >= :fromOrder ORDER BY message_order ASC")
    suspend fun getShortTermMemoryFromOrder(conversationId: String, fromOrder: Int): List<ShortTermMemory>
    
    @Query("SELECT * FROM short_term_memory WHERE conversation_id = :conversationId ORDER BY message_order DESC LIMIT :limit")
    suspend fun getRecentShortTermMemory(conversationId: String, limit: Int): List<ShortTermMemory>
    
    @Query("SELECT COUNT(*) FROM short_term_memory WHERE conversation_id = :conversationId")
    suspend fun getShortTermMemoryCount(conversationId: String): Int
    
    @Query("SELECT SUM(token_count) FROM short_term_memory WHERE conversation_id = :conversationId")
    suspend fun getTotalTokenCount(conversationId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortTermMemory(memory: ShortTermMemory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortTermMemories(memories: List<ShortTermMemory>)
    
    @Update
    suspend fun updateShortTermMemory(memory: ShortTermMemory)
    
    @Delete
    suspend fun deleteShortTermMemory(memory: ShortTermMemory)
    
    @Query("DELETE FROM short_term_memory WHERE conversation_id = :conversationId AND message_order < :beforeOrder")
    suspend fun deleteOldShortTermMemory(conversationId: String, beforeOrder: Int)
    
    @Query("DELETE FROM short_term_memory WHERE conversation_id = :conversationId")
    suspend fun clearShortTermMemory(conversationId: String)
    
    // Long-term Memory Operations
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId ORDER BY importance_score DESC, last_accessed DESC")
    fun getLongTermMemoryByUser(userId: String): Flow<List<LongTermMemory>>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId AND memory_type = :memoryType ORDER BY importance_score DESC")
    suspend fun getLongTermMemoryByType(userId: String, memoryType: String): List<LongTermMemory>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId AND category = :category ORDER BY importance_score DESC")
    suspend fun getLongTermMemoryByCategory(userId: String, category: String): List<LongTermMemory>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId AND (memory_text LIKE :query OR keywords LIKE :query) ORDER BY importance_score DESC LIMIT :limit")
    suspend fun searchLongTermMemory(userId: String, query: String, limit: Int = 20): List<LongTermMemory>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId AND importance_score >= :minScore ORDER BY importance_score DESC LIMIT :limit")
    suspend fun getHighImportanceMemories(userId: String, minScore: Float, limit: Int = 10): List<LongTermMemory>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId AND last_accessed < :cutoffTime ORDER BY importance_score ASC")
    suspend fun getUnusedMemories(userId: String, cutoffTime: Long): List<LongTermMemory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLongTermMemory(memory: LongTermMemory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLongTermMemories(memories: List<LongTermMemory>)
    
    @Update
    suspend fun updateLongTermMemory(memory: LongTermMemory)
    
    @Delete
    suspend fun deleteLongTermMemory(memory: LongTermMemory)
    
    @Query("UPDATE long_term_memory SET last_accessed = :accessTime, access_count = access_count + 1 WHERE memoryId = :memoryId")
    suspend fun updateMemoryAccess(memoryId: String, accessTime: Long)
    
    @Query("UPDATE long_term_memory SET importance_score = importance_score * decay_factor WHERE user_id = :userId AND last_accessed < :cutoffTime")
    suspend fun applyMemoryDecay(userId: String, cutoffTime: Long)
    
    @Query("DELETE FROM long_term_memory WHERE user_id = :userId AND importance_score < :minScore")
    suspend fun cleanupLowImportanceMemories(userId: String, minScore: Float = 0.1f)
    
    // Reflection Operations
    @Query("SELECT * FROM reflections WHERE user_id = :userId ORDER BY created_at DESC")
    fun getReflectionsByUser(userId: String): Flow<List<Reflection>>
    
    @Query("SELECT * FROM reflections WHERE user_id = :userId AND reflection_type = :reflectionType ORDER BY created_at DESC")
    suspend fun getReflectionsByType(userId: String, reflectionType: String): List<Reflection>
    
    @Query("SELECT * FROM reflections WHERE user_id = :userId AND actionable = 1 AND implemented = 0 ORDER BY confidence_score DESC")
    suspend fun getActionableReflections(userId: String): List<Reflection>
    
    @Query("SELECT * FROM reflections WHERE user_id = :userId AND confidence_score >= :minScore ORDER BY created_at DESC LIMIT :limit")
    suspend fun getHighConfidenceReflections(userId: String, minScore: Float, limit: Int = 10): List<Reflection>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: Reflection)
    
    @Update
    suspend fun updateReflection(reflection: Reflection)
    
    @Delete
    suspend fun deleteReflection(reflection: Reflection)
    
    @Query("UPDATE reflections SET implemented = 1 WHERE reflectionId = :reflectionId")
    suspend fun markReflectionImplemented(reflectionId: String)
    
    @Query("DELETE FROM reflections WHERE user_id = :userId AND created_at < :cutoffTime")
    suspend fun cleanupOldReflections(userId: String, cutoffTime: Long)
    
    // Analytics and Statistics
    @Query("SELECT COUNT(*) FROM short_term_memory WHERE conversation_id = :conversationId")
    suspend fun getConversationMessageCount(conversationId: String): Int
    
    @Query("SELECT COUNT(*) FROM long_term_memory WHERE user_id = :userId")
    suspend fun getLongTermMemoryCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM reflections WHERE user_id = :userId")
    suspend fun getReflectionCount(userId: String): Int
    
    @Query("SELECT AVG(importance_score) FROM long_term_memory WHERE user_id = :userId")
    suspend fun getAverageMemoryImportance(userId: String): Float?
    
    @Query("SELECT memory_type, COUNT(*) as count FROM long_term_memory WHERE user_id = :userId GROUP BY memory_type")
    suspend fun getMemoryTypeDistribution(userId: String): List<MemoryTypeStats>
    
    @Query("SELECT reflection_type, COUNT(*) as count FROM reflections WHERE user_id = :userId GROUP BY reflection_type")
    suspend fun getReflectionTypeDistribution(userId: String): List<ReflectionTypeStats>
    
    // Memory Capacity Management
    @Query("SELECT * FROM short_term_memory WHERE conversation_id = :conversationId ORDER BY message_order ASC LIMIT :limit")
    suspend fun getOldestShortTermMemories(conversationId: String, limit: Int): List<ShortTermMemory>
    
    @Query("SELECT * FROM long_term_memory WHERE user_id = :userId ORDER BY importance_score ASC, last_accessed ASC LIMIT :limit")
    suspend fun getLeastImportantMemories(userId: String, limit: Int): List<LongTermMemory>
}

// Data classes for analytics
data class MemoryTypeStats(
    val memory_type: String,
    val count: Int
)

data class ReflectionTypeStats(
    val reflection_type: String,
    val count: Int
)