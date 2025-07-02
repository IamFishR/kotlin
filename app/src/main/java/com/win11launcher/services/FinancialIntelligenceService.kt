package com.win11launcher.services

import android.content.Context
import com.win11launcher.analysis.FinancialTransactionAnalyzer
import com.win11launcher.analysis.FinanceSmartSuggestionEngine
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.data.entities.FinancialPattern
import com.win11launcher.data.entities.SmartSuggestion
import com.win11launcher.data.entities.SuggestionCategory
import com.win11launcher.models.AppNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialIntelligenceService @Inject constructor(
    private val context: Context,
    private val financialAnalyzer: FinancialTransactionAnalyzer,
    private val suggestionEngine: FinanceSmartSuggestionEngine
) {
    
    private val database = NotesDatabase.getDatabase(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Process notification for financial intelligence
     * This should be called from the existing RuleEngine after standard processing
     */
    suspend fun processNotificationForFinancialIntelligence(notification: AppNotification) {
        try {
            // Analyze notification for financial patterns
            val financialPattern = financialAnalyzer.analyzeNotification(notification)
            
            financialPattern?.let { pattern ->
                // Store the financial pattern
                database.financialPatternDao().insertPattern(pattern)
                
                // Update existing patterns if this is a recurring transaction
                updateRecurringPatterns(pattern)
                
                // Trigger suggestion generation if we have enough data
                triggerSuggestionGeneration()
            }
        } catch (e: Exception) {
            // Log error but don't break the main notification processing
            android.util.Log.e("FinancialIntelligence", "Error processing notification", e)
        }
    }
    
    /**
     * Generate smart suggestions based on accumulated financial patterns
     */
    suspend fun generateSmartSuggestions(): List<SmartSuggestion> {
        return try {
            val suggestions = suggestionEngine.generateFinancialSuggestions()
            
            // Store new suggestions in database
            suggestions.forEach { suggestion ->
                // Check if similar suggestion already exists
                val existingSuggestions = database.smartSuggestionDao()
                    .getSuggestionsBySubCategory(suggestion.subCategory)
                
                val isDuplicate = existingSuggestions.any { existing ->
                    isSimilarSuggestion(existing, suggestion)
                }
                
                if (!isDuplicate) {
                    database.smartSuggestionDao().insertSuggestion(suggestion)
                }
            }
            
            suggestions
        } catch (e: Exception) {
            android.util.Log.e("FinancialIntelligence", "Error generating suggestions", e)
            emptyList()
        }
    }
    
    /**
     * Get active financial suggestions for the user
     */
    fun getActiveSuggestions(): Flow<List<SmartSuggestion>> {
        return database.smartSuggestionDao().getActiveSuggestions()
    }
    
    /**
     * Get financial suggestions by category
     */
    suspend fun getFinancialSuggestions(): List<SmartSuggestion> {
        return database.smartSuggestionDao().getFinancialSuggestions()
    }
    
    /**
     * Apply a suggestion and create the corresponding tracking rule
     */
    suspend fun applySuggestion(suggestionId: String): Result<String> {
        return try {
            val suggestion = database.smartSuggestionDao().getSuggestionById(suggestionId)
                ?: return Result.failure(Exception("Suggestion not found"))
            
            // Create tracking rule from suggestion config
            val ruleId = createTrackingRuleFromSuggestion(suggestion)
            
            // Mark suggestion as applied
            database.smartSuggestionDao().applySuggestion(suggestionId, System.currentTimeMillis())
            
            Result.success(ruleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Dismiss a suggestion
     */
    suspend fun dismissSuggestion(suggestionId: String, reason: String = "USER_DISMISSED") {
        database.smartSuggestionDao().dismissSuggestion(
            suggestionId, 
            reason, 
            System.currentTimeMillis()
        )
    }
    
    /**
     * Get financial insights based on patterns
     */
    suspend fun getFinancialInsights(): FinancialInsights {
        val patterns = database.financialPatternDao().getRecentPatterns(
            System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000) // Last 30 days
        )
        
        val totalExpenses = patterns
            .filter { it.transactionType == "DEBIT" && it.amount != null }
            .sumOf { it.amount!! }
        
        val totalInvestments = patterns
            .filter { it.transactionType == "INVESTMENT" && it.amount != null }
            .sumOf { it.amount!! }
        
        val categoryBreakdown = patterns
            .filter { it.transactionType == "DEBIT" && it.amount != null }
            .groupBy { it.category }
            .mapValues { (_, categoryPatterns) ->
                categoryPatterns.sumOf { it.amount!! }
            }
        
        val recurringPayments = patterns.filter { it.isRecurring }
        
        return FinancialInsights(
            totalExpenses = totalExpenses,
            totalInvestments = totalInvestments,
            categoryBreakdown = categoryBreakdown,
            recurringPaymentsCount = recurringPayments.size,
            topExpenseCategory = categoryBreakdown.maxByOrNull { it.value }?.key ?: "Unknown",
            transactionCount = patterns.size
        )
    }
    
    /**
     * Periodic cleanup and maintenance
     */
    fun performMaintenance() {
        coroutineScope.launch {
            try {
                // Clean up old patterns (older than 6 months)
                val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
                database.financialPatternDao().deleteOldPatterns(sixMonthsAgo)
                
                // Clean up low confidence patterns
                database.financialPatternDao().deleteLowConfidencePatterns(0.3f)
                
                // Clean up old dismissed suggestions (older than 30 days)
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                database.smartSuggestionDao().deleteOldDismissedSuggestions(thirtyDaysAgo)
                
                // Clean up old applied suggestions (older than 90 days)
                val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                database.smartSuggestionDao().deleteOldAppliedSuggestions(ninetyDaysAgo)
                
            } catch (e: Exception) {
                android.util.Log.e("FinancialIntelligence", "Error during maintenance", e)
            }
        }
    }
    
    /**
     * Update patterns to detect recurring transactions
     */
    private suspend fun updateRecurringPatterns(newPattern: FinancialPattern) {
        if (newPattern.merchant == null || newPattern.amount == null) return
        
        // Look for similar patterns (same merchant and similar amount)
        val similarPatterns = database.financialPatternDao()
            .getPatternsByMerchant(newPattern.merchant)
            .filter { 
                it.amount != null && 
                kotlin.math.abs(it.amount - newPattern.amount) < newPattern.amount * 0.1 // Within 10%
            }
        
        if (similarPatterns.size >= 2) {
            // Check if they're recurring (similar time intervals)
            val timeIntervals = similarPatterns
                .sortedBy { it.lastSeen }
                .zipWithNext { a, b -> b.lastSeen - a.lastSeen }
            
            if (timeIntervals.isNotEmpty()) {
                val avgInterval = timeIntervals.average()
                val isRegular = timeIntervals.all { kotlin.math.abs(it - avgInterval) < avgInterval * 0.2 }
                
                if (isRegular) {
                    // Mark all similar patterns as recurring
                    similarPatterns.forEach { pattern ->
                        val updatedPattern = pattern.copy(
                            isRecurring = true,
                            frequency = when {
                                avgInterval < 7 * 24 * 60 * 60 * 1000 -> "WEEKLY"
                                avgInterval < 35 * 24 * 60 * 60 * 1000 -> "MONTHLY"
                                else -> "YEARLY"
                            },
                            updatedAt = System.currentTimeMillis()
                        )
                        database.financialPatternDao().updatePattern(updatedPattern)
                    }
                }
            }
        }
    }
    
    /**
     * Trigger suggestion generation when we have enough data
     */
    private suspend fun triggerSuggestionGeneration() {
        val totalPatterns = database.financialPatternDao().getRecentPatterns(
            System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // Last 7 days
        ).size
        
        // Generate suggestions if we have accumulated enough patterns
        if (totalPatterns >= 10) {
            coroutineScope.launch {
                generateSmartSuggestions()
            }
        }
    }
    
    /**
     * Create a tracking rule from a suggestion's configuration
     */
    private suspend fun createTrackingRuleFromSuggestion(suggestion: SmartSuggestion): String {
        val ruleId = java.util.UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        
        // Create folder if suggested
        var folderId = "default"
        suggestion.suggestedFolderName?.let { folderName ->
            // Check if folder already exists
            val existingFolder = database.folderDao().getFolderByName(folderName)
            
            folderId = if (existingFolder != null) {
                existingFolder.id
            } else {
                // Create new folder
                val newFolderId = java.util.UUID.randomUUID().toString()
                val folder = com.win11launcher.data.entities.Folder(
                    id = newFolderId,
                    name = folderName,
                    description = "Auto-created from smart suggestion: ${suggestion.title}",
                    color = suggestion.suggestedFolderColor ?: "#2196F3",
                    icon = suggestion.suggestedFolderIcon ?: "folder",
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                database.folderDao().insertFolder(folder)
                newFolderId
            }
        }
        
        // Create tracking rule
        val trackingRule = com.win11launcher.data.entities.TrackingRule(
            id = ruleId,
            name = suggestion.title,
            description = suggestion.description,
            sourcePackages = "[]", // Will be populated from config
            filterType = "KEYWORD_INCLUDE", // Default, will be updated from config
            filterCriteria = suggestion.automatedRuleConfig,
            destinationFolderId = folderId,
            autoTags = "[]", // Will be populated from config
            isActive = true,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        database.trackingRuleDao().insertRule(trackingRule)
        
        return ruleId
    }
    
    /**
     * Check if two suggestions are similar to avoid duplicates
     */
    private fun isSimilarSuggestion(existing: SmartSuggestion, new: SmartSuggestion): Boolean {
        return existing.category == new.category &&
                existing.subCategory == new.subCategory &&
                existing.title.lowercase().contains(new.title.lowercase().split(" ").first())
    }
}

/**
 * Data class for financial insights
 */
data class FinancialInsights(
    val totalExpenses: Double,
    val totalInvestments: Double,
    val categoryBreakdown: Map<String, Double>,
    val recurringPaymentsCount: Int,
    val topExpenseCategory: String,
    val transactionCount: Int
)