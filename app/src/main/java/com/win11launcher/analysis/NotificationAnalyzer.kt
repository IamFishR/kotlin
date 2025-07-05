package com.win11launcher.analysis

import android.content.Context
import com.win11launcher.models.AppNotification
import com.win11launcher.data.entities.ExtractedNotificationData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAnalyzer @Inject constructor(
    private val context: Context
) {

    fun analyzeNotification(notification: AppNotification): ExtractedNotificationData {
        // TODO: Implement logic to extract keywords, identify categories, etc.
        // For now, a basic implementation:
        val extractedKeywords = extractKeywords(notification.title + " " + notification.content)
        val suggestedCategory = determineCategory(notification)

        return ExtractedNotificationData(
            sourcePackage = notification.packageName,
            notificationTitle = notification.title,
            notificationContent = notification.content,
            extractedKeywords = extractedKeywords,
            suggestedCategory = suggestedCategory,
            timestamp = notification.timestamp
        )
    }

    private fun extractKeywords(text: String): List<String> {
        // Simple keyword extraction: split by space and filter common words
        return text.lowercase()
            .replace("[^a-zA-Z0-9\\s]".toRegex(), "") // Remove special characters
            .split(" ", ",", ".", "!", "?")
            .filter { it.isNotBlank() && it.length > 2 && it !in COMMON_WORDS }
            .distinct()
    }

    private fun determineCategory(notification: AppNotification): String {
        // TODO: Implement more sophisticated category determination based on content, app, etc.
        // For now, a placeholder:
        val titleAndContent = (notification.title + " " + notification.content).lowercase()
        return when {
            titleAndContent.contains("otp") || titleAndContent.contains("transaction") || titleAndContent.contains("bank") -> "FINANCE"
            titleAndContent.contains("meeting") || titleAndContent.contains("task") -> "PRODUCTIVITY"
            titleAndContent.contains("update") || titleAndContent.contains("news") -> "GENERAL"
            else -> "GENERAL"
        }
    }

    companion object {
        private val COMMON_WORDS = setOf(
            "a", "an", "the", "is", "am", "are", "was", "were", "be", "been", "being",
            "and", "or", "but", "if", "then", "else", "when", "where", "how", "what",
            "this", "that", "these", "those", "of", "on", "in", "at", "by", "for",
            "with", "from", "to", "as", "it", "its", "he", "she", "they", "we", "you",
            "i", "me", "him", "her", "us", "them", "my", "your", "our", "their",
            "can", "will", "would", "should", "could", "has", "have", "had", "do", "does",
            "did", "not", "no", "yes", "up", "down", "out", "in", "on", "off", "about",
            "just", "get", "go", "come", "make", "know", "see", "take", "think", "look",
            "want", "give", "use", "find", "tell", "ask", "work", "seem", "feel", "try",
            "leave", "call", "good", "new", "first", "last", "long", "great", "little",
            "own", "other", "old", "right", "big", "high", "different", "small", "large",
            "next", "early", "important", "few", "public", "bad", "same", "able"
        )
    }
}