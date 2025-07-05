package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.win11launcher.services.AIService
import com.win11launcher.viewmodel.AIServiceViewModel
import kotlinx.coroutines.launch

@Composable
fun CommandPrompt(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            CommandPromptWindow(
                onClose = onDismiss,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CommandPromptWindow(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commandHistory by remember { mutableStateOf(listOf<CommandEntry>()) }
    var currentCommand by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val aiService = hiltViewModel<AIServiceViewModel>().aiService
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-focus the input field when the dialog opens
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
    
    // Auto-scroll to bottom when new commands are added
    LaunchedEffect(commandHistory.size) {
        if (commandHistory.isNotEmpty()) {
            listState.animateScrollToItem(commandHistory.size - 1)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF808080),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C0C0C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Command Prompt",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Command history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Welcome message
                if (commandHistory.isEmpty()) {
                    item {
                        Text(
                            text = "Win11 Launcher Command Prompt [Version 1.0.0]\n(c) Win11 Launcher. All rights reserved.\n",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                items(commandHistory) { entry ->
                    CommandEntry(entry = entry)
                }
            }
            
            // Command input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0C0C))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "android> ",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = currentCommand,
                    onValueChange = { currentCommand = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = !isProcessing,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (currentCommand.isNotBlank() && !isProcessing) {
                                isProcessing = true
                                val command = currentCommand.trim()
                                currentCommand = ""
                                
                                // Handle AI commands asynchronously
                                if (command.startsWith("ai ") || command in listOf("ai-info", "ai-status", "ai-clear")) {
                                    // Add processing entry immediately
                                    commandHistory = commandHistory + CommandEntry(
                                        command = command,
                                        output = "🤖 AI is thinking...",
                                        timestamp = System.currentTimeMillis(),
                                        isProcessing = true
                                    )
                                    
                                    coroutineScope.launch {
                                        val result = executeAICommand(command, aiService)
                                        // Update the last entry with the result
                                        commandHistory = commandHistory.dropLast(1) + CommandEntry(
                                            command = command,
                                            output = result,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        isProcessing = false
                                    }
                                } else {
                                    // Handle regular commands synchronously
                                    val result = executeCommand(command, context)
                                    
                                    // Handle clear/cls commands
                                    if (command.lowercase().trim() in listOf("clear", "cls")) {
                                        commandHistory = emptyList()
                                    } else {
                                        commandHistory = commandHistory + CommandEntry(
                                            command = command,
                                            output = result,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }
                                    isProcessing = false
                                }
                                keyboardController?.hide()
                            }
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun CommandEntry(
    entry: CommandEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Command input
        Text(
            text = "android> ${entry.command}",
            color = Color(0xFF4CAF50),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        
        // Command output
        if (entry.output.isNotBlank()) {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.output,
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                
                // Show loading indicator for processing AI commands
                if (entry.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 8.dp),
                        color = Color(0xFF4CAF50),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

private data class CommandEntry(
    val command: String,
    val output: String,
    val timestamp: Long,
    val isProcessing: Boolean = false
)

private fun executeCommand(command: String, context: android.content.Context): String {
    return when (command.lowercase().trim()) {
        "help" -> {
            """Available commands:
help - Show this help message
clear - Clear the screen
date - Show current date
time - Show current time
echo <text> - Display text
ver - Show version information
exit - Close command prompt
about - Show about information
calc <expression> - Simple calculator (e.g., calc 2+2)

Device Information Commands:
device - Show device information
system - Show system information
hardware - Show hardware information
build - Show build information
memory - Show memory information
network - Show network information

AI Commands (Powered by Gemma 3):
ai <question> - Ask AI anything
ai-info - Show AI model information
ai-status - Check AI model status
ai-clear - Clear AI conversation history
"""
        }
        "clear", "cls" -> {
            "" // Clear command returns empty string, actual clearing handled by UI
        }
        "date" -> {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
        }
        "time" -> {
            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
        }
        "ver", "version" -> {
            "Win11 Launcher Command Prompt [Version 1.0.0]"
        }
        "about" -> {
            """Win11 Launcher Command Prompt
Version 1.0.0
A custom command prompt for Win11 Launcher
Built with Jetpack Compose"""
        }
        "exit" -> {
            "Closing command prompt..."
        }
        "device" -> {
            getDeviceInfo()
        }
        "system" -> {
            getSystemInfo()
        }
        "hardware" -> {
            getHardwareInfo()
        }
        "build" -> {
            getBuildInfo()
        }
        "memory" -> {
            getMemoryInfo(context)
        }
        "network" -> {
            getNetworkInfo(context)
        }
        else -> {
            when {
                command.startsWith("echo ") -> {
                    command.substring(5) // Remove "echo " prefix
                }
                command.startsWith("calc ") -> {
                    try {
                        val expression = command.substring(5).trim()
                        calculateExpression(expression)
                    } catch (e: Exception) {
                        "Error: Invalid expression"
                    }
                }
                else -> {
                    "'$command' is not recognized as an internal or external command.\nType 'help' for available commands."
                }
            }
        }
    }
}

private fun calculateExpression(expression: String): String {
    return try {
        // Simple calculator for basic arithmetic
        val sanitized = expression.replace(" ", "")
        when {
            "+" in sanitized -> {
                val parts = sanitized.split("+")
                if (parts.size == 2) {
                    val result = parts[0].toDouble() + parts[1].toDouble()
                    if (result == result.toInt().toDouble()) {
                        result.toInt().toString()
                    } else {
                        result.toString()
                    }
                } else "Error: Invalid expression"
            }
            "-" in sanitized && sanitized.indexOf("-") > 0 -> {
                val parts = sanitized.split("-")
                if (parts.size == 2) {
                    val result = parts[0].toDouble() - parts[1].toDouble()
                    if (result == result.toInt().toDouble()) {
                        result.toInt().toString()
                    } else {
                        result.toString()
                    }
                } else "Error: Invalid expression"
            }
            "*" in sanitized -> {
                val parts = sanitized.split("*")
                if (parts.size == 2) {
                    val result = parts[0].toDouble() * parts[1].toDouble()
                    if (result == result.toInt().toDouble()) {
                        result.toInt().toString()
                    } else {
                        result.toString()
                    }
                } else "Error: Invalid expression"
            }
            "/" in sanitized -> {
                val parts = sanitized.split("/")
                if (parts.size == 2 && parts[1].toDouble() != 0.0) {
                    val result = parts[0].toDouble() / parts[1].toDouble()
                    if (result == result.toInt().toDouble()) {
                        result.toInt().toString()
                    } else {
                        result.toString()
                    }
                } else "Error: Division by zero or invalid expression"
            }
            else -> "Error: Only +, -, *, / operations are supported"
        }
    } catch (e: Exception) {
        "Error: Invalid expression"
    }
}

private fun getDeviceInfo(): String {
    return """Device Information:
Manufacturer: ${Build.MANUFACTURER}
Brand: ${Build.BRAND}
Model: ${Build.MODEL}
Device: ${Build.DEVICE}
Product: ${Build.PRODUCT}
Board: ${Build.BOARD}
Hardware: ${Build.HARDWARE}
"""
}

private fun getSystemInfo(): String {
    return """System Information:
Android Version: ${Build.VERSION.RELEASE}
API Level: ${Build.VERSION.SDK_INT}
Security Patch: ${Build.VERSION.SECURITY_PATCH}
Build ID: ${Build.ID}
Display: ${Build.DISPLAY}
Host: ${Build.HOST}
User: ${Build.USER}
"""
}

private fun getHardwareInfo(): String {
    return """Hardware Information:
Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}
Supported 32-bit ABIs: ${Build.SUPPORTED_32_BIT_ABIS.joinToString(", ")}
Supported 64-bit ABIs: ${Build.SUPPORTED_64_BIT_ABIS.joinToString(", ")}
Hardware: ${Build.HARDWARE}
Board: ${Build.BOARD}
Bootloader: ${Build.BOOTLOADER}
Radio Version: ${Build.getRadioVersion()}
"""
}

private fun getBuildInfo(): String {
    return """Build Information:
Type: ${Build.TYPE}
Tags: ${Build.TAGS}
Fingerprint: ${Build.FINGERPRINT}
Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(Build.TIME))}
Incremental: ${Build.VERSION.INCREMENTAL}
Codename: ${Build.VERSION.CODENAME}
"""
}

private fun getMemoryInfo(context: android.content.Context): String {
    val activityManager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    val memInfo = android.app.ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)
    
    val totalMemory = memInfo.totalMem / (1024 * 1024) // Convert to MB
    val availableMemory = memInfo.availMem / (1024 * 1024) // Convert to MB
    val usedMemory = totalMemory - availableMemory
    val threshold = memInfo.threshold / (1024 * 1024) // Convert to MB
    
    return """Memory Information:
Total Memory: ${totalMemory} MB
Available Memory: ${availableMemory} MB
Used Memory: ${usedMemory} MB
Low Memory Threshold: ${threshold} MB
Is Low Memory: ${memInfo.lowMemory}
"""
}

private fun getNetworkInfo(context: android.content.Context): String {
    try {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        
        return if (networkInfo != null) {
            """Network Information:
Connected: ${networkInfo.isConnected}
Type: ${networkInfo.typeName}
Subtype: ${networkInfo.subtypeName}
State: ${networkInfo.state}
Detailed State: ${networkInfo.detailedState}
Extra Info: ${networkInfo.extraInfo ?: "N/A"}
Is Roaming: ${networkInfo.isRoaming}
"""
        } else {
            "Network Information:\nNo active network connection"
        }
    } catch (e: Exception) {
        return "Network Information:\nError retrieving network info: ${e.message}"
    }
}

private suspend fun executeAICommand(command: String, aiService: AIService): String {
    return when {
        command.startsWith("ai ") -> {
            val question = command.substring(3).trim()
            if (question.isBlank()) {
                "Please provide a question. Example: ai What is Android?"
            } else {
                val response = aiService.generateResponse(question)
                if (response.success) {
                    "🤖 AI: ${response.response}"
                } else {
                    "❌ AI Error: ${response.error}"
                }
            }
        }
        command == "ai-info" -> {
            "🤖 ${aiService.getModelInfo()}"
        }
        command == "ai-status" -> {
            "🤖 Status: ${aiService.getModelStatus()}"
        }
        command == "ai-clear" -> {
            "🤖 AI conversation history cleared"
        }
        else -> "Unknown AI command"
    }
}