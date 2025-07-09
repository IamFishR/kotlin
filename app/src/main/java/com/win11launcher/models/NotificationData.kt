package com.win11launcher.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime

data class NotificationData(
    val id: String,
    val title: String,
    val message: String,
    val appName: String,
    val icon: ImageVector,
    val timestamp: LocalDateTime,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val isRead: Boolean = false
)

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

object NotificationRepository {
    fun getSampleNotifications(): List<NotificationData> {
        return listOf(
            NotificationData(
                id = "1",
                title = "New Message",
                message = "You have a new message from John",
                appName = "Messages",
                icon = Icons.AutoMirrored.Filled.Message,
                timestamp = LocalDateTime.now().minusMinutes(5)
            ),
            NotificationData(
                id = "2", 
                title = "System Update",
                message = "System update available. Restart required.",
                appName = "System",
                icon = Icons.Default.SystemUpdate,
                timestamp = LocalDateTime.now().minusMinutes(15),
                priority = NotificationPriority.HIGH
            ),
            NotificationData(
                id = "3",
                title = "Battery Low",
                message = "Battery is running low. Please charge your device.",
                appName = "System",
                icon = Icons.Default.BatteryAlert,
                timestamp = LocalDateTime.now().minusMinutes(30),
                priority = NotificationPriority.URGENT
            ),
            NotificationData(
                id = "4",
                title = "Email Received",
                message = "You have 3 new emails",
                appName = "Mail",
                icon = Icons.Default.Email,
                timestamp = LocalDateTime.now().minusHours(1)
            ),
            NotificationData(
                id = "5",
                title = "Calendar Reminder",
                message = "Meeting with team in 30 minutes",
                appName = "Calendar",
                icon = Icons.Default.Event,
                timestamp = LocalDateTime.now().minusHours(2),
                priority = NotificationPriority.HIGH
            ),
            NotificationData(
                id = "6",
                title = "Download Complete",
                message = "File download completed successfully",
                appName = "Downloads",
                icon = Icons.Default.Download,
                timestamp = LocalDateTime.now().minusHours(3)
            )
        )
    }
}