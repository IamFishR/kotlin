package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.TrackingRule
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingRuleDao {
    
    @Query("SELECT * FROM tracking_rules ORDER BY priority DESC, created_at DESC")
    fun getAllRules(): Flow<List<TrackingRule>>
    
    @Query("SELECT * FROM tracking_rules WHERE is_active = 1 ORDER BY priority DESC")
    suspend fun getActiveRules(): List<TrackingRule>
    
    @Query("SELECT * FROM tracking_rules WHERE is_active = 1 ORDER BY priority DESC")
    fun getActiveRulesFlow(): Flow<List<TrackingRule>>
    
    @Query("SELECT * FROM tracking_rules WHERE is_active = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesList(): List<TrackingRule>
    
    @Query("SELECT * FROM tracking_rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: String): TrackingRule?
    
    @Query("SELECT * FROM tracking_rules WHERE source_packages LIKE '%' || :packageName || '%' AND is_active = 1 ORDER BY priority DESC")
    suspend fun getRulesForPackage(packageName: String): List<TrackingRule>
    
    @Query("SELECT * FROM tracking_rules WHERE source_packages LIKE '%' || :packageName || '%' AND is_active = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesForPackage(packageName: String): List<TrackingRule>
    
    @Query("SELECT * FROM tracking_rules WHERE destination_folder_id = :folderId")
    fun getRulesByFolder(folderId: String): Flow<List<TrackingRule>>
    
    @Query("SELECT COUNT(*) FROM tracking_rules")
    suspend fun getRulesCount(): Int
    
    @Query("SELECT COUNT(*) FROM tracking_rules WHERE is_active = 1")
    suspend fun getActiveRulesCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TrackingRule)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<TrackingRule>)
    
    @Update
    suspend fun updateRule(rule: TrackingRule)
    
    @Query("UPDATE tracking_rules SET is_active = :isActive WHERE id = :ruleId")
    suspend fun updateRuleActive(ruleId: String, isActive: Boolean)
    
    @Query("UPDATE tracking_rules SET is_active = :enabled WHERE id = :ruleId")
    suspend fun updateRuleEnabled(ruleId: String, enabled: Boolean)
    
    @Query("UPDATE tracking_rules SET last_triggered_at = :timestamp, notes_captured_count = notes_captured_count + 1, total_matches_count = total_matches_count + 1 WHERE id = :ruleId")
    suspend fun updateRuleTriggered(ruleId: String, timestamp: Long)
    
    @Query("UPDATE tracking_rules SET total_matches_count = total_matches_count + 1 WHERE id = :ruleId")
    suspend fun updateRuleMatched(ruleId: String)
    
    @Query("UPDATE tracking_rules SET priority = :priority WHERE id = :ruleId")
    suspend fun updateRulePriority(ruleId: String, priority: Int)
    
    @Delete
    suspend fun deleteRule(rule: TrackingRule)
    
    @Query("DELETE FROM tracking_rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: String)
    
    @Query("DELETE FROM tracking_rules WHERE source_packages LIKE '%' || :packageName || '%'")
    suspend fun deleteRulesByPackage(packageName: String)
}