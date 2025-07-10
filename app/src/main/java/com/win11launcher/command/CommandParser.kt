package com.win11launcher.command

import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandParser @Inject constructor(
    private val commandRegistry: CommandRegistry
) {
    
    private val parameterPattern = Pattern.compile("--([a-zA-Z][a-zA-Z0-9-]*)(=([^\\s]+))?")
    private val flagPattern = Pattern.compile("-([a-zA-Z])")
    
    fun parseCommand(input: String): ParsedCommand {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            throw CommandValidationException("Empty command")
        }
        
        // Split by spaces but preserve quoted strings
        val tokens = tokenize(trimmedInput)
        if (tokens.isEmpty()) {
            throw CommandValidationException("Invalid command format")
        }
        
        val commandName = tokens[0]
        val remainingTokens = tokens.drop(1)
        
        // Check if command exists
        val command = commandRegistry.getCommand(commandName)
            ?: throw CommandNotFoundException("Command '$commandName' not found")
        
        // Parse parameters and arguments
        val parameters = mutableMapOf<String, String>()
        val arguments = mutableListOf<String>()
        var subCommand: String? = null
        
        var i = 0
        while (i < remainingTokens.size) {
            val token = remainingTokens[i]
            
            when {
                // Long parameter (--param=value or --param value)
                token.startsWith("--") -> {
                    val matcher = parameterPattern.matcher(token)
                    if (matcher.matches()) {
                        val paramName = matcher.group(1)
                        val paramValue = matcher.group(3)
                        
                        if (paramValue != null) {
                            parameters[paramName] = paramValue
                        } else if (i + 1 < remainingTokens.size && !remainingTokens[i + 1].startsWith("-")) {
                            parameters[paramName] = remainingTokens[i + 1]
                            i++
                        } else {
                            parameters[paramName] = "true" // Boolean flag
                        }
                    } else {
                        throw CommandValidationException("Invalid parameter format: $token")
                    }
                }
                
                // Short flag (-f)
                token.startsWith("-") -> {
                    val matcher = flagPattern.matcher(token)
                    if (matcher.matches()) {
                        val flagName = matcher.group(1)
                        parameters[flagName] = "true"
                    } else {
                        throw CommandValidationException("Invalid flag format: $token")
                    }
                }
                
                // Sub-command (first non-parameter token)
                subCommand == null && !token.startsWith("-") -> {
                    subCommand = token
                }
                
                // Regular argument
                else -> {
                    arguments.add(token)
                }
            }
            i++
        }
        
        // Validate the parsed command
        validateParsedCommand(command, parameters, arguments, subCommand)
        
        return ParsedCommand(
            commandName = commandName,
            subCommand = subCommand,
            parameters = parameters,
            arguments = arguments,
            rawInput = trimmedInput
        )
    }
    
    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = '"'
        
        for (i in input.indices) {
            val char = input[i]
            
            when {
                char == '"' || char == '\'' -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                        // Don't include the quote in the token
                    } else {
                        current.append(char)
                    }
                }
                
                char.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                
                else -> {
                    current.append(char)
                }
            }
        }
        
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }
        
        if (inQuotes) {
            throw CommandValidationException("Unclosed quote in command")
        }
        
        return tokens
    }
    
    private fun validateParsedCommand(
        command: CommandDefinition,
        parameters: Map<String, String>,
        arguments: List<String>,
        subCommand: String?
    ) {
        // Validate required parameters
        command.parameters.filter { it.required }.forEach { param ->
            if (!parameters.containsKey(param.name)) {
                throw CommandValidationException("Required parameter '--${param.name}' is missing")
            }
        }
        
        // Validate parameter types and values
        parameters.forEach { (name, value) ->
            val paramDef = command.parameters.find { it.name == name }
                ?: throw CommandValidationException("Unknown parameter '--$name'")
            
            validateParameterValue(paramDef, value)
        }
        
        // Set default values for missing optional parameters
        command.parameters.forEach { param ->
            if (!param.required && !parameters.containsKey(param.name) && param.defaultValue != null) {
                parameters.toMutableMap()[param.name] = param.defaultValue
            }
        }
    }
    
    private fun validateParameterValue(parameter: CommandParameter, value: String) {
        when (parameter.type) {
            ParameterType.INTEGER -> {
                try {
                    value.toInt()
                } catch (e: NumberFormatException) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be an integer")
                }
            }
            
            ParameterType.BOOLEAN -> {
                if (value.lowercase() !in listOf("true", "false", "1", "0", "yes", "no")) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be a boolean value")
                }
            }
            
            ParameterType.ENUM -> {
                if (parameter.options.isNotEmpty() && value !in parameter.options) {
                    throw CommandValidationException(
                        "Parameter '${parameter.name}' must be one of: ${parameter.options.joinToString(", ")}"
                    )
                }
            }
            
            ParameterType.IP_ADDRESS -> {
                if (!isValidIpAddress(value)) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be a valid IP address")
                }
            }
            
            ParameterType.MAC_ADDRESS -> {
                if (!isValidMacAddress(value)) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be a valid MAC address")
                }
            }
            
            ParameterType.URL -> {
                if (!isValidUrl(value)) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be a valid URL")
                }
            }
            
            ParameterType.PACKAGE_NAME -> {
                if (!isValidPackageName(value)) {
                    throw CommandValidationException("Parameter '${parameter.name}' must be a valid package name")
                }
            }
            
            ParameterType.PATH -> {
                // Basic path validation - could be enhanced
                if (value.contains("..") || value.contains("//")) {
                    throw CommandValidationException("Parameter '${parameter.name}' contains invalid path characters")
                }
            }
            
            ParameterType.STRING -> {
                // String validation is handled by custom validator if provided
            }
        }
        
        // Apply custom validator if provided
        parameter.validator?.let { validator ->
            if (!validator(value)) {
                throw CommandValidationException("Parameter '${parameter.name}' failed validation")
            }
        }
    }
    
    private fun isValidIpAddress(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            try {
                val num = part.toInt()
                num in 0..255
            } catch (e: NumberFormatException) {
                false
            }
        }
    }
    
    private fun isValidMacAddress(mac: String): Boolean {
        val macPattern = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        return macPattern.matcher(mac).matches()
    }
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isValidPackageName(packageName: String): Boolean {
        val packagePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)*$")
        return packagePattern.matcher(packageName).matches()
    }
    
    fun getCommandSuggestions(input: String): List<String> {
        val trimmedInput = input.trim()
        
        return if (trimmedInput.isEmpty()) {
            commandRegistry.getAllCommands().map { it.name }.sorted()
        } else {
            try {
                val tokens = tokenize(trimmedInput)
                if (tokens.size == 1) {
                    // Command name completion
                    commandRegistry.getAutoCompleteSuggestions(tokens[0])
                } else {
                    // Parameter completion
                    val commandName = tokens[0]
                    val command = commandRegistry.getCommand(commandName)
                    
                    if (command != null) {
                        val lastToken = tokens.last()
                        if (lastToken.startsWith("--")) {
                            // Parameter name completion
                            command.parameters.map { "--${it.name}" }
                                .filter { it.startsWith(lastToken) }
                        } else {
                            // Parameter value completion
                            commandRegistry.getAutoCompleteSuggestions(trimmedInput)
                        }
                    } else {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    fun formatCommandError(error: Exception): String {
        return when (error) {
            is CommandNotFoundException -> {
                val similar = commandRegistry.searchCommands(error.message?.substringAfter("'")?.substringBefore("'") ?: "")
                    .take(3)
                    .joinToString(", ") { it.text }
                
                if (similar.isNotEmpty()) {
                    "${error.message}\n\nDid you mean: $similar"
                } else {
                    "${error.message}\n\nUse 'help' to see all available commands"
                }
            }
            
            is CommandValidationException -> {
                error.message ?: "Command validation failed"
            }
            
            is CommandPermissionException -> {
                "${error.message}\n\nPlease grant the required permissions and try again"
            }
            
            else -> {
                "Command parsing error: ${error.message}"
            }
        }
    }
}