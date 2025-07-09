package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "db_queries",
    foreignKeys = [
        ForeignKey(
            entity = CommandHistory::class,
            parentColumns = ["id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["query_type"]),
        Index(value = ["timestamp"]),
        Index(value = ["command_id"])
    ]
)
data class DatabaseQuery(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "query")
    val query: String,
    
    @ColumnInfo(name = "table_name")
    val tableName: String?,
    
    @ColumnInfo(name = "query_type")
    val queryType: String,                  // SELECT, INSERT, UPDATE, DELETE, SCHEMA
    
    @ColumnInfo(name = "result_count")
    val resultCount: Int,
    
    @ColumnInfo(name = "execution_time_ms")
    val executionTimeMs: Long,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String?,
    
    @ColumnInfo(name = "command_id")
    val commandId: String?                  // Reference to command_history
)