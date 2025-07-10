package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "short_term_memory",
    foreignKeys = [
        ForeignKey(
            entity = AIConversation::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["message_order"])
    ]
)
data class ShortTermMemory(
    @PrimaryKey 
    val messageId: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "message_order")
    val messageOrder: Int,
    
    @ColumnInfo(name = "sender")
    val sender: String,                  // 'user', 'ai', 'system'
    
    @ColumnInfo(name = "content_text")
    val contentText: String,
    
    @ColumnInfo(name = "token_count")
    val tokenCount: Int,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "message_type")
    val messageType: String = "TEXT",    // TEXT, COMMAND, SYSTEM_INFO
    
    @ColumnInfo(name = "importance_score")
    val importanceScore: Float = 0.5f,   // 0.0 to 1.0
    
    @ColumnInfo(name = "context_data")
    val contextData: String? = null      // JSON additional context
)