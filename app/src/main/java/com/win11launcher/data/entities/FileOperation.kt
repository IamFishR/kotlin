package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "file_operations",
    foreignKeys = [
        ForeignKey(
            entity = CommandHistory::class,
            parentColumns = ["id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["operation"]),
        Index(value = ["timestamp"]),
        Index(value = ["command_id"])
    ]
)
data class FileOperation(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "operation")
    val operation: String,                  // LS, CD, CP, MV, RM, MKDIR, etc.
    
    @ColumnInfo(name = "source_path")
    val sourcePath: String,
    
    @ColumnInfo(name = "destination_path")
    val destinationPath: String?,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "files_affected")
    val filesAffected: Int,
    
    @ColumnInfo(name = "bytes_transferred")
    val bytesTransferred: Long?,
    
    @ColumnInfo(name = "command_id")
    val commandId: String?                  // Reference to command_history
)