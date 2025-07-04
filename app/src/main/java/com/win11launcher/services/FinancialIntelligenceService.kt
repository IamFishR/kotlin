package com.win11launcher.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.win11launcher.analysis.NotificationAnalyzer
import com.win11launcher.analysis.GeneralSmartSuggestionEngine
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.data.entities.SmartSuggestion
import com.win11launcher.data.entities.SuggestionSubCategory
import com.win11launcher.data.models.FilterCriteria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialIntelligenceService @Inject constructor(
    private val context: Context,
    private val notificationAnalyzer: NotificationAnalyzer,
    private val suggestionEngine: GeneralSmartSuggestionEngine
) {
    
    private val database = NotesDatabase.getDatabase(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    
    /**
     * Process notification for smart suggestions
     * This should be called from the existing RuleEngine after standard processing
     */
    suspend fun processNotificationForSmartSuggestions(notification: com.win11launcher.models.AppNotification) {
        try {
            // Analyze notification for general patterns
            val extractedData = notificationAnalyzer.analyzeNotification(notification)
            
            // Store the extracted notification data
            database.extractedNotificationDataDao().insert(extractedData)
            
            // Trigger suggestion generation if we have enough data
            triggerSuggestionGeneration()
        } catch (e: Exception) {
            // Log error but don't break the main notification processing
            android.util.Log.e("SmartSuggestions", "Error processing notification", e)
        }
    }
    
    /**
     * Generate smart suggestions based on accumulated notification data
     */
    suspend fun generateSmartSuggestions(): List<SmartSuggestion> {
        return try {
            val allExtractedData = database.extractedNotificationDataDao().getAll().first()
            val suggestions = suggestionEngine.generateSuggestions(allExtractedData)
            
            // Store new suggestions in database
            suggestions.forEach { suggestion ->
                // For now, simply insert. Duplication handling can be more sophisticated later.
                database.smartSuggestionDao().insertSuggestion(suggestion)
            }
            
            suggestions
        } catch (e: Exception) {
            android.util.Log.e("SmartSuggestions", "Error generating suggestions", e)
            emptyList()
        }
    }
    
    /**
     * Get all active smart suggestions for the user
     */
    fun getAllSuggestions(): Flow<List<SmartSuggestion>> {
        return database.smartSuggestionDao().getActiveSuggestions()
    }
    
    /**
     * Get a suggestion by its ID
     */
    suspend fun getSuggestionById(suggestionId: String): SmartSuggestion? {
        return database.smartSuggestionDao().getSuggestionById(suggestionId)
    }
    
    /**
     * Apply a suggestion and create the corresponding tracking rule
     */
    suspend fun applySuggestion(suggestionId: String): Result<String> {
        return try {
            val suggestion = database.smartSuggestionDao().getSuggestionById(suggestionId)
                ?: return Result.failure(Exception("Suggestion not found"))
            
            // Mark suggestion as applied
            database.smartSuggestionDao().applySuggestion(suggestionId, System.currentTimeMillis())
            
            Result.success(suggestionId) // Return suggestionId for now, rule creation handled by ViewModel
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
     * Periodic cleanup and maintenance
     */
    fun performMaintenance() {
        coroutineScope.launch {
            try {
                // Clean up old dismissed suggestions (older than 30 days)
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                database.smartSuggestionDao().deleteOldDismissedSuggestions(thirtyDaysAgo)
                
                // Clean up old applied suggestions (older than 90 days)
                val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                database.smartSuggestionDao().deleteOldAppliedSuggestions(ninetyDaysAgo)
                
                // Clean up old extracted notification data (older than 7 days)
                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                database.extractedNotificationDataDao().deleteOldData(sevenDaysAgo)
                
            } catch (e: Exception) {
                android.util.Log.e("SmartSuggestions", "Error during maintenance", e)
            }
        }
    }
    
    /**
     * Trigger suggestion generation when we have enough data
     */
    private suspend fun triggerSuggestionGeneration() {
        val totalExtractedData = database.extractedNotificationDataDao().getAll().first().size
        
        // Generate suggestions if we have accumulated enough data (e.g., more than 10 notifications)
        if (totalExtractedData >= 10) {
            coroutineScope.launch {
                generateSmartSuggestions()
            }
        }
    }
}