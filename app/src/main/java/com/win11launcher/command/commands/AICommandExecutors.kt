package com.win11launcher.command.commands

import android.content.Context
import com.win11launcher.command.*
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import com.win11launcher.services.AICommandInterpreter
import javax.inject.Inject
import javax.inject.Singleton

// Actual AI Command Executors with proper DI integration

@Singleton
class AskCommandExecutor @Inject constructor(
    private val aiService: AIService,
    private val aiMemoryManager: AIMemoryManager
) : CommandExecutor {
    
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val question = parameters["question"] ?: arguments.joinToString(" ")
        val useMemory = parameters["memory"]?.toBoolean() ?: true
        val conversationId = parameters["context"] ?: "default_conversation"
        
        if (question.isEmpty()) {
            return CommandResult(
                success = false,
                output = "Error: Please provide a question to ask the AI",
                executionTimeMs = 0
            )
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            // Check if AI service is ready
            val initResult = aiService.initializeModel()
            if (!initResult.success) {
                return CommandResult(
                    success = false,
                    output = "AI service not available: ${initResult.error ?: "Unknown error"}\n\nTo use AI features, please ensure the Gemma 3N model is available.",
                    executionTimeMs = 0
                )
            }
            
            // Save user message to memory
            aiMemoryManager.saveToShortTermMemory(
                conversationId = conversationId,
                sender = "user",
                content = question,
                messageType = "QUESTION"
            )
            
            // Build context-aware prompt
            val prompt = if (useMemory) {
                val contextHistory = aiMemoryManager.getConversationContext(conversationId, 10)
                val relevantMemories = aiMemoryManager.retrieveRelevantMemories("USER_DEFAULT", question, 3)
                val memoryContext = relevantMemories.joinToString("\n") { "- ${it.memoryText}" }
                
                buildString {
                    if (contextHistory.isNotEmpty()) {
                        appendLine("=== Recent Conversation ===")
                        appendLine(contextHistory)
                        appendLine()
                    }
                    
                    if (memoryContext.isNotEmpty()) {
                        appendLine("=== Relevant Context ===")
                        appendLine(memoryContext)
                        appendLine()
                    }
                    
                    appendLine("=== Current Question ===")
                    append(question)
                }
            } else {
                question
            }
            
            val aiResponse = aiService.generateResponse(prompt)
            val endTime = System.currentTimeMillis()
            
            if (aiResponse.success) {
                // Save AI response to memory
                aiMemoryManager.saveToShortTermMemory(
                    conversationId = conversationId,
                    sender = "ai",
                    content = aiResponse.response,
                    messageType = "RESPONSE"
                )
                
                CommandResult(
                    success = true,
                    output = aiResponse.response,
                    executionTimeMs = endTime - startTime
                )
            } else {
                CommandResult(
                    success = false,
                    output = "AI Error: ${aiResponse.error ?: "Failed to generate response"}",
                    executionTimeMs = endTime - startTime
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

@Singleton
class InterpretCommandExecutor @Inject constructor(
    private val aiCommandInterpreter: AICommandInterpreter
) : CommandExecutor {
    
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val request = parameters["request"] ?: arguments.joinToString(" ")
        
        if (request.isEmpty()) {
            return CommandResult(
                success = false,
                output = "Error: Please provide a natural language request to interpret",
                executionTimeMs = 0
            )
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            val result = aiCommandInterpreter.interpretNaturalLanguage(request, context)
            
            val output = buildString {
                appendLine("=== Intent Analysis ===")
                appendLine("Intent: ${result.intent}")
                appendLine("Confidence: ${(result.confidence * 100).toInt()}%")
                appendLine()
                
                if (result.primaryCommands.isNotEmpty()) {
                    appendLine("=== Suggested Commands ===")
                    result.primaryCommands.forEachIndexed { index, cmd ->
                        appendLine("${index + 1}. ${cmd.command}")
                        if (cmd.parameters.isNotEmpty()) {
                            appendLine("   Parameters: ${cmd.parameters}")
                        }
                        if (cmd.arguments.isNotEmpty()) {
                            appendLine("   Arguments: ${cmd.arguments}")
                        }
                        appendLine("   Explanation: ${cmd.explanation}")
                        appendLine()
                    }
                }
                
                if (result.alternativeCommands.isNotEmpty()) {
                    appendLine("=== Alternative Commands ===")
                    result.alternativeCommands.forEach { cmd ->
                        appendLine("- ${cmd.command}: ${cmd.explanation}")
                    }
                    appendLine()
                }
                
                if (result.requiresClarification) {
                    appendLine("=== Clarification Needed ===")
                    result.clarificationQuestions.forEach { question ->
                        appendLine("- $question")
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error interpreting request: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

@Singleton
class AnalyzeCommandExecutor @Inject constructor(
    private val aiCommandInterpreter: AICommandInterpreter
) : CommandExecutor {
    
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val category = parameters["category"]
        val deep = parameters["deep"]?.toBoolean() ?: false
        
        return try {
            val startTime = System.currentTimeMillis()
            val analysis = aiCommandInterpreter.analyzeSystemState(context)
            
            val output = buildString {
                appendLine("=== System Analysis ===")
                appendLine("Overall Health: ${analysis.overallHealth}")
                appendLine()
                
                if (analysis.keyInsights.isNotEmpty()) {
                    appendLine("=== Key Insights ===")
                    analysis.keyInsights.forEach { insight ->
                        appendLine("• $insight")
                    }
                    appendLine()
                }
                
                if (analysis.recommendations.isNotEmpty()) {
                    appendLine("=== Recommendations ===")
                    analysis.recommendations.forEach { rec ->
                        appendLine("${rec.priority}: ${rec.action}")
                        rec.command?.let { cmd ->
                            appendLine("  Command: $cmd")
                        }
                        appendLine()
                    }
                }
                
                if (analysis.usagePatterns.isNotEmpty()) {
                    appendLine("=== Usage Patterns ===")
                    analysis.usagePatterns.forEach { pattern ->
                        appendLine("• $pattern")
                    }
                    appendLine()
                }
                
                if (analysis.potentialIssues.isNotEmpty()) {
                    appendLine("=== Potential Issues ===")
                    analysis.potentialIssues.forEach { issue ->
                        appendLine("⚠ $issue")
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error performing analysis: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

@Singleton
class AIMemoryCommandExecutor @Inject constructor(
    private val aiMemoryManager: AIMemoryManager
) : CommandExecutor {
    
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val action = parameters["action"] ?: arguments.firstOrNull() ?: "show"
        val type = parameters["type"]
        val query = parameters["query"]
        
        return try {
            val startTime = System.currentTimeMillis()
            
            val result = when (action) {
                "show" -> showMemory(type, query)
                "search" -> searchMemory(query ?: "")
                "stats" -> showMemoryStats()
                "cleanup" -> performMemoryCleanup()
                "reflect" -> generateReflection()
                else -> CommandResult(
                    success = false,
                    output = "Unknown memory action: $action\nAvailable actions: show, search, stats, cleanup, reflect",
                    executionTimeMs = 0
                )
            }
            
            result.copy(executionTimeMs = System.currentTimeMillis() - startTime)
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error managing memory: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private suspend fun showMemory(type: String?, query: String?): CommandResult {
        val stats = aiMemoryManager.getMemoryStatistics("USER_DEFAULT")
        
        val output = buildString {
            appendLine("=== AI Memory Status ===")
            appendLine("Long-term memories: ${stats.longTermMemoryCount}")
            appendLine("Reflections: ${stats.reflectionCount}")
            appendLine("Average importance: ${(stats.averageImportance * 100).toInt()}%")
            appendLine()
            
            appendLine("=== Memory Types ===")
            stats.memoryTypeDistribution.forEach { stat ->
                appendLine("${stat.memory_type}: ${stat.count}")
            }
            
            if (stats.longTermMemoryCount == 0) {
                appendLine()
                appendLine("💡 Start a conversation with 'ask' to build AI memory!")
            }
        }
        
        return CommandResult(success = true, output = output, executionTimeMs = 0)
    }
    
    private suspend fun searchMemory(query: String): CommandResult {
        if (query.isEmpty()) {
            return CommandResult(
                success = false,
                output = "Error: Please provide a search query using --query=<search_term>",
                executionTimeMs = 0
            )
        }
        
        val memories = aiMemoryManager.retrieveRelevantMemories("USER_DEFAULT", query, 10)
        
        val output = buildString {
            appendLine("=== Memory Search Results ===")
            appendLine("Query: $query")
            appendLine("Found: ${memories.size} memories")
            appendLine()
            
            if (memories.isEmpty()) {
                appendLine("No memories found matching '$query'")
                appendLine("💡 Try different search terms or build more conversation history")
            } else {
                memories.forEachIndexed { index, memory ->
                    appendLine("${index + 1}. ${memory.memoryText}")
                    appendLine("   Type: ${memory.memoryType} | Importance: ${(memory.importanceScore * 100).toInt()}%")
                    appendLine("   Created: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(memory.createdAt))}")
                    appendLine()
                }
            }
        }
        
        return CommandResult(success = true, output = output, executionTimeMs = 0)
    }
    
    private suspend fun showMemoryStats(): CommandResult {
        val stats = aiMemoryManager.getMemoryStatistics("USER_DEFAULT")
        
        val output = buildString {
            appendLine("=== Detailed Memory Statistics ===")
            appendLine("Total long-term memories: ${stats.longTermMemoryCount}")
            appendLine("Total reflections: ${stats.reflectionCount}")
            appendLine("Average memory importance: ${(stats.averageImportance * 100).toInt()}%")
            appendLine()
            
            appendLine("=== Memory Type Distribution ===")
            if (stats.memoryTypeDistribution.isEmpty()) {
                appendLine("No memory types recorded yet")
            } else {
                stats.memoryTypeDistribution.forEach { stat ->
                    appendLine("${stat.memory_type}: ${stat.count}")
                }
            }
            appendLine()
            
            appendLine("=== Reflection Type Distribution ===")
            if (stats.reflectionTypeDistribution.isEmpty()) {
                appendLine("No reflections generated yet")
            } else {
                stats.reflectionTypeDistribution.forEach { stat ->
                    appendLine("${stat.reflection_type}: ${stat.count}")
                }
            }
            
            appendLine()
            appendLine("💡 Use 'ask' command to generate conversations and build memory")
        }
        
        return CommandResult(success = true, output = output, executionTimeMs = 0)
    }
    
    private suspend fun performMemoryCleanup(): CommandResult {
        aiMemoryManager.performMemoryMaintenance("USER_DEFAULT")
        
        return CommandResult(
            success = true,
            output = "✅ Memory cleanup completed!\n\n• Old and low-importance memories cleaned up\n• Memory decay applied\n• Database optimized",
            executionTimeMs = 0
        )
    }
    
    private suspend fun generateReflection(): CommandResult {
        val reflectionId = aiMemoryManager.generateReflection("default_conversation", "USER_DEFAULT")
        
        return CommandResult(
            success = true,
            output = "✅ Generated new reflection!\n\nID: $reflectionId\n\n💡 Use 'memory search --query=pattern' to view recent insights.",
            executionTimeMs = 0
        )
    }
}