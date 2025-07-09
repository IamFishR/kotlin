package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "command_usage",
    indices = [
        Index(value = ["category"]),
        Index(value = ["last_used"]),
        Index(value = ["usage_count"])
    ]
)
data class CommandUsage(
    @PrimaryKey 
    val command: String,                    // Command pattern (e.g., "net scan")
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long?,
    
    @ColumnInfo(name = "success_rate")
    val successRate: Float = 0.0f,
    
    @ColumnInfo(name = "average_execution_time")
    val averageExecutionTime: Float = 0.0f,
    
    @ColumnInfo(name = "total_execution_time")
    val totalExecutionTime: Long = 0L,
    
    @ColumnInfo(name = "category")
    val category: String                    // NET, APP, FILE, AI, DEV, etc.
)