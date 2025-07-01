package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "rule_activity",
    foreignKeys = [
        ForeignKey(
            entity = TrackingRule::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rule_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["action_type"])
    ]
)
data class RuleActivity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    
    @ColumnInfo(name = "action_type")
    val actionType: String, // TRIGGERED, SKIPPED, ERROR
    
    @ColumnInfo(name = "notification_title")
    val notificationTitle: String = "",
    
    @ColumnInfo(name = "notification_content")
    val notificationContent: String = "",
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String = "",
    
    @ColumnInfo(name = "note_created_id")
    val noteCreatedId: String? = null,
    
    @ColumnInfo(name = "skip_reason")
    val skipReason: String = "", // QUIET_HOURS, DAILY_LIMIT, DUPLICATE, etc.
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String = "",
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)