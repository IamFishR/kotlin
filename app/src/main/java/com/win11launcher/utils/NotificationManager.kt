package com.win11launcher.utils

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
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
            // For Android 10+ (API 29+), we need to handle background launch restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use the notification's content intent if available
                notification.contentIntent?.let { pendingIntent ->
                    // Create a proper bundle for background launch
                    val options = android.os.Bundle().apply {
                        putBoolean("android.pendingIntent.backgroundActivityAllowed", true)
                    }
                    
                    // Send the pending intent with proper callback
                    val callback = object : PendingIntent.OnFinished {
                        override fun onSendFinished(
                            pendingIntent: PendingIntent?,
                            intent: Intent?,
                            resultCode: Int,
                            resultData: String?,
                            resultExtras: android.os.Bundle?
                        ) {
                            // If pending intent fails, try launching app directly
                            if (resultCode != 0) {
                                launchAppDirectly(notification.packageName)
                            }
                        }
                    }
                    
                    pendingIntent.send(context, 0, null, callback, null, null, options)
                    return
                }
            } else {
                // For older Android versions, use the content intent directly
                notification.contentIntent?.let { pendingIntent ->
                    pendingIntent.send()
                    return
                }
            }
            
            // Fallback: Launch the app directly
            launchAppDirectly(notification.packageName)
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to handle notification click", e)
            // Last resort: try launching the app
            launchAppDirectly(notification.packageName)
        }
    }
    
    private fun launchAppDirectly(packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                          Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.addCategory(Intent.CATEGORY_LAUNCHER)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to launch app directly: $packageName", e)
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