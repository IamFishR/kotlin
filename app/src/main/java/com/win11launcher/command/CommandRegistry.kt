package com.win11launcher.command

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRegistry @Inject constructor() {
    
    private val _commands = mutableMapOf<String, CommandDefinition>()
    private val _commandsByCategory = mutableMapOf<CommandCategory, MutableList<CommandDefinition>>()
    private val _aliases = mutableMapOf<String, String>()
    
    private val _registeredCommands = MutableStateFlow<List<CommandDefinition>>(emptyList())
    val registeredCommands: Flow<List<CommandDefinition>> = _registeredCommands.asStateFlow()
    
    init {
        // Initialize with built-in commands
        registerBuiltInCommands()
    }
    
    fun registerCommand(command: CommandDefinition) {
        _commands[command.name] = command
        
        // Register aliases
        command.aliases.forEach { alias ->
            _aliases[alias] = command.name
        }
        
        // Add to category mapping
        _commandsByCategory.getOrPut(command.category) { mutableListOf() }.add(command)
        
        // Update flow
        _registeredCommands.value = _commands.values.toList()
    }
    
    fun getCommand(name: String): CommandDefinition? {
        val commandName = _aliases[name] ?: name
        return _commands[commandName]
    }
    
    fun getAllCommands(): List<CommandDefinition> {
        return _commands.values.toList()
    }
    
    fun getCommandsByCategory(category: CommandCategory): List<CommandDefinition> {
        return _commandsByCategory[category] ?: emptyList()
    }
    
    fun searchCommands(query: String): List<CommandSuggestion> {
        val suggestions = mutableListOf<CommandSuggestion>()
        val lowerQuery = query.lowercase()
        
        _commands.values.forEach { command ->
            var score = 0
            
            // Exact name match
            if (command.name.lowercase() == lowerQuery) {
                score += 100
            }
            // Name starts with query
            else if (command.name.lowercase().startsWith(lowerQuery)) {
                score += 50
            }
            // Name contains query
            else if (command.name.lowercase().contains(lowerQuery)) {
                score += 25
            }
            // Description contains query
            else if (command.description.lowercase().contains(lowerQuery)) {
                score += 10
            }
            // Alias matches
            else if (command.aliases.any { it.lowercase().contains(lowerQuery) }) {
                score += 30
            }
            
            if (score > 0) {
                suggestions.add(CommandSuggestion(
                    text = command.name,
                    description = command.description,
                    category = command.category,
                    score = score
                ))
            }
        }
        
        return suggestions.sortedByDescending { it.score }
    }
    
    fun getAutoCompleteSuggestions(input: String): List<String> {
        val parts = input.split(" ")
        val currentPart = parts.lastOrNull()?.lowercase() ?: ""
        
        return if (parts.size <= 1) {
            // Command name completion
            _commands.keys.filter { it.lowercase().startsWith(currentPart) }
                .plus(_aliases.keys.filter { it.lowercase().startsWith(currentPart) })
                .sorted()
        } else {
            // Parameter completion
            val commandName = _aliases[parts[0]] ?: parts[0]
            val command = _commands[commandName]
            
            command?.parameters?.filter { param ->
                param.type == ParameterType.ENUM && 
                param.options.any { it.lowercase().startsWith(currentPart) }
            }?.flatMap { param ->
                param.options.filter { it.lowercase().startsWith(currentPart) }
            } ?: emptyList()
        }
    }
    
    fun validatePermissions(context: Context, command: CommandDefinition): Boolean {
        return command.requiresPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun getMissingPermissions(context: Context, command: CommandDefinition): List<String> {
        return command.requiresPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun getHelpText(commandName: String): String {
        val command = getCommand(commandName) ?: return "Command '$commandName' not found"
        
        return buildString {
            appendLine("${command.name} - ${command.description}")
            appendLine()
            appendLine("Usage: ${command.usage}")
            
            if (command.parameters.isNotEmpty()) {
                appendLine()
                appendLine("Parameters:")
                command.parameters.forEach { param ->
                    val required = if (param.required) " (required)" else ""
                    val defaultVal = param.defaultValue?.let { " [default: $it]" } ?: ""
                    appendLine("  ${param.name}: ${param.description}$required$defaultVal")
                    
                    if (param.options.isNotEmpty()) {
                        appendLine("    Options: ${param.options.joinToString(", ")}")
                    }
                }
            }
            
            if (command.examples.isNotEmpty()) {
                appendLine()
                appendLine("Examples:")
                command.examples.forEach { example ->
                    appendLine("  $example")
                }
            }
            
            if (command.aliases.isNotEmpty()) {
                appendLine()
                appendLine("Aliases: ${command.aliases.joinToString(", ")}")
            }
            
            if (command.requiresPermissions.isNotEmpty()) {
                appendLine()
                appendLine("Required permissions: ${command.requiresPermissions.joinToString(", ")}")
            }
        }
    }
    
    fun getCategoryHelp(category: CommandCategory): String {
        val commands = getCommandsByCategory(category)
        if (commands.isEmpty()) return "No commands found in category ${category.displayName}"
        
        return buildString {
            appendLine("${category.displayName} Commands:")
            appendLine()
            commands.forEach { command ->
                appendLine("  ${command.name.padEnd(15)} - ${command.description}")
            }
        }
    }
    
    fun getOverallHelp(): String {
        return buildString {
            appendLine("Win11 Launcher Command Line Interface")
            appendLine()
            appendLine("Available Categories:")
            CommandCategory.values().forEach { category ->
                val count = getCommandsByCategory(category).size
                if (count > 0) {
                    appendLine("  ${category.displayName.padEnd(15)} - $count commands")
                }
            }
            appendLine()
            appendLine("Use 'help <category>' to see commands in a specific category")
            appendLine("Use 'help <command>' to see detailed help for a command")
            appendLine("Use 'commands' to see all available commands")
        }
    }
    
    private fun registerBuiltInCommands() {
        // This will be populated with actual commands in the next step
        // For now, we'll add a placeholder
        val helpCommand = CommandDefinition(
            name = "help",
            category = CommandCategory.SYSTEM,
            description = "Show help information",
            usage = "help [command|category]",
            examples = listOf("help", "help system", "help device"),
            parameters = listOf(
                CommandParameter(
                    name = "topic",
                    type = ParameterType.STRING,
                    description = "Command or category to get help for",
                    required = false
                )
            ),
            executor = object : CommandExecutor {
                override suspend fun execute(
                    context: Context,
                    parameters: Map<String, String>,
                    arguments: List<String>
                ): CommandResult {
                    val topic = parameters["topic"] ?: arguments.firstOrNull()
                    val output = when {
                        topic == null -> getOverallHelp()
                        getCommand(topic) != null -> getHelpText(topic)
                        else -> {
                            // Check if it's a category
                            val category = CommandCategory.values().find { 
                                it.displayName.lowercase() == topic.lowercase() 
                            }
                            if (category != null) {
                                getCategoryHelp(category)
                            } else {
                                "Unknown command or category: $topic"
                            }
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
        
        registerCommand(helpCommand)
    }
}