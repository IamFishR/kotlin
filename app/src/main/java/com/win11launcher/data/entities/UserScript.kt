package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "user_scripts",
    indices = [
        Index(value = ["name"]),
        Index(value = ["created_at"]),
        Index(value = ["last_executed"])
    ]
)
data class UserScript(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "commands")
    val commands: String,                   // JSON array of commands
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long,
    
    @ColumnInfo(name = "last_executed")
    val lastExecuted: Long?,
    
    @ColumnInfo(name = "execution_count")
    val executionCount: Int = 0,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "tags")
    val tags: String?,                      // JSON array of tags
    
    @ColumnInfo(name = "schedule_type")
    val scheduleType: String?,              // MANUAL, HOURLY, DAILY, etc.
    
    @ColumnInfo(name = "schedule_data")
    val scheduleData: String?               // JSON schedule configuration
)