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
    
    
    @Query("UPDATE all_notifications SET notes_created = :notesCreated, notes_created_at = :createdAt, note_ids = :noteIds, updated_at = :updatedAt WHERE id = :id")
    suspend fun markNotesCreated(id: String, notesCreated: Boolean, createdAt: Long, noteIds: String?, updatedAt: Long = System.currentTimeMillis())
    
    // Removed AI-related user interaction methods
    
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
    
    
    @Query("SELECT COUNT(*) FROM all_notifications WHERE notes_created = 1 AND deleted_at IS NULL")
    suspend fun getNotesCreatedCount(): Int
    
    // Removed getUserInterestCount as it's related to AI functionality
    
    @Query("SELECT source_package, COUNT(*) as count FROM all_notifications WHERE deleted_at IS NULL GROUP BY source_package ORDER BY count DESC")
    suspend fun getNotificationCountsByPackage(): List<PackageCount>
    
    // Removed auto_category query as AI functionality is removed
    
    
    // Removed getAverageUserRating as it's related to AI functionality
    
    // Search queries
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR source_app_name LIKE '%' || :searchQuery || '%') ORDER BY timestamp DESC")
    fun searchNotifications(searchQuery: String): Flow<List<NotificationEntity>>
    
    
    @Query("SELECT * FROM all_notifications WHERE deleted_at IS NULL AND notes_created = :notesCreated ORDER BY timestamp DESC")
    fun getNotificationsByNotesCreated(notesCreated: Boolean): Flow<List<NotificationEntity>>
    
    // Removed getNotificationsByUserInterest as it's related to AI functionality
    
    // Removed auto_category query as AI functionality is removed
    
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

// Removed CategoryCount as AI functionality is removed