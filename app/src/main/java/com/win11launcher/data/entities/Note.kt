package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackingRule::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["rule_id"]),
        Index(value = ["created_at"]),
        Index(value = ["source_package"])
    ]
)
data class Note(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String,
    
    @ColumnInfo(name = "source_app_name")
    val sourceAppName: String,
    
    @ColumnInfo(name = "folder_id")
    val folderId: String,
    
    @ColumnInfo(name = "rule_id")
    val ruleId: String? = null,
    
    @ColumnInfo(name = "tags")
    val tags: String = "", // JSON array of strings
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean = false,
    
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    
    @ColumnInfo(name = "original_notification_id")
    val originalNotificationId: String? = null,
    
    @ColumnInfo(name = "notification_timestamp")
    val notificationTimestamp: Long? = null
)