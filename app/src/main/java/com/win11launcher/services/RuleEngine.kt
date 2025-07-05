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
        // Global duplicate check: Check if this notification ID already exists
        val existingNote = database.noteDao().getNoteByOriginalNotificationId(notification.id)
        if (existingNote != null) {
            // Notification already processed, skip entirely
            return
        }
        
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
        val timeWindowMs = rule.duplicateDetectionTimeWindowHours * 60 * 60 * 1000L
        val recentTimeThreshold = System.currentTimeMillis() - timeWindowMs
        
        // First check for exact match (faster)
        val exactMatch = database.noteDao().findExactRecentNote(
            rule.id,
            notification.title,
            notification.content,
            recentTimeThreshold
        )
        
        if (exactMatch != null) {
            return true
        }
        
        // If fuzzy detection is enabled, check for similar content
        if (rule.fuzzyDuplicateDetectionEnabled) {
            val recentNotes = if (rule.crossRuleDuplicateDetectionEnabled) {
                // Check across all rules
                database.noteDao().findAllRecentNotes(recentTimeThreshold)
            } else {
                // Check only within the same rule
                database.noteDao().findRecentNotesByRule(rule.id, recentTimeThreshold)
            }
            
            val normalizedNewContent = normalizeContent(notification.title, notification.content)
            
            for (note in recentNotes) {
                val normalizedExistingContent = normalizeContent(note.title, note.content)
                val similarity = calculateTextSimilarity(normalizedNewContent, normalizedExistingContent)
                
                if (similarity >= rule.duplicateSimilarityThreshold) {
                    return true
                }
            }
        }
        
        // If cross-rule detection is enabled but fuzzy is disabled, check exact matches across all rules
        if (rule.crossRuleDuplicateDetectionEnabled && !rule.fuzzyDuplicateDetectionEnabled) {
            val allRecentNotes = database.noteDao().findAllRecentNotes(recentTimeThreshold)
            for (note in allRecentNotes) {
                if (note.title == notification.title && note.content == notification.content) {
                    return true
                }
            }
        }
        
        return false
    }
    
    private fun normalizeContent(title: String, content: String): String {
        val combined = "$title $content"
        return combined.trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters for comparison
    }
    
    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        if (text1 == text2) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0
        
        // Use Levenshtein distance for similarity calculation
        val distance = levenshteinDistance(text1, text2)
        val maxLength = maxOf(text1.length, text2.length)
        
        return if (maxLength == 0) 1.0 else 1.0 - (distance.toDouble() / maxLength.toDouble())
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
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