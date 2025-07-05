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
import com.win11launcher.analysis.NotificationAnalyzer
import com.win11launcher.analysis.GeneralSmartSuggestionEngine
import java.text.SimpleDateFormat
import java.util.*

class Win11NotificationListenerService : NotificationListenerService() {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var ruleEngine: RuleEngine
    private lateinit var financialIntelligenceService: FinancialIntelligenceService
    
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
        
        fun getFinancialIntelligenceService(): FinancialIntelligenceService? {
            return serviceInstance?.financialIntelligenceService
        }
    }
    
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
        ruleEngine = RuleEngine(this)
        
        // Initialize smart suggestion service
        val notificationAnalyzer = NotificationAnalyzer(this)
        val database = com.win11launcher.data.database.NotesDatabase.getDatabase(this)
        val suggestionEngine = GeneralSmartSuggestionEngine()
        financialIntelligenceService = FinancialIntelligenceService(
            this,
            notificationAnalyzer,
            suggestionEngine
        )
        
        Log.d(TAG, "NotificationListenerService created with smart suggestion service")
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
                            // First process through standard rule engine
                            ruleEngine.processNotification(appNotification)
                            
                            // Then process through smart suggestion service
                            financialIntelligenceService.processNotificationForSmartSuggestions(appNotification)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing notification through engines", e)
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
    
    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}