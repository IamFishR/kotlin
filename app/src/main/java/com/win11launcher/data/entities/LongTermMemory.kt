package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "long_term_memory",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["memory_type"]),
        Index(value = ["importance_score"]),
        Index(value = ["last_accessed"]),
        Index(value = ["created_at"])
    ]
)
data class LongTermMemory(
    @PrimaryKey 
    val memoryId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "memory_text")
    val memoryText: String,
    
    @ColumnInfo(name = "embedding")
    val embedding: String? = null,       // Vector embedding for semantic search
    
    @ColumnInfo(name = "memory_type")
    val memoryType: String,              // 'fact', 'preference', 'goal', 'synthesized', 'command_pattern'
    
    @ColumnInfo(name = "importance_score")
    val importanceScore: Float,          // 0.0 to 1.0
    
    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "access_count")
    val accessCount: Int = 0,
    
    @ColumnInfo(name = "source_conversations")
    val sourceConversations: String? = null,  // JSON array of conversation IDs
    
    @ColumnInfo(name = "keywords")
    val keywords: String? = null,        // Comma-separated keywords for search
    
    @ColumnInfo(name = "category")
    val category: String? = null,        // SYSTEM, USER, COMMAND, PREFERENCE
    
    @ColumnInfo(name = "decay_factor")
    val decayFactor: Float = 0.99f       // Memory strength decay over time
)