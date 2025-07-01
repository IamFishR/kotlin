package com.win11launcher.models

import android.app.PendingIntent
import android.graphics.drawable.Icon

data class AppNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val time: String,
    val timestamp: Long,
    val smallIcon: Icon?,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val contentIntent: PendingIntent?
)