package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "reflections",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["created_at"]),
        Index(value = ["reflection_type"])
    ]
)
data class Reflection(
    @PrimaryKey 
    val reflectionId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "reflection_text")
    val reflectionText: String,
    
    @ColumnInfo(name = "reflection_type")
    val reflectionType: String,          // 'PATTERN', 'INSIGHT', 'RECOMMENDATION', 'LEARNING'
    
    @ColumnInfo(name = "triggering_message_ids")
    val triggeringMessageIds: String,    // JSON array of message IDs that triggered this reflection
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float = 0.7f,   // AI confidence in this reflection
    
    @ColumnInfo(name = "actionable")
    val actionable: Boolean = false,     // Whether this reflection suggests specific actions
    
    @ColumnInfo(name = "implemented")
    val implemented: Boolean = false,    // Whether suggested actions were implemented
    
    @ColumnInfo(name = "context_data")
    val contextData: String? = null,     // Additional context as JSON
    
    @ColumnInfo(name = "tags")
    val tags: String? = null             // Comma-separated tags for categorization
)