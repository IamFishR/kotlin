package com.win11launcher.data.repositories

import com.win11launcher.data.dao.TrackingRuleDao
import com.win11launcher.data.entities.TrackingRule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRuleRepository @Inject constructor(
    private val trackingRuleDao: TrackingRuleDao
) {
    fun getAllRules(): Flow<List<TrackingRule>> = trackingRuleDao.getAllRules()
    
    fun getActiveRules(): Flow<List<TrackingRule>> = trackingRuleDao.getActiveRulesFlow()
    
    suspend fun getRuleById(ruleId: String): TrackingRule? = trackingRuleDao.getRuleById(ruleId)
    
    suspend fun getRulesForPackage(packageName: String): List<TrackingRule> = 
        trackingRuleDao.getRulesForPackage(packageName)
    
    suspend fun getActiveRulesForPackage(packageName: String): List<TrackingRule> = 
        trackingRuleDao.getActiveRulesForPackage(packageName)
    
    suspend fun insertRule(rule: TrackingRule) = trackingRuleDao.insertRule(rule)
    
    suspend fun insertRules(rules: List<TrackingRule>) = trackingRuleDao.insertRules(rules)
    
    suspend fun updateRule(rule: TrackingRule) = trackingRuleDao.updateRule(rule)
    
    suspend fun updateRuleEnabled(ruleId: String, enabled: Boolean) = 
        trackingRuleDao.updateRuleEnabled(ruleId, enabled)
    
    suspend fun updateRuleTriggered(ruleId: String, timestamp: Long) = 
        trackingRuleDao.updateRuleTriggered(ruleId, timestamp)
    
    suspend fun deleteRule(rule: TrackingRule) = trackingRuleDao.deleteRule(rule)
    
    suspend fun deleteRuleById(ruleId: String) = trackingRuleDao.deleteRuleById(ruleId)
    
    suspend fun deleteRulesByPackage(packageName: String) = 
        trackingRuleDao.deleteRulesByPackage(packageName)
    
    suspend fun getActiveRulesList(): List<TrackingRule> = trackingRuleDao.getActiveRulesList()
    
    suspend fun createDefaultRuleForApp(packageName: String, appName: String): TrackingRule {
        val rule = TrackingRule(
            id = java.util.UUID.randomUUID().toString(),
            name = "Auto-track $appName",
            description = "Automatically track all notifications from $appName",
            sourcePackages = "[$packageName]",
            filterType = "ALL",
            filterCriteria = "",
            destinationFolderId = "",
            autoTags = "[\"${appName.lowercase()}\", \"auto-tracked\"]",
            isActive = true,
            priority = 0,
            quietHoursEnabled = false,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            weekdaysOnly = false,
            maxNotesPerDay = -1,
            duplicateDetectionEnabled = true,
            fuzzyDuplicateDetectionEnabled = false,
            duplicateSimilarityThreshold = 0.85,
            duplicateDetectionTimeWindowHours = 24,
            crossRuleDuplicateDetectionEnabled = false,
            minContentLength = 0,
            maxContentLength = -1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastTriggeredAt = null,
            notesCapturedCount = 0,
            totalMatchesCount = 0
        )
        
        insertRule(rule)
        return rule
    }
    
    suspend fun createKeywordRuleForNotification(
        packageName: String,
        appName: String,
        title: String,
        content: String,
        keywords: List<String>
    ): TrackingRule {
        val rule = TrackingRule(
            id = java.util.UUID.randomUUID().toString(),
            name = "Track: ${title.take(30)}...",
            description = "Track notifications with keywords: ${keywords.take(3).joinToString(", ")}",
            sourcePackages = "[$packageName]",
            filterType = "KEYWORD_INCLUDE",
            filterCriteria = "{\"keywords\": [${keywords.joinToString(",") { "\"$it\"" }}]}",
            destinationFolderId = "",
            autoTags = "[\"${appName.lowercase()}\", \"keyword-based\"]",
            isActive = true,
            priority = 0,
            quietHoursEnabled = false,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            weekdaysOnly = false,
            maxNotesPerDay = -1,
            duplicateDetectionEnabled = true,
            fuzzyDuplicateDetectionEnabled = false,
            duplicateSimilarityThreshold = 0.85,
            duplicateDetectionTimeWindowHours = 24,
            crossRuleDuplicateDetectionEnabled = false,
            minContentLength = 0,
            maxContentLength = -1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastTriggeredAt = null,
            notesCapturedCount = 0,
            totalMatchesCount = 0
        )
        
        insertRule(rule)
        return rule
    }
    
    suspend fun getStatistics(): RuleStatistics {
        val allRules = getAllRules()
        var totalRules = 0
        var activeRules = 0
        var totalTriggers = 0
        
        allRules.collect { rules ->
            totalRules = rules.size
            activeRules = rules.count { it.isActive }
            totalTriggers = rules.sumOf { it.totalMatchesCount.toInt() }
        }
        
        return RuleStatistics(
            totalRules = totalRules,
            activeRules = activeRules,
            totalTriggers = totalTriggers
        )
    }
}

data class RuleStatistics(
    val totalRules: Int,
    val activeRules: Int,
    val totalTriggers: Int
)