package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "ai_conversations",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["conversation_type"]),
        Index(value = ["timestamp"])
    ]
)
data class AIConversation(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    
    @ColumnInfo(name = "prompt")
    val prompt: String,
    
    @ColumnInfo(name = "response")
    val response: String,
    
    @ColumnInfo(name = "model_used")
    val modelUsed: String,
    
    @ColumnInfo(name = "tokens_used")
    val tokensUsed: Int?,
    
    @ColumnInfo(name = "processing_time_ms")
    val processingTimeMs: Long,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "conversation_type")
    val conversationType: String,           // CHAT, COMMAND_GENERATION, ANALYSIS
    
    @ColumnInfo(name = "context_data")
    val contextData: String?,               // JSON context for AI
    
    @ColumnInfo(name = "generated_commands")
    val generatedCommands: String?,         // JSON array of commands AI generated
    
    @ColumnInfo(name = "user_rating")
    val userRating: Int?                    // User feedback 1-5
)