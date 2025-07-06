package com.win11launcher.data.repositories

import com.win11launcher.data.dao.NotificationDao
import com.win11launcher.data.dao.PackageCount
import com.win11launcher.data.dao.CategoryCount
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
    
    suspend fun markAsAiProcessed(id: String, isProcessed: Boolean, processedAt: Long, result: String?) {
        notificationDao.markAsAiProcessed(id, isProcessed, processedAt, result)
    }
    
    suspend fun markNotesCreated(id: String, notesCreated: Boolean, createdAt: Long, noteIds: String?) {
        notificationDao.markNotesCreated(id, notesCreated, createdAt, noteIds)
    }
    
    suspend fun markUserInteraction(id: String, showedInterest: Boolean, interactionType: String?, interactionAt: Long, rating: Int?, notes: String?) {
        notificationDao.markUserInteraction(id, showedInterest, interactionType, interactionAt, rating, notes)
    }
    
    suspend fun updateAiAnalysis(id: String, importance: Float?, sentiment: Float?, urgency: Float?, category: String?, tags: String?) {
        notificationDao.updateAiAnalysis(id, importance, sentiment, urgency, category, tags)
    }
    
    suspend fun archiveNotification(id: String, isArchived: Boolean, archivedAt: Long?) {
        notificationDao.archiveNotification(id, isArchived, archivedAt)
    }
    
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
    
    suspend fun getAiProcessedCount(): Int {
        return notificationDao.getAiProcessedCount()
    }
    
    suspend fun getNotesCreatedCount(): Int {
        return notificationDao.getNotesCreatedCount()
    }
    
    suspend fun getUserInterestCount(): Int {
        return notificationDao.getUserInterestCount()
    }
    
    suspend fun getNotificationCountsByPackage(): List<PackageCount> {
        return notificationDao.getNotificationCountsByPackage()
    }
    
    suspend fun getNotificationCountsByCategory(): List<CategoryCount> {
        return notificationDao.getNotificationCountsByCategory()
    }
    
    suspend fun getAverageImportanceScore(): Float? {
        return notificationDao.getAverageImportanceScore()
    }
    
    suspend fun getAverageUserRating(): Float? {
        return notificationDao.getAverageUserRating()
    }
    
    // Search operations
    fun searchNotifications(searchQuery: String): Flow<List<NotificationEntity>> {
        return notificationDao.searchNotifications(searchQuery)
    }
    
    fun getNotificationsByAiProcessed(isProcessed: Boolean): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByAiProcessed(isProcessed)
    }
    
    fun getNotificationsByNotesCreated(notesCreated: Boolean): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByNotesCreated(notesCreated)
    }
    
    fun getNotificationsByUserInterest(showedInterest: Boolean): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByUserInterest(showedInterest)
    }
    
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByCategory(category)
    }
    
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
                aiProcessed = notificationList.count { it.isAiProcessed },
                notesCreated = notificationList.count { it.notesCreated },
                userInterest = notificationList.count { it.userShowedInterest },
                archived = notificationList.count { it.isArchived },
                averageImportance = notificationList.mapNotNull { it.importanceScore }.average().takeIf { !it.isNaN() }?.toFloat(),
                averageUserRating = notificationList.mapNotNull { it.userRating?.toDouble() }.average().takeIf { !it.isNaN() }?.toFloat()
            )
        }
    }
}

data class NotificationAnalytics(
    val total: Int,
    val aiProcessed: Int,
    val notesCreated: Int,
    val userInterest: Int,
    val archived: Int,
    val averageImportance: Float?,
    val averageUserRating: Float?
)