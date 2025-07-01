package com.win11launcher.utils

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
    
    fun checkAndRequestBackgroundPermissions() {
        // Check and request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    android.util.Log.d("NotificationManager", "Requesting battery optimization exemption")
                } catch (e: Exception) {
                    android.util.Log.w("NotificationManager", "Failed to request battery optimization exemption", e)
                }
            }
        }
        
        // Check and request system alert window permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                android.util.Log.d("NotificationManager", "Requesting system alert window permission")
            } catch (e: Exception) {
                android.util.Log.w("NotificationManager", "Failed to request system alert window permission", e)
            }
        }
    }
    
    fun isBackgroundLaunchAllowed(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            val overlayPermission = Settings.canDrawOverlays(context)
            
            android.util.Log.d("NotificationManager", "Background launch permissions - Battery: $batteryOptimizationIgnored, Overlay: $overlayPermission")
            return batteryOptimizationIgnored && overlayPermission
        }
        return true
    }
    
    fun handleNotificationClick(notification: AppNotification) {
        try {
            android.util.Log.d("NotificationManager", "Handling notification click for: ${notification.appName}")
            
            // Check background permissions first
            if (!isBackgroundLaunchAllowed()) {
                android.util.Log.w("NotificationManager", "Background launch not allowed, requesting permissions")
                checkAndRequestBackgroundPermissions()
            }
            
            // For Android 10+ (API 29+), use the BubbleMetadata approach
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notification.contentIntent?.let { originalPendingIntent ->
                    // Approach 1: Use BubbleMetadata to signal legitimate user interaction
                    try {
                        android.util.Log.d("NotificationManager", "Attempting BubbleMetadata approach")
                        
                        // Get the small icon for the bubble metadata
                        val smallIcon = notification.smallIcon ?: createDefaultIcon()
                        
                        // Create BubbleMetadata to signal this is a legitimate user-initiated action
                        val bubbleMetadata = Notification.BubbleMetadata.Builder(originalPendingIntent, smallIcon)
                            .setDesiredHeight(600)
                            .setDesiredHeightResId(0)
                            .setSuppressNotification(true)
                            .setAutoExpandBubble(false)
                            .build()
                        
                        // Create ActivityOptions with bubble context
                        val activityOptions = ActivityOptions.makeBasic()
                        
                        val bundle = activityOptions.toBundle()
                        
                        // Add additional flags to signal legitimate launch - use correct Integer types
                        bundle.putInt("android.pendingIntent.backgroundActivityAllowed", 1) // 1 = MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        bundle.putInt("android.activity.allowDuringSetup", 1)
                        bundle.putBoolean("android.intent.extra.USER_INITIATED", true)
                        
                        // Send the PendingIntent with proper context
                        originalPendingIntent.send(context, 0, null, null, null, null, bundle)
                        android.util.Log.d("NotificationManager", "Successfully sent PendingIntent with BubbleMetadata approach")
                        return
                        
                    } catch (e: Exception) {
                        android.util.Log.w("NotificationManager", "BubbleMetadata approach failed", e)
                    }
                    
                    // Approach 2: Enhanced ActivityOptions approach
                    try {
                        android.util.Log.d("NotificationManager", "Attempting enhanced ActivityOptions approach")
                        
                        val activityOptions = ActivityOptions.makeBasic()
                        val bundle = activityOptions.toBundle()
                        
                        // Signal that this is a user-initiated action from foreground app - use correct Integer types
                        bundle.putInt("android.pendingIntent.backgroundActivityAllowed", 1) // 1 = MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        bundle.putInt("android.activity.allowDuringSetup", 1)
                        bundle.putBoolean("android.intent.extra.USER_INITIATED", true)
                        bundle.putString("android.intent.extra.CALLING_PACKAGE", context.packageName)
                        bundle.putInt("android.activity.launchDisplayId", 0)
                        
                        val callback = object : PendingIntent.OnFinished {
                            override fun onSendFinished(
                                pendingIntent: PendingIntent?,
                                intent: Intent?,
                                resultCode: Int,
                                resultData: String?,
                                resultExtras: Bundle?
                            ) {
                                android.util.Log.d("NotificationManager", "PendingIntent finished with code: $resultCode")
                                if (resultCode != 0) {
                                    android.util.Log.w("NotificationManager", "PendingIntent failed, trying direct launch")
                                    launchAppDirectly(notification.packageName)
                                }
                            }
                        }
                        
                        originalPendingIntent.send(context, 0, null, callback, null, null, bundle)
                        android.util.Log.d("NotificationManager", "Successfully sent PendingIntent with enhanced options")
                        return
                        
                    } catch (e: Exception) {
                        android.util.Log.w("NotificationManager", "Enhanced ActivityOptions approach failed", e)
                    }
                    
                    // Approach 3: Simple send with basic options
                    try {
                        android.util.Log.d("NotificationManager", "Attempting simple send approach")
                        originalPendingIntent.send(context, 0, null)
                        android.util.Log.d("NotificationManager", "Successfully sent PendingIntent with simple approach")
                        return
                    } catch (e: Exception) {
                        android.util.Log.w("NotificationManager", "Simple send approach failed", e)
                    }
                }
            } else {
                // For older Android versions, use the content intent directly
                notification.contentIntent?.let { pendingIntent ->
                    try {
                        pendingIntent.send()
                        android.util.Log.d("NotificationManager", "Successfully sent PendingIntent for older Android")
                        return
                    } catch (e: Exception) {
                        android.util.Log.w("NotificationManager", "PendingIntent send failed on older Android", e)
                    }
                }
            }
            
            // Final fallback: Launch the app directly
            android.util.Log.d("NotificationManager", "All PendingIntent approaches failed, launching app directly")
            launchAppDirectly(notification.packageName)
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to handle notification click", e)
            // Last resort: try launching the app
            launchAppDirectly(notification.packageName)
        }
    }
    
    private fun createDefaultIcon(): Icon {
        return try {
            // Create a default icon from the app's icon
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            Icon.createWithResource(context, appInfo.icon)
        } catch (e: Exception) {
            // Fallback to system notification icon
            Icon.createWithResource(context, android.R.drawable.ic_dialog_info)
        }
    }
    
    private fun launchAppDirectly(packageName: String) {
        try {
            // Approach 1: Try standard launch intent
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                              Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                              Intent.FLAG_ACTIVITY_CLEAR_TOP or
                              Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                
                // Try with ActivityOptions for better compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val options = ActivityOptions.makeBasic()
                        context.startActivity(intent, options.toBundle())
                        android.util.Log.d("NotificationManager", "Successfully launched app with ActivityOptions: $packageName")
                        return
                    } catch (e: Exception) {
                        android.util.Log.w("NotificationManager", "ActivityOptions launch failed, trying normal launch", e)
                    }
                }
                
                // Fallback to normal startActivity
                context.startActivity(intent)
                android.util.Log.d("NotificationManager", "Successfully launched app directly: $packageName")
                return
            }
            
            // Approach 2: If no launch intent, try finding main activity manually
            android.util.Log.w("NotificationManager", "No launch intent found for $packageName, trying to find main activity")
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            
            val resolveInfoList = context.packageManager.queryIntentActivities(mainIntent, 0)
            if (resolveInfoList.isNotEmpty()) {
                val resolveInfo = resolveInfoList[0]
                val componentName = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                val specificIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = componentName
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
                
                context.startActivity(specificIntent)
                android.util.Log.d("NotificationManager", "Successfully launched app via component: $packageName")
                return
            }
            
            android.util.Log.e("NotificationManager", "Could not find any way to launch app: $packageName")
            
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