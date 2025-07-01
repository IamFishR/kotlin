package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.RuleActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleActivityDao {
    
    @Query("SELECT * FROM rule_activity ORDER BY timestamp DESC LIMIT 100")
    fun getRecentActivity(): Flow<List<RuleActivity>>
    
    @Query("SELECT * FROM rule_activity WHERE rule_id = :ruleId ORDER BY timestamp DESC")
    fun getActivityForRule(ruleId: String): Flow<List<RuleActivity>>
    
    @Query("SELECT * FROM rule_activity WHERE rule_id = :ruleId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getActivityForRuleInRange(ruleId: String, startTime: Long, endTime: Long): Flow<List<RuleActivity>>
    
    @Query("SELECT COUNT(*) FROM rule_activity WHERE rule_id = :ruleId AND action_type = 'TRIGGERED' AND timestamp >= :startTime")
    suspend fun getTriggeredCountSince(ruleId: String, startTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM rule_activity WHERE rule_id = :ruleId AND action_type = 'SKIPPED' AND timestamp >= :startTime")
    suspend fun getSkippedCountSince(ruleId: String, startTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM rule_activity WHERE rule_id = :ruleId AND action_type = 'ERROR' AND timestamp >= :startTime")
    suspend fun getErrorCountSince(ruleId: String, startTime: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: RuleActivity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<RuleActivity>)
    
    @Query("DELETE FROM rule_activity WHERE timestamp < :cutoffTime")
    suspend fun deleteOldActivity(cutoffTime: Long)
    
    @Query("DELETE FROM rule_activity WHERE rule_id = :ruleId")
    suspend fun deleteActivityForRule(ruleId: String)
}