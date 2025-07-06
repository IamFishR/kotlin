package com.win11launcher.data.repositories

import com.win11launcher.data.dao.NotificationDao
import com.win11launcher.data.dao.PackageCount
// Removed CategoryCount import as AI functionality is removed
import com.win11launcher.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    
    // Insert operations
    suspend fun insertNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }
    
    suspend fun insertNotifications(notifications: List<NotificationEntity>): List<Long> {
        return notificationDao.insertNotifications(notifications)
    }
    
    // Update operations
    suspend fun updateNotification(notification: NotificationEntity) {
        notificationDao.updateNotification(notification)
    }
    
    
    suspend fun markNotesCreated(id: String, notesCreated: Boolean, createdAt: Long, noteIds: String?) {
        notificationDao.markNotesCreated(id, notesCreated, createdAt, noteIds)
    }
    
    // Removed AI-related user interaction methods
    
    // Query operations
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }
    
    fun getActiveNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getActiveNotifications()
    }
    
    fun getArchivedNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getArchivedNotifications()
    }
    
    suspend fun getNotificationsPaged(limit: Int, offset: Int): List<NotificationEntity> {
        return notificationDao.getNotificationsPaged(limit, offset)
    }
    
    suspend fun getNotificationById(id: String): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }
    
    suspend fun getNotificationByNotificationId(notificationId: String): NotificationEntity? {
        return notificationDao.getNotificationByNotificationId(notificationId)
    }
    
    suspend fun getNotificationByKey(notificationKey: String): NotificationEntity? {
        return notificationDao.getNotificationByKey(notificationKey)
    }
    
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByPackage(packageName)
    }
    
    // Duplicate checking
    suspend fun isDuplicateNotification(packageName: String, title: String, content: String): Boolean {
        return notificationDao.findDuplicateNotification(packageName, title, content) != null
    }
    
    suspend fun isDuplicateByNotificationId(notificationId: String): Boolean {
        return notificationDao.countByNotificationId(notificationId) > 0
    }
    
    suspend fun isDuplicateByNotificationKey(notificationKey: String): Boolean {
        return notificationDao.countByNotificationKey(notificationKey) > 0
    }
    
    // Analytics operations
    suspend fun getTotalNotificationsCount(): Int {
        return notificationDao.getTotalNotificationsCount()
    }
    
    
    suspend fun getNotesCreatedCount(): Int {
        return notificationDao.getNotesCreatedCount()
    }
    
    // Removed getUserInterestCount as it's related to AI functionality
    
    suspend fun getNotificationCountsByPackage(): List<PackageCount> {
        return notificationDao.getNotificationCountsByPackage()
    }
    
    // Removed getNotificationCountsByCategory as AI functionality is removed
    
    
    // Removed getAverageUserRating as it's related to AI functionality
    
    // Search operations
    fun searchNotifications(searchQuery: String): Flow<List<NotificationEntity>> {
        return notificationDao.searchNotifications(searchQuery)
    }
    
    
    fun getNotificationsByNotesCreated(notesCreated: Boolean): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByNotesCreated(notesCreated)
    }
    
    // Removed getNotificationsByUserInterest as it's related to AI functionality
    
    // Removed getNotificationsByCategory as AI functionality is removed
    
    fun getNotificationsByTimeRange(startTime: Long, endTime: Long): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByTimeRange(startTime, endTime)
    }
    
    // Cleanup operations
    suspend fun softDeleteNotification(id: String) {
        notificationDao.softDeleteNotification(id)
    }
    
    suspend fun hardDeleteNotification(id: String) {
        notificationDao.hardDeleteNotification(id)
    }
    
    suspend fun cleanupSoftDeletedNotifications(cutoffTime: Long) {
        notificationDao.cleanupSoftDeletedNotifications(cutoffTime)
    }
    
    suspend fun cleanupOldNotifications(cutoffTime: Long) {
        notificationDao.cleanupOldNotifications(cutoffTime)
    }
    
    // Utility methods
    fun getNotificationAnalytics(): Flow<NotificationAnalytics> {
        return notificationDao.getAllNotifications().map { notificationList ->
            NotificationAnalytics(
                total = notificationList.size,
                notesCreated = notificationList.count { it.notesCreated },
                archived = notificationList.count { it.isArchived }
            )
        }
    }
}

data class NotificationAnalytics(
    val total: Int,
    val notesCreated: Int,
    val archived: Int
)