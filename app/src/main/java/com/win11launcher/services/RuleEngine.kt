package com.win11launcher.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.data.entities.Note
import com.win11launcher.data.entities.TrackingRule
import com.win11launcher.data.entities.RuleActivity
import com.win11launcher.data.models.FilterCriteria
import com.win11launcher.data.models.FilterType
import com.win11launcher.models.AppNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

class RuleEngine(private val context: Context) {
    
    private val database = NotesDatabase.getDatabase(context)
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    suspend fun processNotification(notification: AppNotification) {
        val activeRules = database.trackingRuleDao().getRulesForPackage(notification.packageName)
        
        for (rule in activeRules) {
            try {
                processRuleForNotification(rule, notification)
            } catch (e: Exception) {
                logRuleActivity(
                    rule.id,
                    "ERROR",
                    notification,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    private suspend fun processRuleForNotification(rule: TrackingRule, notification: AppNotification) {
        // Check if rule should be skipped
        val skipReason = shouldSkipRule(rule, notification)
        if (skipReason != null) {
            logRuleActivity(rule.id, "SKIPPED", notification, skipReason = skipReason)
            database.trackingRuleDao().updateRuleMatched(rule.id)
            return
        }
        
        // Check if notification matches rule criteria
        if (!matchesFilterCriteria(rule, notification)) {
            return // Don't log non-matches to avoid spam
        }
        
        // Check for duplicates if enabled
        if (rule.duplicateDetectionEnabled && isDuplicate(rule, notification)) {
            logRuleActivity(rule.id, "SKIPPED", notification, skipReason = "DUPLICATE")
            database.trackingRuleDao().updateRuleMatched(rule.id)
            return
        }
        
        // Create the note
        val note = createNoteFromNotification(rule, notification)
        database.noteDao().insertNote(note)
        
        // Update rule statistics
        database.trackingRuleDao().updateRuleTriggered(rule.id, System.currentTimeMillis())
        
        // Log successful activity
        logRuleActivity(rule.id, "TRIGGERED", notification, noteCreatedId = note.id)
    }
    
    private suspend fun shouldSkipRule(rule: TrackingRule, notification: AppNotification): String? {
        // Check quiet hours
        if (rule.quietHoursEnabled && isInQuietHours(rule)) {
            return "QUIET_HOURS"
        }
        
        // Check weekdays only
        if (rule.weekdaysOnly && !isWeekday()) {
            return "WEEKEND"
        }
        
        // Check daily limit
        if (rule.maxNotesPerDay > 0) {
            val todayStart = getTodayStartTimestamp()
            val todayCount = database.noteDao().getNotesCountByRuleSince(rule.id, todayStart)
            if (todayCount >= rule.maxNotesPerDay) {
                return "DAILY_LIMIT"
            }
        }
        
        // Check content length limits
        val contentLength = "${notification.title} ${notification.content}".length
        if (rule.minContentLength > 0 && contentLength < rule.minContentLength) {
            return "TOO_SHORT"
        }
        if (rule.maxContentLength > 0 && contentLength > rule.maxContentLength) {
            return "TOO_LONG"
        }
        
        return null
    }
    
    private fun matchesFilterCriteria(rule: TrackingRule, notification: AppNotification): Boolean {
        val filterType = FilterType.valueOf(rule.filterType)
        val combinedContent = "${notification.title} ${notification.content}"
        
        return when (filterType) {
            FilterType.ALL -> true
            
            FilterType.KEYWORD_INCLUDE -> {
                val criteria = gson.fromJson(rule.filterCriteria, FilterCriteria::class.java)
                val content = if (criteria.caseSensitive) combinedContent else combinedContent.lowercase()
                criteria.keywords.any { keyword ->
                    val searchKeyword = if (criteria.caseSensitive) keyword else keyword.lowercase()
                    content.contains(searchKeyword)
                }
            }
            
            FilterType.KEYWORD_EXCLUDE -> {
                val criteria = gson.fromJson(rule.filterCriteria, FilterCriteria::class.java)
                val content = if (criteria.caseSensitive) combinedContent else combinedContent.lowercase()
                criteria.excludeKeywords.none { keyword ->
                    val searchKeyword = if (criteria.caseSensitive) keyword else keyword.lowercase()
                    content.contains(searchKeyword)
                }
            }
            
            FilterType.REGEX -> {
                val criteria = gson.fromJson(rule.filterCriteria, FilterCriteria::class.java)
                try {
                    val flags = if (criteria.caseSensitive) 0 else Pattern.CASE_INSENSITIVE
                    val pattern = Pattern.compile(criteria.regexPattern, flags)
                    pattern.matcher(combinedContent).find()
                } catch (e: Exception) {
                    false // Invalid regex
                }
            }
        }
    }
    
    private suspend fun isDuplicate(rule: TrackingRule, notification: AppNotification): Boolean {
        val recentTimeThreshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours
        val content = "${notification.title} ${notification.content}"
        
        // Look for similar content in recent notes from this rule
        val similarNote = database.noteDao().findSimilarRecentNote(
            rule.id,
            content.take(50), // Use first 50 chars for similarity check
            recentTimeThreshold
        )
        
        return similarNote != null
    }
    
    private fun createNoteFromNotification(rule: TrackingRule, notification: AppNotification): Note {
        val noteId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        
        // Parse auto tags
        val autoTags = if (rule.autoTags.isNotEmpty()) {
            try {
                val tagsList: List<String> = gson.fromJson(rule.autoTags, object : TypeToken<List<String>>() {}.type)
                gson.toJson(tagsList)
            } catch (e: Exception) {
                "[]"
            }
        } else {
            "[]"
        }
        
        return Note(
            id = noteId,
            title = notification.title.ifEmpty { "Notification from ${notification.appName}" },
            content = notification.content,
            sourcePackage = notification.packageName,
            sourceAppName = notification.appName,
            folderId = rule.destinationFolderId,
            ruleId = rule.id,
            tags = autoTags,
            createdAt = currentTime,
            updatedAt = currentTime,
            originalNotificationId = notification.id,
            notificationTimestamp = notification.timestamp
        )
    }
    
    private suspend fun logRuleActivity(
        ruleId: String,
        actionType: String,
        notification: AppNotification,
        noteCreatedId: String? = null,
        skipReason: String = "",
        errorMessage: String = ""
    ) {
        val activity = RuleActivity(
            id = UUID.randomUUID().toString(),
            ruleId = ruleId,
            actionType = actionType,
            notificationTitle = notification.title,
            notificationContent = notification.content,
            sourcePackage = notification.packageName,
            noteCreatedId = noteCreatedId,
            skipReason = skipReason,
            errorMessage = errorMessage,
            timestamp = System.currentTimeMillis()
        )
        
        database.ruleActivityDao().insertActivity(activity)
    }
    
    private fun isInQuietHours(rule: TrackingRule): Boolean {
        val now = LocalTime.now()
        val startTime = LocalTime.parse(rule.quietHoursStart, DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.parse(rule.quietHoursEnd, DateTimeFormatter.ofPattern("HH:mm"))
        
        return if (startTime.isBefore(endTime)) {
            // Same day range (e.g., 09:00 to 17:00)
            now.isAfter(startTime) && now.isBefore(endTime)
        } else {
            // Overnight range (e.g., 22:00 to 08:00)
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }
    
    private fun isWeekday(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
    }
    
    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun cleanupOldData() {
        coroutineScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            
            // Clean up old rule activity (keep 30 days)
            database.ruleActivityDao().deleteOldActivity(thirtyDaysAgo)
            
            // Clean up old archived notes (keep 90 days)
            val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
            database.noteDao().deleteOldArchivedNotes(ninetyDaysAgo)
        }
    }
}