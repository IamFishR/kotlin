package com.win11launcher.services

import android.content.Context
import com.win11launcher.command.CommandDefinition
import com.win11launcher.command.CommandRegistry
import com.win11launcher.command.CommandCategory
import com.win11launcher.data.repositories.CommandLineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class SmartCommandCompletion @Inject constructor(
    private val commandRegistry: CommandRegistry,
    private val commandLineRepository: CommandLineRepository,
    private val aiMemoryManager: AIMemoryManager,
    private val aiService: AIService
) {
    
    companion object {
        private const val MAX_SUGGESTIONS = 8
        private const val MIN_SCORE_THRESHOLD = 0.2f
        private const val LEARNING_DECAY_FACTOR = 0.95f
    }
    
    // Weighted scoring factors
    private val scoringWeights = mapOf(
        "frequency" to 0.3f,
        "recency" to 0.2f,
        "context" to 0.25f,
        "textSimilarity" to 0.15f,
        "aiInsight" to 0.1f
    )
    
    suspend fun getSmartSuggestions(
        input: String,
        context: Context,
        userId: String = "USER_DEFAULT",
        conversationId: String? = null
    ): List<SmartCommandSuggestion> = withContext(Dispatchers.IO) {
        
        if (input.isBlank()) {
            return@withContext getContextualSuggestions(context, userId)
        }
        
        val allCommands = commandRegistry.getAllCommands()
        val recentCommands = commandLineRepository.getRecentCommands(50)
        val userMemories = aiMemoryManager.retrieveRelevantMemories(userId, input, 5)
        
        val suggestions = allCommands.map { command ->
            calculateCommandScore(
                command = command,
                input = input,
                recentCommands = recentCommands,
                userMemories = userMemories,
                context = context
            )
        }
        
        val filteredSuggestions = suggestions
            .filter { it.totalScore >= MIN_SCORE_THRESHOLD }
            .sortedByDescending { it.totalScore }
            .take(MAX_SUGGESTIONS)
        
        // Enhance top suggestions with AI insights if needed
        enhanceWithAI(filteredSuggestions, input, context, userId)
    }
    
    suspend fun getContextualSuggestions(
        context: Context,
        userId: String = "USER_DEFAULT"
    ): List<SmartCommandSuggestion> = withContext(Dispatchers.IO) {
        
        val recentCommands = commandLineRepository.getRecentCommands(20)
        val frequentCommands = getFrequentlyUsedCommands()
        val contextualCommands = getContextBasedCommands(context)
        
        val suggestions = mutableListOf<SmartCommandSuggestion>()
        
        // Add recent commands with decay
        recentCommands.take(3).forEachIndexed { index, cmdName ->
            val command = commandRegistry.getCommand(cmdName)
            if (command != null) {
                val recencyScore = 1.0f - (index * 0.2f)
                suggestions.add(
                    SmartCommandSuggestion(
                        command = command,
                        completionText = cmdName,
                        reason = "Recently used",
                        totalScore = recencyScore * 0.8f,
                        scoreBreakdown = mapOf("recency" to recencyScore),
                        contextHint = "You used this recently"
                    )
                )
            }
        }
        
        // Add frequent commands
        frequentCommands.take(3).forEach { (cmdName, frequency) ->
            val command = commandRegistry.getCommand(cmdName)
            if (command != null && suggestions.none { it.command.name == cmdName }) {
                suggestions.add(
                    SmartCommandSuggestion(
                        command = command,
                        completionText = cmdName,
                        reason = "Frequently used",
                        totalScore = min(frequency / 10f, 1.0f) * 0.7f,
                        scoreBreakdown = mapOf("frequency" to frequency),
                        contextHint = "You use this often"
                    )
                )
            }
        }
        
        // Add contextual commands
        contextualCommands.forEach { suggestion ->
            if (suggestions.none { it.command.name == suggestion.command.name }) {
                suggestions.add(suggestion)
            }
        }
        
        suggestions.sortedByDescending { it.totalScore }.take(MAX_SUGGESTIONS)
    }
    
    suspend fun getParameterSuggestions(
        commandName: String,
        currentParameters: Map<String, String>,
        input: String,
        userId: String = "USER_DEFAULT"
    ): List<ParameterSuggestion> = withContext(Dispatchers.IO) {
        
        val command = commandRegistry.getCommand(commandName) ?: return@withContext emptyList()
        val suggestions = mutableListOf<ParameterSuggestion>()
        
        // Get parameter suggestions based on command definition
        command.parameters.forEach { param ->
            if (!currentParameters.containsKey(param.name)) {
                val suggestion = when {
                    param.options.isNotEmpty() -> {
                        // Enum-like parameter with predefined options
                        ParameterSuggestion(
                            parameterName = param.name,
                            suggestedValue = param.options.first(),
                            options = param.options,
                            description = param.description,
                            isRequired = param.required
                        )
                    }
                    param.defaultValue != null -> {
                        // Parameter with default value
                        ParameterSuggestion(
                            parameterName = param.name,
                            suggestedValue = param.defaultValue,
                            options = emptyList(),
                            description = param.description,
                            isRequired = param.required
                        )
                    }
                    else -> {
                        // Generate suggestion based on parameter type and user history
                        generateParameterSuggestion(param, commandName, userId)
                    }
                }
                suggestions.add(suggestion)
            }
        }
        
        suggestions.sortedBy { if (it.isRequired) 0 else 1 }
    }
    
    private suspend fun calculateCommandScore(
        command: CommandDefinition,
        input: String,
        recentCommands: List<String>,
        userMemories: List<com.win11launcher.data.entities.LongTermMemory>,
        context: Context
    ): SmartCommandSuggestion {
        
        val scores = mutableMapOf<String, Float>()
        
        // Text similarity score
        scores["textSimilarity"] = calculateTextSimilarity(input, command)
        
        // Frequency score
        val frequency = recentCommands.count { it == command.name }
        scores["frequency"] = min(frequency.toFloat() / 5f, 1.0f)
        
        // Recency score
        val recentIndex = recentCommands.indexOf(command.name)
        scores["recency"] = if (recentIndex >= 0) {
            1.0f - (recentIndex.toFloat() / recentCommands.size)
        } else 0.0f
        
        // Context score based on user memories
        scores["context"] = calculateContextScore(command, userMemories)
        
        // AI insight score (placeholder for future enhancement)
        scores["aiInsight"] = 0.0f
        
        // Calculate weighted total score
        val totalScore = scores.entries.sumOf { (key, score) ->
            ((scoringWeights[key] ?: 0.0f) * score).toDouble()
        }.toFloat()
        
        val reason = generateSuggestionReason(scores, command)
        val completionText = generateCompletionText(command, input)
        val contextHint = generateContextHint(command, scores)
        
        return SmartCommandSuggestion(
            command = command,
            completionText = completionText,
            reason = reason,
            totalScore = totalScore,
            scoreBreakdown = scores.toMap(),
            contextHint = contextHint
        )
    }
    
    private fun calculateTextSimilarity(input: String, command: CommandDefinition): Float {
        val inputLower = input.lowercase()
        val commandName = command.name.lowercase()
        
        // Exact match
        if (inputLower == commandName) return 1.0f
        
        // Prefix match
        if (commandName.startsWith(inputLower)) {
            return 0.8f + (inputLower.length.toFloat() / commandName.length) * 0.2f
        }
        
        // Alias match
        val aliasMatch = command.aliases.any { alias ->
            alias.lowercase().startsWith(inputLower)
        }
        if (aliasMatch) return 0.7f
        
        // Description keyword match
        val descriptionMatch = command.description.lowercase().contains(inputLower)
        if (descriptionMatch) return 0.5f
        
        // Fuzzy match using Levenshtein distance
        val distance = levenshteinDistance(inputLower, commandName)
        val maxLength = max(inputLower.length, commandName.length)
        if (maxLength == 0) return 0.0f
        
        val similarity = 1.0f - (distance.toFloat() / maxLength)
        return max(similarity - 0.3f, 0.0f) // Threshold for fuzzy matching
    }
    
    private fun calculateContextScore(
        command: CommandDefinition,
        userMemories: List<com.win11launcher.data.entities.LongTermMemory>
    ): Float {
        if (userMemories.isEmpty()) return 0.0f
        
        val commandKeywords = listOf(command.name) + command.aliases + 
                             command.description.split(" ").filter { it.length > 3 }
        
        val memoryText = userMemories.joinToString(" ") { it.memoryText.lowercase() }
        
        val keywordMatches = commandKeywords.count { keyword ->
            memoryText.contains(keyword.lowercase())
        }
        
        return min(keywordMatches.toFloat() / commandKeywords.size, 1.0f)
    }
    
    private suspend fun getFrequentlyUsedCommands(): List<Pair<String, Float>> {
        // This would query the command usage statistics from the database
        // For now, return a placeholder implementation
        return emptyList()
    }
    
    private suspend fun getContextBasedCommands(context: Context): List<SmartCommandSuggestion> {
        // Generate contextual suggestions based on system state
        val suggestions = mutableListOf<SmartCommandSuggestion>()
        
        // Example: Suggest battery command if battery is low
        // Example: Suggest network commands if offline
        // Example: Suggest cleanup commands if storage is low
        
        return suggestions
    }
    
    private suspend fun enhanceWithAI(
        suggestions: List<SmartCommandSuggestion>,
        input: String,
        context: Context,
        userId: String
    ): List<SmartCommandSuggestion> {
        // For now, return suggestions as-is
        // Future enhancement: Use AI to analyze and re-rank suggestions
        return suggestions
    }
    
    private suspend fun generateParameterSuggestion(
        param: com.win11launcher.command.CommandParameter,
        commandName: String,
        userId: String
    ): ParameterSuggestion {
        // Generate smart parameter suggestions based on type and usage history
        val suggestedValue = when (param.type) {
            com.win11launcher.command.ParameterType.BOOLEAN -> "true"
            com.win11launcher.command.ParameterType.INTEGER -> "1"
            com.win11launcher.command.ParameterType.PATH -> "/sdcard/"
            com.win11launcher.command.ParameterType.STRING -> ""
            else -> ""
        }
        
        return ParameterSuggestion(
            parameterName = param.name,
            suggestedValue = suggestedValue,
            options = param.options,
            description = param.description,
            isRequired = param.required
        )
    }
    
    private fun generateSuggestionReason(
        scores: Map<String, Float>,
        command: CommandDefinition
    ): String {
        val topScore = scores.maxByOrNull { it.value }
        return when (topScore?.key) {
            "frequency" -> "Frequently used"
            "recency" -> "Recently used"
            "context" -> "Matches your context"
            "textSimilarity" -> "Best match for input"
            else -> "Suggested for you"
        }
    }
    
    private fun generateCompletionText(command: CommandDefinition, input: String): String {
        return if (input.isBlank()) {
            command.name
        } else {
            command.name
        }
    }
    
    private fun generateContextHint(
        command: CommandDefinition,
        scores: Map<String, Float>
    ): String? {
        val contextScore = scores["context"] ?: 0.0f
        val frequencyScore = scores["frequency"] ?: 0.0f
        
        return when {
            frequencyScore > 0.5f -> "You use this command often"
            contextScore > 0.3f -> "Relevant to your recent activity"
            else -> null
        }
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
}

// Data classes
data class SmartCommandSuggestion(
    val command: CommandDefinition,
    val completionText: String,
    val reason: String,
    val totalScore: Float,
    val scoreBreakdown: Map<String, Float>,
    val contextHint: String? = null,
    val estimatedExecutionTime: Long? = null,
    val requiresPermissions: List<String> = emptyList()
)

data class ParameterSuggestion(
    val parameterName: String,
    val suggestedValue: String,
    val options: List<String>,
    val description: String,
    val isRequired: Boolean,
    val validationPattern: String? = null
)