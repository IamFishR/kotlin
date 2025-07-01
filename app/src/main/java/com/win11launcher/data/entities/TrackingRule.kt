package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "tracking_rules",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["destination_folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["source_packages"]),
        Index(value = ["destination_folder_id"]),
        Index(value = ["is_active"]),
        Index(value = ["created_at"])
    ]
)
data class TrackingRule(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "source_packages")
    val sourcePackages: String, // JSON array of package names
    
    @ColumnInfo(name = "filter_type")
    val filterType: String, // ALL, KEYWORD_INCLUDE, KEYWORD_EXCLUDE, REGEX
    
    @ColumnInfo(name = "filter_criteria")
    val filterCriteria: String = "", // JSON object with filter data
    
    @ColumnInfo(name = "destination_folder_id")
    val destinationFolderId: String,
    
    @ColumnInfo(name = "auto_tags")
    val autoTags: String = "", // JSON array of tags to auto-apply
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0, // Higher number = higher priority
    
    @ColumnInfo(name = "quiet_hours_enabled")
    val quietHoursEnabled: Boolean = false,
    
    @ColumnInfo(name = "quiet_hours_start")
    val quietHoursStart: String = "22:00", // HH:mm format
    
    @ColumnInfo(name = "quiet_hours_end")
    val quietHoursEnd: String = "08:00", // HH:mm format
    
    @ColumnInfo(name = "weekdays_only")
    val weekdaysOnly: Boolean = false,
    
    @ColumnInfo(name = "max_notes_per_day")
    val maxNotesPerDay: Int = -1, // -1 for unlimited
    
    @ColumnInfo(name = "duplicate_detection_enabled")
    val duplicateDetectionEnabled: Boolean = true,
    
    @ColumnInfo(name = "min_content_length")
    val minContentLength: Int = 0,
    
    @ColumnInfo(name = "max_content_length")
    val maxContentLength: Int = -1, // -1 for unlimited
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "last_triggered_at")
    val lastTriggeredAt: Long? = null,
    
    @ColumnInfo(name = "notes_captured_count")
    val notesCapturedCount: Long = 0,
    
    @ColumnInfo(name = "total_matches_count")
    val totalMatchesCount: Long = 0
)