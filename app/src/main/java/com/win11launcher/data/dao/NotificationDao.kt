package com.win11launcher.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.win11launcher.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>): List<Long>
    
    // Update operations
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Query("UPDATE all_notifications SET is_ai_processed = :isProcessed, ai_processed_at = :processedAt, ai_processing_result = :result, updated_at = :updatedAt WHERE id = :id")
    suspend fun markAsAiProcessed(id: String, isProcessed: Boolean, processedAt: Long, result: String?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE all_notifications SET notes_created = :notesCreated, notes_created_at = :createdAt, note_ids = :noteIds, updated_at = :updatedAt WHERE id = :id")
    suspend fun markNotesCreated(id: String, notesCreated: Boolean, createdAt: Long, noteIds: String?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE all_notifications SET user_showed_interest = :showedInterest, user_interaction_type = :interactionType, user_interaction_at = :interactionAt, user_rating = :rating, user_notes = :notes, updated_at = :updatedAt WHERE id = :id")
    suspend fun markUserInteraction(id: String, showedInterest: Boolean, interactionType: String?, interactionAt: Long, rating: Int?, notes: String?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE all_notifications SET importance_score = :importance, sentiment_score = :sentiment, urgency_score = :urgency, auto_category = :category, auto_tags = :tags, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateAiAnalysis(id: String, importance: Float?, sentiment: Float?, urgency: Float?, category: String?, tags: String?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE all_notifications SET is_archived = :isArchived, archived_at = :archivedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun archiveNotification(id: String, isArchived: Boolean, archivedAt: Long?, updatedAt: Long = System.currentTimeMillis())
    
    // Query operations
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND is_archived = 0 ORDER BY timestamp DESC")
    fun getActiveNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND is_archived = 1 ORDER BY timestamp DESC")
    fun getArchivedNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotificationsPaged(limit: Int, offset: Int): List<NotificationEntity>
    
    @Query("SELECT * FROM all_notifications WHERE id = :id AND deleted_at IS NULL")
    suspend fun getNotificationById(id: String): NotificationEntity?
    
    @Query("SELECT * FROM all_notifications WHERE notification_id = :notificationId AND deleted_at IS NULL")
    suspend fun getNotificationByNotificationId(notificationId: String): NotificationEntity?
    
    @Query("SELECT * FROM all_notifications WHERE notification_key = :notificationKey AND deleted_at IS NULL")
    suspend fun getNotificationByKey(notificationKey: String): NotificationEntity?
    
    @Query("SELECT * FROM all_notifications WHERE source_package = :packageName AND deleted_at IS NULL ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE source_package = :packageName AND title = :title AND content = :content AND deleted_at IS NULL ORDER BY timestamp DESC LIMIT 1")
    suspend fun findDuplicateNotification(packageName: String, title: String, content: String): NotificationEntity?
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM all_notifications WHERE deleted_at IS NULL")
    suspend fun getTotalNotificationsCount(): Int
    
    @Query("SELECT COUNT(*) FROM all_notifications WHERE is_ai_processed = 1 AND deleted_at IS NULL")
    suspend fun getAiProcessedCount(): Int
    
    @Query("SELECT COUNT(*) FROM all_notifications WHERE notes_created = 1 AND deleted_at IS NULL")
    suspend fun getNotesCreatedCount(): Int
    
    @Query("SELECT COUNT(*) FROM all_notifications WHERE user_showed_interest = 1 AND deleted_at IS NULL")
    suspend fun getUserInterestCount(): Int
    
    @Query("SELECT source_package, COUNT(*) as count FROM all_notifications WHERE deleted_at IS NULL GROUP BY source_package ORDER BY count DESC")
    suspend fun getNotificationCountsByPackage(): List<PackageCount>
    
    @Query("SELECT auto_category, COUNT(*) as count FROM all_notifications WHERE auto_category IS NOT NULL AND deleted_at IS NULL GROUP BY auto_category ORDER BY count DESC")
    suspend fun getNotificationCountsByCategory(): List<CategoryCount>
    
    @Query("SELECT AVG(importance_score) FROM all_notifications WHERE importance_score IS NOT NULL AND deleted_at IS NULL")
    suspend fun getAverageImportanceScore(): Float?
    
    @Query("SELECT AVG(user_rating) FROM all_notifications WHERE user_rating IS NOT NULL AND deleted_at IS NULL")
    suspend fun getAverageUserRating(): Float?
    
    // Search queries
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR source_app_name LIKE '%' || :searchQuery || '%') ORDER BY timestamp DESC")
    fun searchNotifications(searchQuery: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND is_ai_processed = :isProcessed ORDER BY timestamp DESC")
    fun getNotificationsByAiProcessed(isProcessed: Boolean): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND notes_created = :notesCreated ORDER BY timestamp DESC")
    fun getNotificationsByNotesCreated(notesCreated: Boolean): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND user_showed_interest = :showedInterest ORDER BY timestamp DESC")
    fun getNotificationsByUserInterest(showedInterest: Boolean): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND auto_category = :category ORDER BY timestamp DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getNotificationsByTimeRange(startTime: Long, endTime: Long): Flow<List<NotificationEntity>>
    
    // Cleanup operations
    @Query("UPDATE all_notifications SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDeleteNotification(id: String, deletedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM all_notifications WHERE id = :id")
    suspend fun hardDeleteNotification(id: String)
    
    @Query("DELETE FROM all_notifications WHERE deleted_at IS NOT NULL AND deleted_at < :cutoffTime")
    suspend fun cleanupSoftDeletedNotifications(cutoffTime: Long)
    
    @Query("DELETE FROM all_notifications WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldNotifications(cutoffTime: Long)
    
    // Duplicate checking
    @Query("SELECT COUNT(*) FROM all_notifications WHERE notification_id = :notificationId AND deleted_at IS NULL")
    suspend fun countByNotificationId(notificationId: String): Int
    
    @Query("SELECT COUNT(*) FROM all_notifications WHERE notification_key = :notificationKey AND deleted_at IS NULL")
    suspend fun countByNotificationKey(notificationKey: String): Int
}

// Data classes for aggregated results
data class PackageCount(
    val source_package: String,
    val count: Int
)

data class CategoryCount(
    val auto_category: String,
    val count: Int
)