package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.SmartSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartSuggestionDao {
    
    @Query("SELECT * FROM smart_suggestions WHERE is_dismissed = 0 AND is_applied = 0 ORDER BY priority ASC, confidence_score DESC")
    fun getActiveSuggestions(): Flow<List<SmartSuggestion>>
    
    @Query("SELECT * FROM smart_suggestions WHERE id = :id")
    suspend fun getSuggestionById(id: String): SmartSuggestion?
    
    @Query("SELECT * FROM smart_suggestions WHERE category = :category AND is_dismissed = 0 ORDER BY confidence_score DESC")
    suspend fun getSuggestionsByCategory(category: String): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE sub_category = :subCategory AND is_dismissed = 0 ORDER BY confidence_score DESC")
    suspend fun getSuggestionsBySubCategory(subCategory: String): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE is_finance_related = 1 AND is_dismissed = 0 ORDER BY priority ASC, confidence_score DESC")
    suspend fun getFinancialSuggestions(): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE priority = :priority AND is_dismissed = 0 ORDER BY confidence_score DESC")
    suspend fun getSuggestionsByPriority(priority: Int): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE confidence_score >= :minScore AND is_dismissed = 0 ORDER BY confidence_score DESC")
    suspend fun getHighConfidenceSuggestions(minScore: Float): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE is_applied = 1 ORDER BY application_date DESC")
    suspend fun getAppliedSuggestions(): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE is_dismissed = 1 ORDER BY updated_at DESC")
    suspend fun getDismissedSuggestions(): List<SmartSuggestion>
    
    @Query("SELECT * FROM smart_suggestions WHERE created_at >= :since ORDER BY created_at DESC")
    suspend fun getRecentSuggestions(since: Long): List<SmartSuggestion>
    
    @Query("SELECT COUNT(*) FROM smart_suggestions WHERE category = :category AND is_dismissed = 0")
    suspend fun getSuggestionCountByCategory(category: String): Int
    
    @Query("SELECT COUNT(*) FROM smart_suggestions WHERE is_finance_related = 1 AND is_dismissed = 0")
    suspend fun getFinancialSuggestionCount(): Int
    
    @Query("SELECT COUNT(*) FROM smart_suggestions WHERE priority = 1 AND is_dismissed = 0")
    suspend fun getHighPrioritySuggestionCount(): Int
    
    @Query("SELECT AVG(confidence_score) FROM smart_suggestions WHERE category = :category")
    suspend fun getAverageConfidenceByCategory(category: String): Double?
    
    @Query("SELECT DISTINCT category FROM smart_suggestions WHERE is_dismissed = 0 ORDER BY category")
    suspend fun getActiveCategories(): List<String>
    
    @Query("SELECT DISTINCT sub_category FROM smart_suggestions WHERE category = :category AND is_dismissed = 0 ORDER BY sub_category")
    suspend fun getActiveSubCategories(category: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SmartSuggestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<SmartSuggestion>)
    
    @Update
    suspend fun updateSuggestion(suggestion: SmartSuggestion)
    
    @Delete
    suspend fun deleteSuggestion(suggestion: SmartSuggestion)
    
    @Query("DELETE FROM smart_suggestions WHERE id = :id")
    suspend fun deleteSuggestionById(id: String)
    
    @Query("UPDATE smart_suggestions SET is_dismissed = 1, dismissal_reason = :reason, updated_at = :timestamp WHERE id = :id")
    suspend fun dismissSuggestion(id: String, reason: String, timestamp: Long)
    
    @Query("UPDATE smart_suggestions SET is_applied = 1, application_date = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun applySuggestion(id: String, timestamp: Long)
    
    @Query("UPDATE smart_suggestions SET confidence_score = :score, updated_at = :timestamp WHERE id = :id")
    suspend fun updateConfidenceScore(id: String, score: Float, timestamp: Long)
    
    @Query("UPDATE smart_suggestions SET priority = :priority, updated_at = :timestamp WHERE id = :id")
    suspend fun updatePriority(id: String, priority: Int, timestamp: Long)
    
    @Query("DELETE FROM smart_suggestions WHERE is_dismissed = 1 AND updated_at < :cutoff")
    suspend fun deleteOldDismissedSuggestions(cutoff: Long)
    
    @Query("DELETE FROM smart_suggestions WHERE is_applied = 1 AND application_date < :cutoff")
    suspend fun deleteOldAppliedSuggestions(cutoff: Long)
    
    @Query("DELETE FROM smart_suggestions WHERE confidence_score < :minScore AND priority > 2")
    suspend fun deleteLowConfidenceSuggestions(minScore: Float)
}