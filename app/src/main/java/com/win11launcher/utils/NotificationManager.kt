package com.win11launcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
import com.win11launcher.services.AppNotification
import com.win11launcher.services.Win11NotificationListenerService
import kotlinx.coroutines.flow.StateFlow

class NotificationManager(private val context: Context) {
    
    fun isNotificationAccessEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val packageName = context.packageName
        return enabledListeners?.contains(packageName) == true
    }
    
    fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    fun requestNotificationAccess() {
        if (!isNotificationAccessEnabled()) {
            openNotificationAccessSettings()
        }
    }
    
    fun getNotifications(): StateFlow<List<AppNotification>> {
        return Win11NotificationListenerService.notifications
    }
    
    fun isServiceConnected(): Boolean {
        return Win11NotificationListenerService.isServiceConnected()
    }
    
    fun dismissNotification(notificationId: String) {
        Win11NotificationListenerService.dismissNotification(notificationId)
    }
    
    fun dismissAllNotifications() {
        Win11NotificationListenerService.dismissAllNotifications()
    }
    
    fun getNotificationAccessIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    fun handleNotificationClick(notification: AppNotification) {
        try {
            // Try to use the notification's content intent first
            notification.contentIntent?.let { pendingIntent ->
                pendingIntent.send()
                return
            }
            
            // Fallback: Launch the app directly
            val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
            launchIntent?.let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(it)
            }
        } catch (e: Exception) {
            // If all else fails, try to launch the app
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
                launchIntent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
            } catch (fallbackException: Exception) {
                // Log the error but don't crash
                android.util.Log.e("NotificationManager", "Failed to handle notification click", fallbackException)
            }
        }
    }
    
    companion object {
        const val NOTIFICATION_ACCESS_REQUEST_CODE = 1001
    }
}

// Extension function to convert AppNotification to the UI data class
fun AppNotification.toNotificationItem(): com.win11launcher.ui.components.NotificationItem {
    return com.win11launcher.ui.components.NotificationItem(
        title = this.title,
        content = this.content,
        time = this.time,
        appName = this.appName,
        icon = androidx.compose.material.icons.Icons.Default.Notifications // We'll update this later for app icons
    )
}

// Composable function to observe notifications
@Composable
fun rememberNotifications(notificationManager: NotificationManager): List<AppNotification> {
    val notifications by notificationManager.getNotifications().collectAsState()
    return notifications
}