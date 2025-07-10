package com.win11launcher.command.commands

import com.win11launcher.command.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AICommandProvider @Inject constructor(
    private val askExecutor: AskCommandExecutor,
    private val interpretExecutor: InterpretCommandExecutor,
    private val analyzeExecutor: AnalyzeCommandExecutor,
    private val memoryExecutor: AIMemoryCommandExecutor
) {
    
    fun getAskCommand() = CommandDefinition(
        name = "ask",
        category = CommandCategory.AI,
        description = "Ask AI a question with memory and context",
        usage = "ask <question> [--memory] [--context=conversation_id]",
        examples = listOf(
            "ask How can I improve battery life?",
            "ask --memory What did I ask about earlier?",
            "ask --context=conv123 Continue our previous discussion"
        ),
        parameters = listOf(
            CommandParameter(
                name = "question",
                type = ParameterType.STRING,
                description = "Question to ask the AI",
                required = true
            ),
            CommandParameter(
                name = "memory",
                type = ParameterType.BOOLEAN,
                description = "Include relevant memories in context",
                defaultValue = "true"
            ),
            CommandParameter(
                name = "context",
                type = ParameterType.STRING,
                description = "Conversation ID for context",
                required = false
            )
        ),
        aliases = listOf("ai"),
        executor = askExecutor
    )
    
    fun getInterpretCommand() = CommandDefinition(
        name = "interpret",
        category = CommandCategory.AI,
        description = "Convert natural language to executable commands",
        usage = "interpret <natural_language_request>",
        examples = listOf(
            "interpret Show me the battery status",
            "interpret Find all apps using lots of memory",
            "interpret Check network connection and speed"
        ),
        parameters = listOf(
            CommandParameter(
                name = "request",
                type = ParameterType.STRING,
                description = "Natural language request",
                required = true
            )
        ),
        aliases = listOf("translate", "parse"),
        executor = interpretExecutor
    )
    
    fun getAnalyzeCommand() = CommandDefinition(
        name = "analyze",
        category = CommandCategory.AI,
        description = "AI-powered system analysis and recommendations",
        usage = "analyze [--category=system|battery|network|apps] [--deep]",
        examples = listOf(
            "analyze",
            "analyze --category=battery",
            "analyze --category=system --deep"
        ),
        parameters = listOf(
            CommandParameter(
                name = "category",
                type = ParameterType.ENUM,
                description = "Category to analyze",
                options = listOf("system", "battery", "network", "apps", "performance"),
                required = false
            ),
            CommandParameter(
                name = "deep",
                type = ParameterType.BOOLEAN,
                description = "Perform deep analysis with recommendations",
                defaultValue = "false"
            )
        ),
        aliases = listOf("diagnose"),
        executor = analyzeExecutor
    )
    
    fun getMemoryCommand() = CommandDefinition(
        name = "memory",
        category = CommandCategory.AI,
        description = "Manage AI memory and conversation history",
        usage = "memory <action> [--type=short|long|reflection] [--query=<search>]",
        examples = listOf(
            "memory show",
            "memory search --query=battery",
            "memory stats",
            "memory cleanup",
            "memory reflect"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Memory action",
                options = listOf("show", "search", "stats", "cleanup", "reflect"),
                required = true
            ),
            CommandParameter(
                name = "type",
                type = ParameterType.ENUM,
                description = "Memory type filter",
                options = listOf("short", "long", "reflection"),
                required = false
            ),
            CommandParameter(
                name = "query",
                type = ParameterType.STRING,
                description = "Search query for memories",
                required = false
            )
        ),
        aliases = listOf("mem", "recall"),
        executor = memoryExecutor
    )
    
    // Simplified versions of other commands that don't require heavy AI processing
    fun getOptimizeCommand() = CommandDefinition(
        name = "optimize",
        category = CommandCategory.AI,
        description = "Get AI optimization suggestions (simplified)",
        usage = "optimize [--category=performance|battery|storage]",
        examples = listOf(
            "optimize",
            "optimize --category=battery"
        ),
        parameters = listOf(
            CommandParameter(
                name = "category",
                type = ParameterType.ENUM,
                description = "Optimization category",
                options = listOf("performance", "battery", "storage", "network"),
                required = false
            )
        ),
        aliases = listOf("improve", "tune"),
        executor = object : CommandExecutor {
            override suspend fun execute(
                context: android.content.Context,
                parameters: Map<String, String>,
                arguments: List<String>
            ): CommandResult {
                val category = parameters["category"] ?: "general"
                
                val suggestions = when (category.lowercase()) {
                    "battery" -> listOf(
                        "• Use 'power battery' to check battery status",
                        "• Run 'apps' to find power-hungry applications",
                        "• Consider using 'screen --brightness=low' to save power",
                        "• Use 'network wifi --power-save' to reduce WiFi power usage"
                    )
                    "performance" -> listOf(
                        "• Run 'memory' to check system memory usage",
                        "• Use 'apps --sort=memory' to find memory-intensive apps",
                        "• Clear cache with app management commands",
                        "• Check running processes with system monitoring"
                    )
                    "storage" -> listOf(
                        "• Use 'storage' command to check disk usage",
                        "• Clean temporary files and cache",
                        "• Review and uninstall unused applications",
                        "• Move files to external storage if needed"
                    )
                    "network" -> listOf(
                        "• Run 'network' to check connection status",
                        "• Use 'ping' to test network connectivity",
                        "• Check WiFi signal strength and optimization",
                        "• Review network usage patterns"
                    )
                    else -> listOf(
                        "• Use 'system' command for overall system health",
                        "• Run 'analyze' for AI-powered system analysis",
                        "• Check 'memory stats' for AI learning progress",
                        "• Use specific category: --category=battery|performance|storage|network"
                    )
                }
                
                val output = buildString {
                    appendLine("=== $category Optimization Suggestions ===")
                    appendLine()
                    suggestions.forEach { suggestion ->
                        appendLine(suggestion)
                    }
                    appendLine()
                    appendLine("💡 Use 'ask' command for personalized AI recommendations!")
                }
                
                return CommandResult(
                    success = true,
                    output = output,
                    executionTimeMs = 0
                )
            }
        }
    )
    
    fun getSuggestCommand() = CommandDefinition(
        name = "suggest",
        category = CommandCategory.AI,
        description = "Get intelligent command suggestions",
        usage = "suggest [--context=<context>] [--category=<category>]",
        examples = listOf(
            "suggest",
            "suggest --context=battery_low",
            "suggest --category=system"
        ),
        parameters = listOf(
            CommandParameter(
                name = "context",
                type = ParameterType.STRING,
                description = "Context for suggestions",
                required = false
            ),
            CommandParameter(
                name = "category",
                type = ParameterType.ENUM,
                description = "Command category filter",
                options = CommandCategory.values().map { it.name.lowercase() },
                required = false
            )
        ),
        aliases = listOf("hints", "recommendations"),
        executor = object : CommandExecutor {
            override suspend fun execute(
                context: android.content.Context,
                parameters: Map<String, String>,
                arguments: List<String>
            ): CommandResult {
                val contextHint = parameters["context"]
                val category = parameters["category"]
                
                val suggestions = when {
                    contextHint?.contains("battery") == true -> listOf(
                        "power battery - Check battery status and health",
                        "apps --sort=battery - Find battery-draining apps",
                        "screen --brightness=low - Reduce screen brightness",
                        "optimize --category=battery - Get battery optimization tips"
                    )
                    contextHint?.contains("memory") == true -> listOf(
                        "system memory - Check memory usage",
                        "apps --sort=memory - Find memory-intensive apps",
                        "memory stats - View AI memory statistics",
                        "analyze --category=performance - Get performance analysis"
                    )
                    category == "ai" -> listOf(
                        "ask <question> - Start AI conversation",
                        "memory show - View AI memory status",
                        "interpret <request> - Convert natural language to commands",
                        "analyze - Get AI system analysis"
                    )
                    else -> listOf(
                        "ask <question> - Ask AI for help",
                        "system - Check overall system status",
                        "apps - Manage applications",
                        "network - Check network status",
                        "commands - List all available commands"
                    )
                }
                
                val output = buildString {
                    appendLine("=== Command Suggestions ===")
                    if (contextHint != null) {
                        appendLine("Context: $contextHint")
                    }
                    if (category != null) {
                        appendLine("Category: $category")
                    }
                    appendLine()
                    
                    suggestions.forEachIndexed { index, suggestion ->
                        appendLine("${index + 1}. $suggestion")
                    }
                    
                    appendLine()
                    appendLine("💡 Use 'commands --category=<category>' to see all commands in a category")
                }
                
                return CommandResult(
                    success = true,
                    output = output,
                    executionTimeMs = 0
                )
            }
        }
    )
    
    fun getScriptCommand() = CommandDefinition(
        name = "script",
        category = CommandCategory.AI,
        description = "AI-generated automation scripts (simplified)",
        usage = "script <action> [--name=<name>] [--goal=<description>]",
        examples = listOf(
            "script list",
            "script generate --goal=\"Monitor battery health\"",
            "script examples"
        ),
        parameters = listOf(
            CommandParameter(
                name = "action",
                type = ParameterType.ENUM,
                description = "Script action",
                options = listOf("list", "generate", "examples", "help"),
                required = true
            ),
            CommandParameter(
                name = "name",
                type = ParameterType.STRING,
                description = "Script name",
                required = false
            ),
            CommandParameter(
                name = "goal",
                type = ParameterType.STRING,
                description = "Goal description for script generation",
                required = false
            )
        ),
        aliases = listOf("automation", "auto"),
        executor = object : CommandExecutor {
            override suspend fun execute(
                context: android.content.Context,
                parameters: Map<String, String>,
                arguments: List<String>
            ): CommandResult {
                val action = parameters["action"] ?: "help"
                
                val output = when (action) {
                    "list" -> "=== Available Scripts ===\n\nNo custom scripts created yet.\n\n💡 Use 'script generate --goal=\"<description>\"' to create automation scripts"
                    "examples" -> buildString {
                        appendLine("=== Script Examples ===")
                        appendLine()
                        appendLine("Battery Monitor:")
                        appendLine("  power battery && if [ battery < 20% ]; then echo 'Low battery!'; fi")
                        appendLine()
                        appendLine("System Health Check:")
                        appendLine("  system && memory && storage && network")
                        appendLine()
                        appendLine("App Cleanup:")
                        appendLine("  apps --sort=memory && optimize --category=performance")
                        appendLine()
                        appendLine("💡 Use 'ask' command to generate custom scripts with AI!")
                    }
                    "generate" -> {
                        val goal = parameters["goal"]
                        if (goal.isNullOrEmpty()) {
                            "Error: Please provide a goal with --goal=\"description\"\n\nExample: script generate --goal=\"Monitor system performance\""
                        } else {
                            buildString {
                                appendLine("=== AI Script Generation ===")
                                appendLine("Goal: $goal")
                                appendLine()
                                appendLine("Suggested script commands:")
                                when {
                                    goal.contains("battery", true) -> {
                                        appendLine("  power battery")
                                        appendLine("  apps --sort=battery") 
                                        appendLine("  optimize --category=battery")
                                    }
                                    goal.contains("memory", true) || goal.contains("performance", true) -> {
                                        appendLine("  system memory")
                                        appendLine("  apps --sort=memory")
                                        appendLine("  analyze --category=performance")
                                    }
                                    goal.contains("network", true) -> {
                                        appendLine("  network")
                                        appendLine("  ping 8.8.8.8")
                                        appendLine("  wifi --status")
                                    }
                                    else -> {
                                        appendLine("  system")
                                        appendLine("  analyze")
                                        appendLine("  optimize")
                                    }
                                }
                                appendLine()
                                appendLine("💡 Use 'ask' for more sophisticated AI-generated scripts!")
                            }
                        }
                    }
                    else -> buildString {
                        appendLine("=== AI Script System ===")
                        appendLine()
                        appendLine("Available actions:")
                        appendLine("  list     - Show available scripts")
                        appendLine("  generate - Generate script for a goal")
                        appendLine("  examples - Show script examples")
                        appendLine()
                        appendLine("💡 For advanced script generation, use: ask \"Create a script that...\"")
                    }
                }
                
                return CommandResult(
                    success = true,
                    output = output,
                    executionTimeMs = 0
                )
            }
        }
    )
}