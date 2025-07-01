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
        Index(value = ["is_finance_related"]),
        Index(value = ["created_at"]),
        Index(value = ["is_dismissed"]),
        Index(value = ["is_applied"])
    ]
)
data class SmartSuggestion(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "category")
    val category: String, // FINANCE, INVESTMENT, RESEARCH, MARKET_NEWS
    
    @ColumnInfo(name = "sub_category")
    val subCategory: String, // TRANSACTIONS, STOCKS, SOLAR_TECH, EMI_TRACKING
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "automated_rule_config")
    val automatedRuleConfig: String, // JSON config for one-click rule creation
    
    @ColumnInfo(name = "expected_benefit")
    val expectedBenefit: String, // "Save 5 minutes daily", "Track Rs 50,000 monthly spending"
    
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,
    
    @ColumnInfo(name = "priority")
    val priority: Int, // 1 = High, 2 = Medium, 3 = Low
    
    @ColumnInfo(name = "is_finance_related")
    val isFinanceRelated: Boolean,
    
    @ColumnInfo(name = "estimated_savings")
    val estimatedSavings: Double?, // Time/money savings in minutes or rupees
    
    @ColumnInfo(name = "savings_type")
    val savingsType: String?, // TIME_MINUTES, MONEY_RUPEES
    
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
    const val FINANCE = "FINANCE"
    const val INVESTMENT = "INVESTMENT"
    const val RESEARCH = "RESEARCH"
    const val MARKET_NEWS = "MARKET_NEWS"
    const val PRODUCTIVITY = "PRODUCTIVITY"
    const val ORGANIZATION = "ORGANIZATION"
}

// Suggestion subcategory constants
object SuggestionSubCategory {
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
}

// Priority constants
object SuggestionPriority {
    const val HIGH = 1
    const val MEDIUM = 2
    const val LOW = 3
}

// Savings type constants
object SavingsType {
    const val TIME_MINUTES = "TIME_MINUTES"
    const val MONEY_RUPEES = "MONEY_RUPEES"
    const val EFFICIENCY_PERCENT = "EFFICIENCY_PERCENT"
}