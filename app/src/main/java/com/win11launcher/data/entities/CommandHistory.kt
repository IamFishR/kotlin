package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "command_history",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["command_type"]),
        Index(value = ["timestamp"])
    ]
)
data class CommandHistory(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "command")
    val command: String,                    // Full command text
    
    @ColumnInfo(name = "command_type")
    val commandType: String,                // SYSTEM, AI, FILE, NET, APP, DEV, etc.
    
    @ColumnInfo(name = "sub_command")
    val subCommand: String?,                // e.g., "scan", "list", "info"
    
    @ColumnInfo(name = "arguments")
    val arguments: String?,                 // JSON array of arguments
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "execution_time_ms")
    val executionTimeMs: Long,
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "output_preview")
    val outputPreview: String,              // First 200 chars of output
    
    @ColumnInfo(name = "full_output_id")
    val fullOutputId: String?,              // Reference to command_outputs
    
    @ColumnInfo(name = "session_id")
    val sessionId: String                   // Group commands by session
)