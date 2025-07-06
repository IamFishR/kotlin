package com.win11launcher.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.win11launcher.models.AppNotification
import com.win11launcher.data.entities.NotificationEntity
import com.win11launcher.data.repositories.NotificationRepository
import com.win11launcher.data.database.NotesDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID
import com.google.gson.Gson

@AndroidEntryPoint
class Win11NotificationListenerService : NotificationListenerService() {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var ruleEngine: RuleEngine
    private lateinit var notificationRepository: NotificationRepository
    private val gson = Gson()
    
    companion object {
        private const val TAG = "NotificationListener"
        private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
        val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()
        
        private var serviceInstance: Win11NotificationListenerService? = null
        
        fun isServiceConnected(): Boolean = serviceInstance != null
        
        fun dismissNotification(notificationId: String) {
            serviceInstance?.let { service ->
                val activeNotifications = service.activeNotifications
                activeNotifications.find { it.key == notificationId }?.let {
                    service.cancelNotification(it.key)
                }
            }
        }
        
        fun dismissAllNotifications() {
            serviceInstance?.cancelAllNotifications()
        }
        
    }
    
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
        ruleEngine = RuleEngine(this)
        
        // Initialize notification repository
        val database = NotesDatabase.getDatabase(this)
        notificationRepository = NotificationRepository(database.notificationDao())
        
        Log.d(TAG, "NotificationListenerService created with notification repository")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceInstance = null
        Log.d(TAG, "NotificationListenerService destroyed")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected")
        updateNotificationsList()
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListenerService disconnected")
        serviceInstance = null
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: ${sbn?.packageName}")
        
        // Store notification in database before processing
        sbn?.let { statusBarNotification ->
            coroutineScope.launch {
                try {
                    storeNotificationInDatabase(statusBarNotification)
                } catch (e: Exception) {
                    Log.e(TAG, "Error storing notification in database", e)
                }
            }
        }
        
        updateNotificationsList()
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn?.packageName}")
        updateNotificationsList()
    }
    
    private fun updateNotificationsList() {
        try {
            val activeNotifications = activeNotifications ?: return
            val notificationsList = mutableListOf<AppNotification>()
            
            for (sbn in activeNotifications) {
                val notification = sbn.notification ?: continue
                val packageName = sbn.packageName
                
                // Skip system notifications and ongoing notifications we don't want to show
                if (shouldSkipNotification(packageName, notification)) continue
                
                val appName = getAppName(packageName)
                val title = getNotificationTitle(notification)
                val content = getNotificationContent(notification)
                
                if (title.isNotEmpty() || content.isNotEmpty()) {
                    val appNotification = AppNotification(
                        id = sbn.key,
                        packageName = packageName,
                        appName = appName,
                        title = title,
                        content = content,
                        time = getRelativeTime(sbn.postTime),
                        timestamp = sbn.postTime,
                        smallIcon = notification.smallIcon,
                        isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0,
                        isClearable = sbn.isClearable,
                        contentIntent = notification.contentIntent
                    )
                    
                    notificationsList.add(appNotification)
                    
                    // Process notification through rule engine for notes conversion
                    coroutineScope.launch {
                        try {
                            // Process through rule engine
                            val ruleResult = ruleEngine.processNotification(appNotification)
                            
                            // Mark notes created in database if rule created notes
                            if (ruleResult != null) {
                                updateNotificationNotesStatus(appNotification.id, true)
                            }
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing notification through rule engine", e)
                        }
                    }
                }
            }
            
            // Sort by timestamp (newest first)
            val sortedNotifications = notificationsList.sortedByDescending { it.timestamp }
            _notifications.value = sortedNotifications
            
            Log.d(TAG, "Updated notifications list: ${sortedNotifications.size} notifications")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notifications list", e)
        }
    }
    
    private fun shouldSkipNotification(packageName: String, notification: Notification): Boolean {
        // Skip our own notifications
        if (packageName == this.packageName) return true
        
        // Skip system UI notifications that are not user-relevant
        val systemPackages = setOf(
            "android",
            "com.android.systemui",
            "com.android.providers.downloads"
        )
        
        if (packageName in systemPackages) {
            // Allow some system notifications but skip others
            val title = getNotificationTitle(notification)
            val content = getNotificationContent(notification)
            
            // Skip empty notifications
            if (title.isEmpty() && content.isEmpty()) return true
            
            // Skip certain system notifications
            if (title.contains("USB", ignoreCase = true) && 
                content.contains("charging", ignoreCase = true)) return true
        }
        
        // Skip ongoing notifications that are not important
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
            val title = getNotificationTitle(notification)
            // Skip ongoing notifications like "App is running"
            if (title.contains("running", ignoreCase = true) ||
                title.contains("active", ignoreCase = true)) return true
        }
        
        return false
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".")
        }
    }
    
    private fun getNotificationTitle(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    }
    
    private fun getNotificationContent(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
    }
    
    private fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 604800_000 -> "${diff / 86400_000}d ago"
            else -> timeFormat.format(Date(timestamp))
        }
    }
    
    private suspend fun storeNotificationInDatabase(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val packageName = sbn.packageName
        
        // Check for duplicates first
        val notificationId = sbn.id.toString()
        val notificationKey = sbn.key
        
        // Skip if already exists
        if (notificationRepository.isDuplicateByNotificationId(notificationId) ||
            notificationRepository.isDuplicateByNotificationKey(notificationKey)) {
            Log.d(TAG, "Skipping duplicate notification: $packageName")
            return
        }
        
        // Skip minimal system notifications that provide no value
        if (shouldSkipNotificationForDatabase(packageName, notification)) {
            Log.d(TAG, "Skipping non-valuable notification: $packageName")
            return
        }
        
        try {
            val appName = getAppName(packageName)
            val title = getNotificationTitle(notification)
            val content = getNotificationContent(notification)
            val subText = getNotificationSubText(notification)
            val bigText = getNotificationBigText(notification)
            val summaryText = getNotificationSummaryText(notification)
            val infoText = getNotificationInfoText(notification)
            
            val notificationEntity = NotificationEntity(
                id = UUID.randomUUID().toString(),
                notificationId = notificationId,
                notificationKey = notificationKey,
                sourcePackage = packageName,
                sourceAppName = appName,
                title = title,
                content = content,
                subText = subText,
                bigText = bigText,
                summaryText = summaryText,
                infoText = infoText,
                timestamp = sbn.postTime,
                whenTime = notification.`when`,
                category = notification.category,
                groupKey = sbn.groupKey,
                sortKey = notification.sortKey,
                channelId = notification.channelId,
                priority = notification.priority,
                visibility = notification.visibility,
                isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0,
                isClearable = sbn.isClearable,
                isDismissible = !sbn.isOngoing,
                isGroupSummary = sbn.isGroup,
                isLocalOnly = notification.flags and Notification.FLAG_LOCAL_ONLY != 0,
                isAutoCancelable = notification.flags and Notification.FLAG_AUTO_CANCEL != 0,
                largeIconPresent = notification.largeIcon != null,
                smallIconResource = notification.smallIcon?.toString(),
                color = if (notification.color != 0) notification.color else null,
                number = if (notification.number != 0) notification.number else null,
                progressMax = notification.extras?.getInt(Notification.EXTRA_PROGRESS_MAX)?.takeIf { it > 0 },
                progressCurrent = notification.extras?.getInt(Notification.EXTRA_PROGRESS)?.takeIf { it >= 0 },
                progressIndeterminate = notification.extras?.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE),
                actionCount = notification.actions?.size ?: 0,
                actionTitles = notification.actions?.map { it.title.toString() }?.let { actions ->
                    if (actions.isNotEmpty()) gson.toJson(actions) else null
                },
                extrasBundle = try {
                    notification.extras?.let { extras ->
                        val extrasMap = mutableMapOf<String, String>()
                        extras.keySet().forEach { key ->
                            extras.get(key)?.let { value ->
                                extrasMap[key] = value.toString()
                            }
                        }
                        if (extrasMap.isNotEmpty()) gson.toJson(extrasMap) else null
                    }
                } catch (e: Exception) { null },
                remoteInputAvailable = notification.actions?.any { action ->
                    action.remoteInputs?.isNotEmpty() == true
                } ?: false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val insertResult = notificationRepository.insertNotification(notificationEntity)
            if (insertResult > 0) {
                Log.d(TAG, "Stored notification in database: $packageName - $title")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification entity", e)
        }
    }
    
    private fun shouldSkipNotificationForDatabase(packageName: String, notification: Notification): Boolean {
        // Skip our own notifications
        if (packageName == this.packageName) return true
        
        // Get notification content
        val title = getNotificationTitle(notification)
        val content = getNotificationContent(notification)
        
        // Skip completely empty notifications
        if (title.isEmpty() && content.isEmpty()) return true
        
        // Skip very short, meaningless notifications
        if (title.length < 2 && content.length < 2) return true
        
        // Skip notifications that are just spaces or dots
        if (title.trim().isEmpty() && content.trim().isEmpty()) return true
        if (title.trim().matches(Regex("^[.\\s]*$")) && content.trim().matches(Regex("^[.\\s]*$"))) return true
        
        return false
    }
    
    private fun getNotificationSubText(notification: Notification): String? {
        return notification.extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf { it.isNotEmpty() }
    }
    
    private fun getNotificationBigText(notification: Notification): String? {
        return notification.extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIf { it.isNotEmpty() }
    }
    
    private fun getNotificationSummaryText(notification: Notification): String? {
        return notification.extras?.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.takeIf { it.isNotEmpty() }
    }
    
    private fun getNotificationInfoText(notification: Notification): String? {
        return notification.extras?.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()?.takeIf { it.isNotEmpty() }
    }
    
    private suspend fun updateNotificationNotesStatus(notificationKey: String, notesCreated: Boolean) {
        try {
            val notificationEntity = notificationRepository.getNotificationByKey(notificationKey)
            if (notificationEntity != null && notesCreated) {
                notificationRepository.markNotesCreated(
                    notificationEntity.id,
                    true,
                    System.currentTimeMillis(),
                    null // Will be updated when actual notes are created
                )
                
                Log.d(TAG, "Updated notes status for notification: $notificationKey")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification notes status", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}