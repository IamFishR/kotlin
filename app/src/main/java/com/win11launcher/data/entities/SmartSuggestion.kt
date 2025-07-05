package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "smart_suggestions",
    indices = [
        Index(value = ["category"]),
        Index(value = ["sub_category"]),
        Index(value = ["confidence_score"]),
        Index(value = ["priority"]),
        Index(value = ["created_at"]),
        Index(value = ["is_dismissed"]),
        Index(value = ["is_applied"])
    ]
)
data class SmartSuggestion(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "category")
    val category: String, // GENERAL, FINANCE, PRODUCTIVITY, etc.
    
    @ColumnInfo(name = "sub_category")
    val subCategory: String, // Keyword, App, Recurring, etc.
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String? = null, // Package name of the app that generated the notification
    
    @ColumnInfo(name = "notification_title")
    val notificationTitle: String? = null, // Original notification title
    
    @ColumnInfo(name = "notification_content")
    val notificationContent: String? = null, // Original notification content
    
    @ColumnInfo(name = "extracted_keywords")
    val extractedKeywords: String? = null, // JSON array of extracted keywords
    
    @ColumnInfo(name = "suggested_tags")
    val suggestedTags: String? = null, // JSON array of suggested tags for the note
    
    @ColumnInfo(name = "automated_rule_config")
    val automatedRuleConfig: String? = null, // JSON config for one-click rule creation (optional)
    
    @ColumnInfo(name = "expected_benefit")
    val expectedBenefit: String? = null, // "Save 5 minutes daily", "Track Rs 50,000 monthly spending"
    
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,
    
    @ColumnInfo(name = "priority")
    val priority: Int, // 1 = High, 2 = Medium, 3 = Low
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_dismissed")
    val isDismissed: Boolean = false,
    
    @ColumnInfo(name = "is_applied")
    val isApplied: Boolean = false,
    
    @ColumnInfo(name = "dismissal_reason")
    val dismissalReason: String? = null,
    
    @ColumnInfo(name = "application_date")
    val applicationDate: Long? = null,
    
    @ColumnInfo(name = "source_patterns")
    val sourcePatterns: String? = null, // JSON array of pattern IDs that triggered this suggestion
    
    @ColumnInfo(name = "suggested_folder_name")
    val suggestedFolderName: String? = null,
    
    @ColumnInfo(name = "suggested_folder_color")
    val suggestedFolderColor: String? = null,
    
    @ColumnInfo(name = "suggested_folder_icon")
    val suggestedFolderIcon: String? = null,
    
    @ColumnInfo(name = "success_metrics")
    val successMetrics: String? = null // JSON object with expected metrics
)

// Suggestion category constants
object SuggestionCategory {
    const val GENERAL = "GENERAL"
    const val FINANCE = "FINANCE"
    const val INVESTMENT = "INVESTMENT"
    const val RESEARCH = "RESEARCH"
    const val MARKET_NEWS = "MARKET_NEWS"
    const val PRODUCTIVITY = "PRODUCTIVITY"
    const val ORGANIZATION = "ORGANIZATION"
    const val COMMUNICATION = "COMMUNICATION"
    const val REMINDER = "REMINDER"
}

// Suggestion subcategory constants
object SuggestionSubCategory {
    // General subcategories
    const val KEYWORD_DETECTION = "KEYWORD_DETECTION"
    const val APP_USAGE = "APP_USAGE"
    const val RECURRING_EVENT = "RECURRING_EVENT"
    const val IMPORTANT_INFO = "IMPORTANT_INFO"
    
    // Finance subcategories
    const val TRANSACTIONS = "TRANSACTIONS"
    const val EMI_TRACKING = "EMI_TRACKING"
    const val EXPENSE_CATEGORIZATION = "EXPENSE_CATEGORIZATION"
    const val BANKING_ALERTS = "BANKING_ALERTS"
    const val BILL_REMINDERS = "BILL_REMINDERS"
    
    // Investment subcategories
    const val STOCKS = "STOCKS"
    const val MUTUAL_FUNDS = "MUTUAL_FUNDS"
    const val PORTFOLIO_TRACKING = "PORTFOLIO_TRACKING"
    const val SIP_MONITORING = "SIP_MONITORING"
    const val MARKET_ALERTS = "MARKET_ALERTS"
    
    // Research subcategories
    const val SOLAR_TECH = "SOLAR_TECH"
    const val RENEWABLE_ENERGY = "RENEWABLE_ENERGY"
    const val TECH_INNOVATION = "TECH_INNOVATION"
    const val INDUSTRY_REPORTS = "INDUSTRY_REPORTS"
    const val ACADEMIC_PAPERS = "ACADEMIC_PAPERS"
    
    // Market news subcategories
    const val FINANCIAL_NEWS = "FINANCIAL_NEWS"
    const val SECTOR_NEWS = "SECTOR_NEWS"
    const val POLICY_UPDATES = "POLICY_UPDATES"
    const val ECONOMIC_INDICATORS = "ECONOMIC_INDICATORS"
    
    // Productivity subcategories
    const val TASK_REMINDER = "TASK_REMINDER"
    const val MEETING_REMINDER = "MEETING_REMINDER"
    
    // Organization subcategories
    const val DOCUMENT_MANAGEMENT = "DOCUMENT_MANAGEMENT"
    const val EMAIL_ORGANIZATION = "EMAIL_ORGANIZATION"
}

// Priority constants
object SuggestionPriority {
    const val HIGH = 1
    const val MEDIUM = 2
    const val LOW = 3
}

// Savings type constants (can be repurposed for general benefits)
object SavingsType {
    const val TIME_MINUTES = "TIME_MINUTES"
    const val MONEY_RUPEES = "MONEY_RUPEES"
    const val EFFICIENCY_PERCENT = "EFFICIENCY_PERCENT"
    const val INFORMATION_GAIN = "INFORMATION_GAIN"
    const val ORGANIZATION_IMPROVEMENT = "ORGANIZATION_IMPROVEMENT"
}