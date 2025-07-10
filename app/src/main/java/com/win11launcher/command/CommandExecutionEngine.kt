package com.win11launcher.command

import android.content.Context
import com.win11launcher.command.commands.SystemCommands
import com.win11launcher.command.commands.NetworkCommands
import com.win11launcher.command.commands.AppCommands
import com.win11launcher.command.commands.FileCommands
import com.win11launcher.command.commands.PowerCommands
import com.win11launcher.command.commands.AICommandProvider
import com.win11launcher.data.repositories.CommandLineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandExecutionEngine @Inject constructor(
    private val commandRegistry: CommandRegistry,
    private val commandParser: CommandParser,
    private val commandLineRepository: CommandLineRepository,
    private val aiCommandProvider: AICommandProvider
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
        commandRegistry.registerCommand(SystemCommands.getMonitorCommand())
        commandRegistry.registerCommand(SystemCommands.getSnapshotCommand())
        
        // Register Network Commands
        commandRegistry.registerCommand(NetworkCommands.getNetworkCommand())
        commandRegistry.registerCommand(NetworkCommands.getWifiCommand())
        commandRegistry.registerCommand(NetworkCommands.getBluetoothCommand())
        commandRegistry.registerCommand(NetworkCommands.getPingCommand())
        commandRegistry.registerCommand(NetworkCommands.getNetstatCommand())
        commandRegistry.registerCommand(NetworkCommands.getWifiAdvancedCommand())
        commandRegistry.registerCommand(NetworkCommands.getNetworkMonitorCommand())
        commandRegistry.registerCommand(NetworkCommands.getNetworkProfileCommand())
        
        // Register App Commands
        commandRegistry.registerCommand(AppCommands.getLaunchCommand())
        commandRegistry.registerCommand(AppCommands.getKillCommand())
        commandRegistry.registerCommand(AppCommands.getAppsCommand())
        commandRegistry.registerCommand(AppCommands.getAppInfoCommand())
        commandRegistry.registerCommand(AppCommands.getUninstallCommand())
        commandRegistry.registerCommand(AppCommands.getInstallCommand())
        commandRegistry.registerCommand(AppCommands.getClearCommand())
        commandRegistry.registerCommand(AppCommands.getPermissionsCommand())
        
        // Register Advanced App Management Commands (Phase 4)
        commandRegistry.registerCommand(AppCommands.getAppMonCommand())
        commandRegistry.registerCommand(AppCommands.getAppStatsCommand())
        commandRegistry.registerCommand(AppCommands.getPermCheckCommand())
        commandRegistry.registerCommand(AppCommands.getPermGrantCommand())
        commandRegistry.registerCommand(AppCommands.getPermRevokeCommand())
        commandRegistry.registerCommand(AppCommands.getAppCleanupCommand())
        commandRegistry.registerCommand(AppCommands.getAppDepsCommand())
        
        // Register File Commands
        commandRegistry.registerCommand(FileCommands.getListCommand())
        commandRegistry.registerCommand(FileCommands.getCopyCommand())
        commandRegistry.registerCommand(FileCommands.getMoveCommand())
        commandRegistry.registerCommand(FileCommands.getRemoveCommand())
        commandRegistry.registerCommand(FileCommands.getMkdirCommand())
        commandRegistry.registerCommand(FileCommands.getCatCommand())
        commandRegistry.registerCommand(FileCommands.getPwdCommand())
        commandRegistry.registerCommand(FileCommands.getTouchCommand())
        commandRegistry.registerCommand(FileCommands.getFindCommand())
        commandRegistry.registerCommand(FileCommands.getGrepCommand())
        commandRegistry.registerCommand(FileCommands.getStatCommand())
        commandRegistry.registerCommand(FileCommands.getHeadCommand())
        commandRegistry.registerCommand(FileCommands.getTailCommand())
        commandRegistry.registerCommand(FileCommands.getWcCommand())
        
        // Register Power Commands
        commandRegistry.registerCommand(PowerCommands.getPowerCommand())
        commandRegistry.registerCommand(PowerCommands.getBatteryCommand())
        commandRegistry.registerCommand(PowerCommands.getThermalCommand())
        commandRegistry.registerCommand(PowerCommands.getScreenCommand())
        
        // Register AI Commands with proper DI
        commandRegistry.registerCommand(aiCommandProvider.getAskCommand())
        commandRegistry.registerCommand(aiCommandProvider.getInterpretCommand())
        commandRegistry.registerCommand(aiCommandProvider.getAnalyzeCommand())
        commandRegistry.registerCommand(aiCommandProvider.getOptimizeCommand())
        commandRegistry.registerCommand(aiCommandProvider.getSuggestCommand())
        commandRegistry.registerCommand(aiCommandProvider.getMemoryCommand())
        commandRegistry.registerCommand(aiCommandProvider.getScriptCommand())
        
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
    ): String? {
        return try {
            val command = parsedCommand?.commandName ?: rawCommand ?: "unknown"
            val commandType = parsedCommand?.let { 
                commandRegistry.getCommand(it.commandName)?.category?.name 
            } ?: "UNKNOWN"
            
            // Process output with compression and size management
            val processedOutput = OutputManager.processCommandOutput(result.output)
            
            val commandId = commandLineRepository.insertCommandHistory(
                command = command,
                commandType = commandType,
                subCommand = parsedCommand?.subCommand,
                arguments = parsedCommand?.arguments?.joinToString(" "),
                sessionId = sessionId,
                executionTimeMs = executionTime,
                success = result.success,
                outputPreview = processedOutput.preview
            )
            
            // Save full output if needed
            if (processedOutput.fullOutput != null) {
                commandLineRepository.insertCommandOutput(
                    commandId = commandId,
                    fullOutput = processedOutput.fullOutput,
                    outputType = if (processedOutput.compressed) "COMPRESSED" else "TEXT",
                    compressed = processedOutput.compressed
                )
            }
            
            // Update command usage statistics
            commandLineRepository.updateCommandUsage(
                command = command,
                category = commandType,
                executionTimeMs = executionTime,
                success = result.success
            )
            
            // Track file operations for FILE category commands
            if (commandType == "FILE" && parsedCommand != null) {
                trackFileOperation(
                    commandName = command,
                    parameters = parsedCommand.parameters,
                    arguments = parsedCommand.arguments,
                    result = result,
                    commandId = commandId
                )
            }
            
            commandId
        } catch (e: Exception) {
            // Log error but don't fail the command execution
            println("Error saving command to database: ${e.message}")
            null
        }
    }
    
    private suspend fun trackFileOperation(
        commandName: String,
        parameters: Map<String, String>,
        arguments: List<String>,
        result: CommandResult,
        commandId: String
    ) {
        try {
            when (commandName.lowercase()) {
                "ls", "list", "dir" -> {
                    val path = parameters["path"] ?: arguments.firstOrNull() ?: "."
                    val actualPath = if (path == ".") {
                        android.os.Environment.getExternalStorageDirectory().absolutePath
                    } else {
                        path
                    }
                    
                    val fileCount = if (result.success) {
                        val dir = java.io.File(actualPath)
                        dir.listFiles()?.size ?: 0
                    } else 0
                    
                    commandLineRepository.insertFileOperation(
                        operation = "LS",
                        sourcePath = actualPath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = fileCount,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "cp", "copy" -> {
                    val source = parameters["source"] ?: arguments.getOrNull(0) ?: ""
                    val destination = parameters["destination"] ?: arguments.getOrNull(1) ?: ""
                    
                    val bytesTransferred = if (result.success && source.isNotEmpty()) {
                        val sourceFile = java.io.File(source)
                        if (sourceFile.exists()) {
                            calculateFileSize(sourceFile)
                        } else null
                    } else null
                    
                    commandLineRepository.insertFileOperation(
                        operation = "CP",
                        sourcePath = source,
                        destinationPath = destination,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = bytesTransferred,
                        commandId = commandId
                    )
                }
                
                "mv", "move", "rename" -> {
                    val source = parameters["source"] ?: arguments.getOrNull(0) ?: ""
                    val destination = parameters["destination"] ?: arguments.getOrNull(1) ?: ""
                    
                    val bytesTransferred = if (result.success && source.isNotEmpty()) {
                        val sourceFile = java.io.File(source)
                        if (sourceFile.exists()) {
                            calculateFileSize(sourceFile)
                        } else null
                    } else null
                    
                    commandLineRepository.insertFileOperation(
                        operation = "MV",
                        sourcePath = source,
                        destinationPath = destination,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = bytesTransferred,
                        commandId = commandId
                    )
                }
                
                "rm", "delete", "del" -> {
                    val path = parameters["path"] ?: arguments.firstOrNull() ?: ""
                    val fileCount = if (result.success && path.isNotEmpty()) {
                        val file = java.io.File(path)
                        if (file.isDirectory) {
                            countFilesRecursively(file)
                        } else 1
                    } else 0
                    
                    commandLineRepository.insertFileOperation(
                        operation = "RM",
                        sourcePath = path,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = fileCount,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "mkdir", "makedir" -> {
                    val path = parameters["path"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "MKDIR",
                        sourcePath = path,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "cat", "view", "type" -> {
                    val filePath = parameters["file"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "CAT",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "pwd", "cwd" -> {
                    val currentDir = android.os.Environment.getExternalStorageDirectory().absolutePath
                    
                    commandLineRepository.insertFileOperation(
                        operation = "PWD",
                        sourcePath = currentDir,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 0,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "touch", "create" -> {
                    val filePath = parameters["file"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "TOUCH",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "find", "search" -> {
                    val path = parameters["path"] ?: arguments.firstOrNull() ?: ""
                    val resultsCount = if (result.success) {
                        // Extract count from output if possible
                        extractNumberFromOutput(result.output, "Found (\\d+) matches")
                    } else 0
                    
                    commandLineRepository.insertFileOperation(
                        operation = "FIND",
                        sourcePath = path,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = resultsCount,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "grep", "search-text" -> {
                    val filePath = parameters["file"] ?: arguments.getOrNull(1) ?: ""
                    val matchesCount = if (result.success) {
                        extractNumberFromOutput(result.output, "Found (\\d+) matches")
                    } else 0
                    
                    commandLineRepository.insertFileOperation(
                        operation = "GREP",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = matchesCount,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "stat", "info" -> {
                    val path = parameters["path"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "STAT",
                        sourcePath = path,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "head", "top" -> {
                    val filePath = parameters["file"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "HEAD",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "tail", "bottom" -> {
                    val filePath = parameters["file"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "TAIL",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
                
                "wc", "count" -> {
                    val filePath = parameters["file"] ?: arguments.firstOrNull() ?: ""
                    
                    commandLineRepository.insertFileOperation(
                        operation = "WC",
                        sourcePath = filePath,
                        destinationPath = null,
                        success = result.success,
                        filesAffected = 1,
                        bytesTransferred = null,
                        commandId = commandId
                    )
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the command execution
            println("Error tracking file operation: ${e.message}")
        }
    }
    
    private fun calculateFileSize(file: java.io.File): Long {
        return if (file.isDirectory) {
            var size = 0L
            file.walkTopDown().forEach { f ->
                if (f.isFile) {
                    size += f.length()
                }
            }
            size
        } else {
            file.length()
        }
    }
    
    private fun countFilesRecursively(directory: java.io.File): Int {
        var count = 0
        directory.walkTopDown().forEach { f ->
            if (f.isFile) {
                count++
            }
        }
        return count
    }
    
    private fun extractNumberFromOutput(output: String, pattern: String): Int {
        return try {
            val regex = pattern.toRegex()
            val match = regex.find(output)
            match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
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