package com.win11launcher.analysis

import com.win11launcher.data.entities.ExtractedNotificationData
import com.win11launcher.data.entities.SmartSuggestion
import com.win11launcher.data.entities.SuggestionCategory
import com.win11launcher.data.entities.SuggestionPriority
import com.win11launcher.data.entities.SuggestionSubCategory
import com.google.gson.Gson
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralSmartSuggestionEngine @Inject constructor() {

    private val gson = Gson()

    fun generateSuggestions(notifications: List<ExtractedNotificationData>): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()

        // Example: Suggest a rule for frequently occurring keywords from a specific app
        val groupedByApp = notifications.groupBy { it.sourcePackage }

        groupedByApp.forEach { (packageName, appNotifications) ->
            val keywordCounts = mutableMapOf<String, Int>()
            appNotifications.forEach { notification ->
                notification.extractedKeywords.forEach { keyword ->
                    keywordCounts[keyword] = (keywordCounts[keyword] ?: 0) + 1
                }
            }

            // Filter keywords that appear frequently (e.g., more than 5 times)
            val frequentKeywords = keywordCounts.filter { it.value > 5 }.keys

            if (frequentKeywords.isNotEmpty()) {
                val title = "Create rule for \"${frequentKeywords.first()}\" from ${packageName}"
                val description = "Automatically organize notifications containing keywords like ${frequentKeywords.joinToString(", ")} from this app."
                val suggestedTags = frequentKeywords.toList()

                val suggestion = SmartSuggestion(
                    id = UUID.randomUUID().toString(),
                    category = SuggestionCategory.GENERAL,
                    subCategory = SuggestionSubCategory.KEYWORD_DETECTION,
                    title = title,
                    description = description,
                    sourcePackage = packageName,
                    extractedKeywords = gson.toJson(frequentKeywords),
                    suggestedTags = gson.toJson(suggestedTags),
                    confidenceScore = 0.7f, // Example score
                    priority = SuggestionPriority.MEDIUM,
                    createdAt = System.currentTimeMillis()
                )
                suggestions.add(suggestion)
            }
        }

        // TODO: Add more sophisticated suggestion generation logic here
        // - Recurring patterns (e.g., monthly bills, weekly reports)
        // - Important information extraction (e.g., OTPs, flight details)
        // - App usage patterns (e.g., frequently used apps for certain tasks)

        return suggestions
    }
}