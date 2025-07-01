package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_patterns",
    indices = [
        Index(value = ["transaction_type"]),
        Index(value = ["category"]),
        Index(value = ["bank_name"]),
        Index(value = ["is_recurring"]),
        Index(value = ["last_seen"]),
        Index(value = ["confidence"])
    ]
)
data class FinancialPattern(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "transaction_type")
    val transactionType: String, // DEBIT, CREDIT, EMI, INVESTMENT, TRANSFER
    
    @ColumnInfo(name = "amount")
    val amount: Double?,
    
    @ColumnInfo(name = "merchant")
    val merchant: String?,
    
    @ColumnInfo(name = "category")
    val category: String, // GROCERIES, FUEL, INVESTMENT, BILLS, ENTERTAINMENT, UTILITIES
    
    @ColumnInfo(name = "bank_name")
    val bankName: String?,
    
    @ColumnInfo(name = "frequency")
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY, IRREGULAR
    
    @ColumnInfo(name = "time_pattern")
    val timePattern: String, // MORNING, AFTERNOON, EVENING, NIGHT
    
    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean,
    
    @ColumnInfo(name = "last_seen")
    val lastSeen: Long,
    
    @ColumnInfo(name = "confidence")
    val confidence: Float,
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String?, // com.phonepe.app, com.google.android.apps.nbu.paisa.user
    
    @ColumnInfo(name = "pattern_keywords")
    val patternKeywords: String, // JSON array of keywords that triggered this pattern
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "occurrence_count")
    val occurrenceCount: Int = 1
)

// Transaction type constants
object TransactionType {
    const val DEBIT = "DEBIT"
    const val CREDIT = "CREDIT"
    const val EMI = "EMI"
    const val INVESTMENT = "INVESTMENT"
    const val TRANSFER = "TRANSFER"
    const val BILL_PAYMENT = "BILL_PAYMENT"
    const val REFUND = "REFUND"
}

// Category constants
object FinancialCategory {
    const val GROCERIES = "GROCERIES"
    const val FUEL = "FUEL"
    const val INVESTMENT = "INVESTMENT"
    const val BILLS = "BILLS"
    const val ENTERTAINMENT = "ENTERTAINMENT"
    const val UTILITIES = "UTILITIES"
    const val SHOPPING = "SHOPPING"
    const val FOOD_DELIVERY = "FOOD_DELIVERY"
    const val TRANSPORTATION = "TRANSPORTATION"
    const val HEALTHCARE = "HEALTHCARE"
    const val EDUCATION = "EDUCATION"
    const val INSURANCE = "INSURANCE"
    const val UNKNOWN = "UNKNOWN"
}

// Frequency constants
object Frequency {
    const val DAILY = "DAILY"
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY"
    const val YEARLY = "YEARLY"
    const val IRREGULAR = "IRREGULAR"
}

// Time pattern constants
object TimePattern {
    const val MORNING = "MORNING" // 6 AM - 12 PM
    const val AFTERNOON = "AFTERNOON" // 12 PM - 6 PM
    const val EVENING = "EVENING" // 6 PM - 10 PM
    const val NIGHT = "NIGHT" // 10 PM - 6 AM
}