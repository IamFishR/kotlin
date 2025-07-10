package com.win11launcher.command

import android.content.Context

data class CommandDefinition(
    val name: String,
    val category: CommandCategory,
    val description: String,
    val usage: String,
    val examples: List<String> = emptyList(),
    val parameters: List<CommandParameter> = emptyList(),
    val aliases: List<String> = emptyList(),
    val requiresPermissions: List<String> = emptyList(),
    val minApiLevel: Int = 1,
    val executor: CommandExecutor
)

data class CommandParameter(
    val name: String,
    val type: ParameterType,
    val description: String,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val options: List<String> = emptyList(), // For enum-like parameters
    val validator: ((String) -> Boolean)? = null
)

enum class CommandCategory(val displayName: String, val color: String) {
    SYSTEM("System", "#0078D4"),
    NET("Network", "#00BCF2"),
    APP("Applications", "#00CC6A"),
    FILE("Files", "#FFB900"),
    DEV("Development", "#8764B8"),
    AI("AI Assistant", "#FF4842"),
    UTILITY("Utilities", "#737373"),
    USER("User Scripts", "#00B294")
}

enum class ParameterType {
    STRING,
    INTEGER,
    BOOLEAN,
    ENUM,
    PATH,
    IP_ADDRESS,
    MAC_ADDRESS,
    PACKAGE_NAME,
    URL
}

data class CommandResult(
    val success: Boolean,
    val output: String,
    val executionTimeMs: Long,
    val data: Map<String, Any> = emptyMap(),
    val suggestions: List<String> = emptyList()
)

interface CommandExecutor {
    suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult
}

data class ParsedCommand(
    val commandName: String,
    val subCommand: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val arguments: List<String> = emptyList(),
    val rawInput: String
)

data class CommandSuggestion(
    val text: String,
    val description: String,
    val category: CommandCategory,
    val score: Int = 0
)

class CommandValidationException(message: String) : Exception(message)
class CommandNotFoundException(message: String) : Exception(message)
class CommandPermissionException(message: String) : Exception(message)