package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "all_notifications",
    indices = [
        Index(value = ["notification_id"], unique = true), // Prevent duplicates
        Index(value = ["source_package"]),
        Index(value = ["timestamp"]),
        Index(value = ["is_ai_processed"]),
        Index(value = ["notes_created"]),
        Index(value = ["user_showed_interest"]),
        Index(value = ["notification_key"], unique = true), // Secondary duplicate prevention
        Index(value = ["source_package", "title", "content"]) // Content-based duplicate check
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    
    // Core notification data
    @ColumnInfo(name = "notification_id")
    val notificationId: String, // Original Android notification ID
    
    @ColumnInfo(name = "notification_key")
    val notificationKey: String, // Unique key for notification
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String,
    
    @ColumnInfo(name = "source_app_name")
    val sourceAppName: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "sub_text")
    val subText: String? = null,
    
    @ColumnInfo(name = "big_text")
    val bigText: String? = null,
    
    @ColumnInfo(name = "summary_text")
    val summaryText: String? = null,
    
    @ColumnInfo(name = "info_text")
    val infoText: String? = null,
    
    // Notification metadata
    @ColumnInfo(name = "timestamp")
    val timestamp: Long, // When notification was posted
    
    @ColumnInfo(name = "when_time")
    val whenTime: Long? = null, // Notification.when field
    
    @ColumnInfo(name = "category")
    val category: String? = null,
    
    @ColumnInfo(name = "group_key")
    val groupKey: String? = null,
    
    @ColumnInfo(name = "sort_key")
    val sortKey: String? = null,
    
    @ColumnInfo(name = "channel_id")
    val channelId: String? = null,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "visibility")
    val visibility: Int = 0,
    
    // Notification flags and state
    @ColumnInfo(name = "is_ongoing")
    val isOngoing: Boolean = false,
    
    @ColumnInfo(name = "is_clearable")
    val isClearable: Boolean = true,
    
    @ColumnInfo(name = "is_dismissible")
    val isDismissible: Boolean = true,
    
    @ColumnInfo(name = "is_group_summary")
    val isGroupSummary: Boolean = false,
    
    @ColumnInfo(name = "is_local_only")
    val isLocalOnly: Boolean = false,
    
    @ColumnInfo(name = "is_auto_cancelable")
    val isAutoCancelable: Boolean = true,
    
    // Rich notification data
    @ColumnInfo(name = "large_icon_present")
    val largeIconPresent: Boolean = false,
    
    @ColumnInfo(name = "small_icon_resource")
    val smallIconResource: String? = null,
    
    @ColumnInfo(name = "color")
    val color: Int? = null,
    
    @ColumnInfo(name = "number")
    val number: Int? = null,
    
    @ColumnInfo(name = "progress_max")
    val progressMax: Int? = null,
    
    @ColumnInfo(name = "progress_current")
    val progressCurrent: Int? = null,
    
    @ColumnInfo(name = "progress_indeterminate")
    val progressIndeterminate: Boolean? = null,
    
    // Actions and extras
    @ColumnInfo(name = "action_count")
    val actionCount: Int = 0,
    
    @ColumnInfo(name = "action_titles")
    val actionTitles: String? = null, // JSON array of action titles
    
    @ColumnInfo(name = "extras_bundle")
    val extrasBundle: String? = null, // JSON of extras Bundle
    
    @ColumnInfo(name = "remote_input_available")
    val remoteInputAvailable: Boolean = false,
    
    // Tracking and Analytics columns
    @ColumnInfo(name = "is_ai_processed")
    val isAiProcessed: Boolean = false,
    
    @ColumnInfo(name = "ai_processed_at")
    val aiProcessedAt: Long? = null,
    
    @ColumnInfo(name = "ai_processing_result")
    val aiProcessingResult: String? = null, // JSON of AI analysis result
    
    @ColumnInfo(name = "notes_created")
    val notesCreated: Boolean = false,
    
    @ColumnInfo(name = "notes_created_at")
    val notesCreatedAt: Long? = null,
    
    @ColumnInfo(name = "note_ids")
    val noteIds: String? = null, // JSON array of created note IDs
    
    @ColumnInfo(name = "user_showed_interest")
    val userShowedInterest: Boolean = false,
    
    @ColumnInfo(name = "user_interaction_type")
    val userInteractionType: String? = null, // "clicked", "saved", "starred", "ignored", "dismissed"
    
    @ColumnInfo(name = "user_interaction_at")
    val userInteractionAt: Long? = null,
    
    @ColumnInfo(name = "user_rating")
    val userRating: Int? = null, // 1-5 scale for notification usefulness
    
    @ColumnInfo(name = "user_notes")
    val userNotes: String? = null, // User's personal notes about this notification
    
    // Rule processing
    @ColumnInfo(name = "matched_rules")
    val matchedRules: String? = null, // JSON array of rule IDs that matched
    
    @ColumnInfo(name = "rule_processing_result")
    val ruleProcessingResult: String? = null, // JSON of rule processing details
    
    @ColumnInfo(name = "was_auto_processed")
    val wasAutoProcessed: Boolean = false,
    
    // Classification and categorization
    @ColumnInfo(name = "auto_category")
    val autoCategory: String? = null, // AI-determined category
    
    @ColumnInfo(name = "auto_tags")
    val autoTags: String? = null, // JSON array of auto-generated tags
    
    @ColumnInfo(name = "importance_score")
    val importanceScore: Float? = null, // AI-calculated importance (0.0-1.0)
    
    @ColumnInfo(name = "sentiment_score")
    val sentimentScore: Float? = null, // AI-calculated sentiment (-1.0 to 1.0)
    
    @ColumnInfo(name = "urgency_score")
    val urgencyScore: Float? = null, // AI-calculated urgency (0.0-1.0)
    
    // System fields
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null, // Soft delete
    
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    
    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null
)