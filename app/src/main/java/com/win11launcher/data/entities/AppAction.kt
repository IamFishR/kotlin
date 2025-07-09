package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "app_actions",
    foreignKeys = [
        ForeignKey(
            entity = CommandHistory::class,
            parentColumns = ["id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["timestamp"]),
        Index(value = ["command_id"])
    ]
)
data class AppAction(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "action")
    val action: String,                     // LAUNCH, KILL, CLEAR_CACHE, etc.
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "command_id")
    val commandId: String?,                 // Reference to command_history
    
    @ColumnInfo(name = "additional_data")
    val additionalData: String?             // JSON for extra info
)