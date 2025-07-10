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
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class AICommandInterpreter @Inject constructor(
    private val aiService: AIService,
    private val commandRegistry: CommandRegistry,
    private val commandLineRepository: CommandLineRepository,
    private val aiMemoryManager: AIMemoryManager
) {
    
    companion object {
        private const val COMMAND_CONFIDENCE_THRESHOLD = 0.7f
        private const val MAX_COMMAND_SUGGESTIONS = 5
    }
    
    suspend fun interpretNaturalLanguage(
        input: String,
        context: Context,
        userId: String = "USER_DEFAULT",
        conversationId: String? = null
    ): CommandInterpretationResult = withContext(Dispatchers.IO) {
        
        // Get conversation context if available
        val conversationContext = conversationId?.let { 
            aiMemoryManager.getConversationContext(it, 10) 
        } ?: ""
        
        // Get relevant memories for context
        val relevantMemories = aiMemoryManager.retrieveRelevantMemories(userId, input, 5)
        val memoryContext = relevantMemories.joinToString("\n") { "- ${it.memoryText}" }
        
        // Get available commands for reference
        val availableCommands = commandRegistry.getAllCommands()
        val commandList = availableCommands.joinToString("\n") { cmd ->
            "${cmd.name}: ${cmd.description} (${cmd.usage})"
        }
        
        // Build interpretation prompt
        val interpretationPrompt = buildString {
            appendLine("You are an expert command line interpreter for an Android launcher system.")
            appendLine("Convert the user's natural language request into specific executable commands.")
            appendLine()
            
            if (conversationContext.isNotEmpty()) {
                appendLine("=== Recent Conversation ===")
                appendLine(conversationContext)
                appendLine()
            }
            
            if (memoryContext.isNotEmpty()) {
                appendLine("=== Relevant User Context ===")
                appendLine(memoryContext)
                appendLine()
            }
            
            appendLine("=== Available Commands ===")
            appendLine(commandList.take(2000)) // Limit to avoid token overflow
            appendLine()
            
            appendLine("=== User Request ===")
            appendLine("\"$input\"")
            appendLine()
            
            appendLine("Analyze this request and provide:")
            appendLine("1. The user's intent")
            appendLine("2. Suggested command(s) to execute")
            appendLine("3. Any parameters or arguments needed")
            appendLine("4. Confidence level (0.0 to 1.0)")
            appendLine("5. Alternative commands if uncertain")
            appendLine()
            
            appendLine("Respond in this JSON format:")
            appendLine("""
                {
                    "intent": "Brief description of what user wants to do",
                    "confidence": 0.85,
                    "primary_commands": [
                        {
                            "command": "command_name",
                            "parameters": {"param1": "value1"},
                            "arguments": ["arg1", "arg2"],
                            "explanation": "Why this command fits the intent"
                        }
                    ],
                    "alternative_commands": [
                        {
                            "command": "alt_command",
                            "parameters": {},
                            "arguments": [],
                            "explanation": "Alternative approach"
                        }
                    ],
                    "requires_clarification": false,
                    "clarification_questions": []
                }
            """.trimIndent())
        }
        
        try {
            val aiResponse = aiService.generateResponse(interpretationPrompt)
            if (aiResponse.success) {
                parseInterpretationResponse(aiResponse.response, input, availableCommands)
            } else {
                CommandInterpretationResult(
                    intent = "AI service error",
                    confidence = 0.0f,
                    primaryCommands = fallbackCommandMatching(input, availableCommands),
                    alternativeCommands = emptyList(),
                    requiresClarification = true,
                    clarificationQuestions = listOf("AI service is unavailable. Please try again later."),
                    rawResponse = aiResponse.error ?: "Unknown AI error"
                )
            }
        } catch (e: Exception) {
            // Fallback to simple pattern matching if AI fails
            CommandInterpretationResult(
                intent = "Parse user command request",
                confidence = 0.3f,
                primaryCommands = fallbackCommandMatching(input, availableCommands),
                alternativeCommands = emptyList(),
                requiresClarification = true,
                clarificationQuestions = listOf("Could you please rephrase your request more specifically?"),
                rawResponse = "AI interpretation failed: ${e.message}"
            )
        }
    }
    
    suspend fun generateCommandSequence(
        intent: String,
        context: Context,
        userId: String = "USER_DEFAULT"
    ): List<String> = withContext(Dispatchers.IO) {
        
        val relevantMemories = aiMemoryManager.retrieveRelevantMemories(userId, intent, 3)
        val memoryContext = relevantMemories.joinToString("\n") { "- ${it.memoryText}" }
        
        val sequencePrompt = buildString {
            appendLine("Generate a sequence of commands to accomplish this goal:")
            appendLine("Intent: $intent")
            appendLine()
            
            if (memoryContext.isNotEmpty()) {
                appendLine("=== User Context ===")
                appendLine(memoryContext)
                appendLine()
            }
            
            appendLine("Provide a step-by-step command sequence as a JSON array:")
            appendLine("Example: [\"command1 arg1\", \"command2 --param=value\", \"command3\"]")
            appendLine()
            appendLine("Focus on practical, executable commands that work together to achieve the goal.")
        }
        
        try {
            val aiResponse = aiService.generateResponse(sequencePrompt)
            if (aiResponse.success) {
                parseCommandSequence(aiResponse.response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun analyzeSystemState(
        context: Context,
        userId: String = "USER_DEFAULT"
    ): SystemAnalysis = withContext(Dispatchers.IO) {
        
        // Get recent command history
        val recentCommands = commandLineRepository.getRecentCommands(20)
        val commandHistory = recentCommands.joinToString("\n") { "- $it" }
        
        // Get system insights from memories
        val systemMemories = aiMemoryManager.getMemoryByCategory(userId, "SYSTEM")
        val systemContext = systemMemories.take(5).joinToString("\n") { "- ${it.memoryText}" }
        
        val analysisPrompt = buildString {
            appendLine("Analyze the current system state and provide insights:")
            appendLine()
            
            appendLine("=== Recent Command Usage ===")
            appendLine(commandHistory)
            appendLine()
            
            if (systemContext.isNotEmpty()) {
                appendLine("=== System Context ===")
                appendLine(systemContext)
                appendLine()
            }
            
            appendLine("Provide analysis in this JSON format:")
            appendLine("""
                {
                    "overall_health": "GOOD|FAIR|POOR",
                    "key_insights": ["insight1", "insight2"],
                    "recommendations": [
                        {
                            "priority": "HIGH|MEDIUM|LOW",
                            "action": "Specific recommendation",
                            "command": "suggested command if applicable"
                        }
                    ],
                    "usage_patterns": ["pattern1", "pattern2"],
                    "potential_issues": ["issue1", "issue2"]
                }
            """.trimIndent())
        }
        
        try {
            val aiResponse = aiService.generateResponse(analysisPrompt)
            if (aiResponse.success) {
                parseSystemAnalysis(aiResponse.response)
            } else {
                SystemAnalysis(
                    overallHealth = "ERROR",
                    keyInsights = listOf("AI analysis unavailable: ${aiResponse.error}"),
                    recommendations = emptyList(),
                    usagePatterns = emptyList(),
                    potentialIssues = listOf("AI service error")
                )
            }
        } catch (e: Exception) {
            SystemAnalysis(
                overallHealth = "UNKNOWN",
                keyInsights = listOf("Unable to analyze system state"),
                recommendations = emptyList(),
                usagePatterns = emptyList(),
                potentialIssues = listOf("AI analysis unavailable")
            )
        }
    }
    
    suspend fun generateOptimizationSuggestions(
        context: Context,
        category: CommandCategory?,
        userId: String = "USER_DEFAULT"
    ): List<OptimizationSuggestion> = withContext(Dispatchers.IO) {
        
        val categoryFilter = category?.name ?: "ALL"
        val relevantMemories = if (category != null) {
            aiMemoryManager.getMemoryByCategory(userId, category.name)
        } else {
            aiMemoryManager.retrieveRelevantMemories(userId, "optimization performance", 10)
        }
        
        val memoryContext = relevantMemories.take(5).joinToString("\n") { "- ${it.memoryText}" }
        
        val optimizationPrompt = buildString {
            appendLine("Generate optimization suggestions for category: $categoryFilter")
            appendLine()
            
            if (memoryContext.isNotEmpty()) {
                appendLine("=== Relevant Context ===")
                appendLine(memoryContext)
                appendLine()
            }
            
            appendLine("Provide optimization suggestions in this JSON format:")
            appendLine("""
                {
                    "suggestions": [
                        {
                            "title": "Optimization title",
                            "description": "Detailed description",
                            "category": "PERFORMANCE|BATTERY|STORAGE|NETWORK|USER_EXPERIENCE",
                            "impact": "HIGH|MEDIUM|LOW",
                            "difficulty": "EASY|MEDIUM|HARD",
                            "commands": ["command1", "command2"],
                            "estimated_improvement": "Quantified benefit"
                        }
                    ]
                }
            """.trimIndent())
        }
        
        try {
            val aiResponse = aiService.generateResponse(optimizationPrompt)
            if (aiResponse.success) {
                parseOptimizationSuggestions(aiResponse.response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseInterpretationResponse(
        response: String,
        originalInput: String,
        availableCommands: List<CommandDefinition>
    ): CommandInterpretationResult {
        return try {
            val json = JSONObject(extractJsonFromResponse(response))
            
            val intent = json.optString("intent", "Unknown intent")
            val confidence = json.optDouble("confidence", 0.5).toFloat()
            
            val primaryCommands = parseCommandSuggestions(json.optJSONArray("primary_commands"))
            val alternativeCommands = parseCommandSuggestions(json.optJSONArray("alternative_commands"))
            
            val requiresClarification = json.optBoolean("requires_clarification", false)
            val clarificationQuestions = parseStringArray(json.optJSONArray("clarification_questions"))
            
            CommandInterpretationResult(
                intent = intent,
                confidence = confidence,
                primaryCommands = primaryCommands,
                alternativeCommands = alternativeCommands,
                requiresClarification = requiresClarification,
                clarificationQuestions = clarificationQuestions,
                rawResponse = response
            )
        } catch (e: Exception) {
            CommandInterpretationResult(
                intent = "Failed to parse AI response",
                confidence = 0.2f,
                primaryCommands = fallbackCommandMatching(originalInput, availableCommands),
                alternativeCommands = emptyList(),
                requiresClarification = true,
                clarificationQuestions = listOf("I couldn't understand your request. Could you try rephrasing it?"),
                rawResponse = response
            )
        }
    }
    
    private fun parseCommandSuggestions(jsonArray: JSONArray?): List<CommandSuggestion> {
        if (jsonArray == null) return emptyList()
        
        val suggestions = mutableListOf<CommandSuggestion>()
        for (i in 0 until jsonArray.length()) {
            val cmdObj = jsonArray.getJSONObject(i)
            
            val command = cmdObj.optString("command")
            val parameters = parseParameters(cmdObj.optJSONObject("parameters"))
            val arguments = parseStringArray(cmdObj.optJSONArray("arguments"))
            val explanation = cmdObj.optString("explanation")
            
            suggestions.add(CommandSuggestion(command, parameters, arguments, explanation))
        }
        
        return suggestions
    }
    
    private fun parseParameters(jsonObject: JSONObject?): Map<String, String> {
        if (jsonObject == null) return emptyMap()
        
        val params = mutableMapOf<String, String>()
        jsonObject.keys().forEach { key ->
            params[key] = jsonObject.optString(key)
        }
        return params
    }
    
    private fun parseStringArray(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        
        val strings = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            strings.add(jsonArray.optString(i))
        }
        return strings
    }
    
    private fun parseCommandSequence(response: String): List<String> {
        return try {
            val jsonArray = JSONArray(extractJsonFromResponse(response))
            parseStringArray(jsonArray)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseSystemAnalysis(response: String): SystemAnalysis {
        return try {
            val json = JSONObject(extractJsonFromResponse(response))
            
            val overallHealth = json.optString("overall_health", "UNKNOWN")
            val keyInsights = parseStringArray(json.optJSONArray("key_insights"))
            val usagePatterns = parseStringArray(json.optJSONArray("usage_patterns"))
            val potentialIssues = parseStringArray(json.optJSONArray("potential_issues"))
            
            val recommendations = mutableListOf<SystemRecommendation>()
            val recArray = json.optJSONArray("recommendations")
            if (recArray != null) {
                for (i in 0 until recArray.length()) {
                    val recObj = recArray.getJSONObject(i)
                    recommendations.add(
                        SystemRecommendation(
                            priority = recObj.optString("priority", "MEDIUM"),
                            action = recObj.optString("action"),
                            command = recObj.optString("command").takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }
            
            SystemAnalysis(overallHealth, keyInsights, recommendations, usagePatterns, potentialIssues)
        } catch (e: Exception) {
            SystemAnalysis(
                overallHealth = "ERROR",
                keyInsights = listOf("Failed to parse system analysis"),
                recommendations = emptyList(),
                usagePatterns = emptyList(),
                potentialIssues = listOf("Analysis parsing failed")
            )
        }
    }
    
    private fun parseOptimizationSuggestions(response: String): List<OptimizationSuggestion> {
        return try {
            val json = JSONObject(extractJsonFromResponse(response))
            val suggestions = mutableListOf<OptimizationSuggestion>()
            
            val sugArray = json.optJSONArray("suggestions")
            if (sugArray != null) {
                for (i in 0 until sugArray.length()) {
                    val sugObj = sugArray.getJSONObject(i)
                    suggestions.add(
                        OptimizationSuggestion(
                            title = sugObj.optString("title"),
                            description = sugObj.optString("description"),
                            category = sugObj.optString("category"),
                            impact = sugObj.optString("impact"),
                            difficulty = sugObj.optString("difficulty"),
                            commands = parseStringArray(sugObj.optJSONArray("commands")),
                            estimatedImprovement = sugObj.optString("estimated_improvement")
                        )
                    )
                }
            }
            
            suggestions
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun extractJsonFromResponse(response: String): String {
        // Extract JSON from AI response that might have additional text
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        
        return if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd + 1)
        } else {
            response
        }
    }
    
    private fun fallbackCommandMatching(input: String, availableCommands: List<CommandDefinition>): List<CommandSuggestion> {
        val lowercaseInput = input.lowercase()
        val matches = availableCommands.filter { cmd ->
            lowercaseInput.contains(cmd.name) || 
            cmd.aliases.any { lowercaseInput.contains(it) } ||
            lowercaseInput.contains(cmd.description.lowercase())
        }
        
        return matches.take(3).map { cmd ->
            CommandSuggestion(
                command = cmd.name,
                parameters = emptyMap(),
                arguments = emptyList(),
                explanation = "Pattern match: ${cmd.description}"
            )
        }
    }
}

// Data classes for AI command interpretation
data class CommandInterpretationResult(
    val intent: String,
    val confidence: Float,
    val primaryCommands: List<CommandSuggestion>,
    val alternativeCommands: List<CommandSuggestion>,
    val requiresClarification: Boolean,
    val clarificationQuestions: List<String>,
    val rawResponse: String
)

data class CommandSuggestion(
    val command: String,
    val parameters: Map<String, String>,
    val arguments: List<String>,
    val explanation: String
)

data class SystemAnalysis(
    val overallHealth: String,
    val keyInsights: List<String>,
    val recommendations: List<SystemRecommendation>,
    val usagePatterns: List<String>,
    val potentialIssues: List<String>
)

data class SystemRecommendation(
    val priority: String,
    val action: String,
    val command: String?
)

data class OptimizationSuggestion(
    val title: String,
    val description: String,
    val category: String,
    val impact: String,
    val difficulty: String,
    val commands: List<String>,
    val estimatedImprovement: String
)