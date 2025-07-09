package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "command_outputs",
    foreignKeys = [
        ForeignKey(
            entity = CommandHistory::class,
            parentColumns = ["id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["command_id"]),
        Index(value = ["timestamp"])
    ]
)
data class CommandOutput(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "command_id")
    val commandId: String,                  // FK to command_history
    
    @ColumnInfo(name = "full_output")
    val fullOutput: String,                 // Complete command output
    
    @ColumnInfo(name = "output_type")
    val outputType: String,                 // TEXT, JSON, ERROR, etc.
    
    @ColumnInfo(name = "compressed")
    val compressed: Boolean = false,        // For large outputs
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)