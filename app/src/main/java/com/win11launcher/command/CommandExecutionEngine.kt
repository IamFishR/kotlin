package com.win11launcher.command

import android.content.Context
import com.win11launcher.command.commands.SystemCommands
import com.win11launcher.command.commands.NetworkCommands
import com.win11launcher.command.commands.AppCommands
import com.win11launcher.data.repositories.CommandLineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandExecutionEngine @Inject constructor(
    private val commandRegistry: CommandRegistry,
    private val commandParser: CommandParser,
    private val commandLineRepository: CommandLineRepository
) {
    
    init {
        registerAllCommands()
    }
    
    private fun registerAllCommands() {
        // Register System Commands
        commandRegistry.registerCommand(SystemCommands.getDeviceCommand())
        commandRegistry.registerCommand(SystemCommands.getSystemCommand())
        commandRegistry.registerCommand(SystemCommands.getMemoryCommand())
        commandRegistry.registerCommand(SystemCommands.getStorageCommand())
        commandRegistry.registerCommand(SystemCommands.getDateCommand())
        commandRegistry.registerCommand(SystemCommands.getVersionCommand())
        commandRegistry.registerCommand(SystemCommands.getUptimeCommand())
        commandRegistry.registerCommand(SystemCommands.getSettingsCommand())
        
        // Register Network Commands
        commandRegistry.registerCommand(NetworkCommands.getNetworkCommand())
        commandRegistry.registerCommand(NetworkCommands.getWifiCommand())
        commandRegistry.registerCommand(NetworkCommands.getBluetoothCommand())
        commandRegistry.registerCommand(NetworkCommands.getPingCommand())
        commandRegistry.registerCommand(NetworkCommands.getNetstatCommand())
        
        // Register App Commands
        commandRegistry.registerCommand(AppCommands.getLaunchCommand())
        commandRegistry.registerCommand(AppCommands.getKillCommand())
        commandRegistry.registerCommand(AppCommands.getAppsCommand())
        commandRegistry.registerCommand(AppCommands.getAppInfoCommand())
        commandRegistry.registerCommand(AppCommands.getUninstallCommand())
        commandRegistry.registerCommand(AppCommands.getInstallCommand())
        commandRegistry.registerCommand(AppCommands.getClearCommand())
        commandRegistry.registerCommand(AppCommands.getPermissionsCommand())
        
        // Register Utility Commands
        registerUtilityCommands()
    }
    
    private fun registerUtilityCommands() {
        // Echo command
        val echoCommand = CommandDefinition(
            name = "echo",
            category = CommandCategory.UTILITY,
            description = "Display text",
            usage = "echo <text>",
            examples = listOf("echo Hello World", "echo \"This is a test\""),
            parameters = listOf(
                CommandParameter(
                    name = "text",
                    type = ParameterType.STRING,
                    description = "Text to display",
                    required = true
                )
            ),
            executor = object : CommandExecutor {
                override suspend fun execute(
                    context: Context,
                    parameters: Map<String, String>,
                    arguments: List<String>
                ): CommandResult {
                    val text = parameters["text"] ?: arguments.joinToString(" ")
                    return CommandResult(
                        success = true,
                        output = text,
                        executionTimeMs = 0
                    )
                }
            }
        )
        
        // Clear command
        val clearCommand = CommandDefinition(
            name = "clear",
            category = CommandCategory.UTILITY,
            description = "Clear the command history",
            usage = "clear",
            examples = listOf("clear"),
            aliases = listOf("cls"),
            executor = object : CommandExecutor {
                override suspend fun execute(
                    context: Context,
                    parameters: Map<String, String>,
                    arguments: List<String>
                ): CommandResult {
                    return CommandResult(
                        success = true,
                        output = "CLEAR_SCREEN",
                        executionTimeMs = 0
                    )
                }
            }
        )
        
        // Commands command
        val commandsCommand = CommandDefinition(
            name = "commands",
            category = CommandCategory.UTILITY,
            description = "List all available commands",
            usage = "commands [--category=<category>]",
            examples = listOf("commands", "commands --category=system"),
            parameters = listOf(
                CommandParameter(
                    name = "category",
                    type = ParameterType.ENUM,
                    description = "Filter by command category",
                    options = CommandCategory.values().map { it.name.lowercase() },
                    required = false
                )
            ),
            executor = object : CommandExecutor {
                override suspend fun execute(
                    context: Context,
                    parameters: Map<String, String>,
                    arguments: List<String>
                ): CommandResult {
                    val categoryFilter = parameters["category"]?.let { categoryName ->
                        CommandCategory.values().find { it.name.lowercase() == categoryName.lowercase() }
                    }
                    
                    val commands = if (categoryFilter != null) {
                        commandRegistry.getCommandsByCategory(categoryFilter)
                    } else {
                        commandRegistry.getAllCommands()
                    }
                    
                    val output = buildString {
                        if (categoryFilter != null) {
                            appendLine("${categoryFilter.displayName} Commands:")
                        } else {
                            appendLine("All Available Commands:")
                        }
                        appendLine()
                        
                        commands.groupBy { it.category }.forEach { (category, categoryCommands) ->
                            if (categoryFilter == null) {
                                appendLine("${category.displayName}:")
                            }
                            
                            categoryCommands.sortedBy { it.name }.forEach { command ->
                                val prefix = if (categoryFilter == null) "  " else ""
                                appendLine("${prefix}${command.name.padEnd(15)} - ${command.description}")
                                
                                if (command.aliases.isNotEmpty()) {
                                    appendLine("${prefix}${" ".repeat(15)} Aliases: ${command.aliases.joinToString(", ")}")
                                }
                            }
                            
                            if (categoryFilter == null) {
                                appendLine()
                            }
                        }
                        
                        appendLine("Use 'help <command>' for detailed information about a specific command")
                    }
                    
                    return CommandResult(
                        success = true,
                        output = output,
                        executionTimeMs = 0
                    )
                }
            }
        )
        
        commandRegistry.registerCommand(echoCommand)
        commandRegistry.registerCommand(clearCommand)
        commandRegistry.registerCommand(commandsCommand)
    }
    
    suspend fun executeCommand(
        context: Context,
        input: String,
        sessionId: String
    ): CommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            // Parse the command
            val parsedCommand = commandParser.parseCommand(input)
            val command = commandRegistry.getCommand(parsedCommand.commandName)
                ?: throw CommandNotFoundException("Command '${parsedCommand.commandName}' not found")
            
            // Check permissions
            if (!commandRegistry.validatePermissions(context, command)) {
                val missingPermissions = commandRegistry.getMissingPermissions(context, command)
                throw CommandPermissionException(
                    "Missing required permissions: ${missingPermissions.joinToString(", ")}"
                )
            }
            
            // Execute the command
            val result = command.executor.execute(
                context = context,
                parameters = parsedCommand.parameters,
                arguments = parsedCommand.arguments
            )
            
            val executionTime = System.currentTimeMillis() - startTime
            
            // Save to database
            saveCommandToDatabase(
                parsedCommand = parsedCommand,
                result = result,
                executionTime = executionTime,
                sessionId = sessionId
            )
            
            result.copy(executionTimeMs = executionTime)
            
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            val errorMessage = commandParser.formatCommandError(e)
            
            // Save error to database
            saveCommandToDatabase(
                parsedCommand = null,
                result = CommandResult(
                    success = false,
                    output = errorMessage,
                    executionTimeMs = executionTime
                ),
                executionTime = executionTime,
                sessionId = sessionId,
                rawCommand = input
            )
            
            CommandResult(
                success = false,
                output = errorMessage,
                executionTimeMs = executionTime
            )
        }
    }
    
    private suspend fun saveCommandToDatabase(
        parsedCommand: ParsedCommand?,
        result: CommandResult,
        executionTime: Long,
        sessionId: String,
        rawCommand: String? = null
    ) {
        try {
            val command = parsedCommand?.commandName ?: rawCommand ?: "unknown"
            val commandType = parsedCommand?.let { 
                commandRegistry.getCommand(it.commandName)?.category?.name 
            } ?: "UNKNOWN"
            
            val outputPreview = if (result.output.length > 200) {
                result.output.substring(0, 200) + "..."
            } else {
                result.output
            }
            
            val commandId = commandLineRepository.insertCommandHistory(
                command = command,
                commandType = commandType,
                subCommand = parsedCommand?.subCommand,
                arguments = parsedCommand?.arguments?.joinToString(" "),
                sessionId = sessionId,
                executionTimeMs = executionTime,
                success = result.success,
                outputPreview = outputPreview
            )
            
            // If output is large, save it separately
            if (result.output.length > 200) {
                commandLineRepository.insertCommandOutput(
                    commandId = commandId,
                    fullOutput = result.output,
                    outputType = "TEXT"
                )
            }
            
            // Update command usage statistics
            commandLineRepository.updateCommandUsage(
                command = command,
                category = commandType,
                executionTimeMs = executionTime,
                success = result.success
            )
            
        } catch (e: Exception) {
            // Log error but don't fail the command execution
            println("Error saving command to database: ${e.message}")
        }
    }
    
    fun getCommandSuggestions(input: String): List<String> {
        return commandParser.getCommandSuggestions(input)
    }
    
    fun searchCommands(query: String): List<CommandSuggestion> {
        return commandRegistry.searchCommands(query)
    }
    
    fun getCommandHelp(commandName: String): String {
        return commandRegistry.getHelpText(commandName)
    }
    
    fun getAllCommands(): List<CommandDefinition> {
        return commandRegistry.getAllCommands()
    }
    
    fun getCommandsByCategory(category: CommandCategory): List<CommandDefinition> {
        return commandRegistry.getCommandsByCategory(category)
    }
}