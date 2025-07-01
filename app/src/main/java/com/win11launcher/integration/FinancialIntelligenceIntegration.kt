package com.win11launcher.integration

import android.content.Context
import com.win11launcher.data.entities.SmartSuggestion
import com.win11launcher.services.FinancialIntelligenceService
import com.win11launcher.services.FinancialInsights
import com.win11launcher.services.Win11NotificationListenerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Integration helper class to provide easy access to financial intelligence features
 * from ViewModels and other components
 */
class FinancialIntelligenceIntegration(private val context: Context) {
    
    private val financialService: FinancialIntelligenceService?
        get() = Win11NotificationListenerService.getFinancialIntelligenceService()
    
    /**
     * Get active smart suggestions
     */
    fun getActiveSuggestions(): Flow<List<SmartSuggestion>> {
        return financialService?.getActiveSuggestions() ?: flowOf(emptyList())
    }
    
    /**
     * Get financial suggestions specifically
     */
    suspend fun getFinancialSuggestions(): List<SmartSuggestion> {
        return financialService?.getFinancialSuggestions() ?: emptyList()
    }
    
    /**
     * Apply a suggestion and create corresponding tracking rule
     */
    suspend fun applySuggestion(suggestionId: String): Result<String> {
        return financialService?.applySuggestion(suggestionId) 
            ?: Result.failure(Exception("Financial intelligence service not available"))
    }
    
    /**
     * Dismiss a suggestion
     */
    suspend fun dismissSuggestion(suggestionId: String, reason: String = "USER_DISMISSED") {
        financialService?.dismissSuggestion(suggestionId, reason)
    }
    
    /**
     * Generate new smart suggestions
     */
    suspend fun generateSmartSuggestions(): List<SmartSuggestion> {
        return financialService?.generateSmartSuggestions() ?: emptyList()
    }
    
    /**
     * Get financial insights
     */
    suspend fun getFinancialInsights(): FinancialInsights? {
        return financialService?.getFinancialInsights()
    }
    
    /**
     * Check if financial intelligence service is available
     */
    fun isServiceAvailable(): Boolean {
        return financialService != null
    }
    
    /**
     * Perform maintenance on financial data
     */
    fun performMaintenance() {
        financialService?.performMaintenance()
    }
}

/**
 * Extension function for easy access to financial intelligence integration
 */
fun Context.getFinancialIntelligence(): FinancialIntelligenceIntegration {
    return FinancialIntelligenceIntegration(this)
}