package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extracted_notification_data")
data class ExtractedNotificationData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "source_package") val sourcePackage: String,
    @ColumnInfo(name = "notification_title") val notificationTitle: String?,
    @ColumnInfo(name = "notification_content") val notificationContent: String?,
    @ColumnInfo(name = "extracted_keywords") val extractedKeywords: List<String>,
    @ColumnInfo(name = "suggested_category") val suggestedCategory: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)