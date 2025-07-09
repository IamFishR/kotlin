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
import kotlinx.coroutines.delay
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.win11launcher.services.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import com.win11launcher.ui.layout.LayoutConstants

@Composable
fun CommandPrompt(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommandPromptViewModel = hiltViewModel()
) {
    if (isVisible) {
        // Use a Box overlay instead of Dialog to respect working area boundaries
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            CommandPromptWindow(
                onClose = onDismiss,
                modifier = Modifier.clickable { }, // Prevent clicks from propagating to background
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun CommandPromptWindow(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommandPromptViewModel
) {
    var commandHistory by remember { mutableStateOf(listOf<CommandEntry>()) }
    var currentCommand by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-focus the input field when the dialog opens
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
    
    // Re-focus the input field after processing is complete
    LaunchedEffect(isProcessing) {
        if (!isProcessing) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
    
    // Auto-scroll to bottom when new commands are added
    LaunchedEffect(commandHistory.size) {
        if (commandHistory.isNotEmpty()) {
            listState.animateScrollToItem(commandHistory.size - 1)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .fillMaxHeight(0.6f)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            .border(
                width = LayoutConstants.COMMAND_PROMPT_BORDER_WIDTH,
                color = Color(0xFF808080),
                shape = RoundedCornerShape(LayoutConstants.SPACING_MEDIUM)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C0C0C).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = LayoutConstants.COMMAND_PROMPT_ELEVATION)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F))
                    .padding(horizontal = LayoutConstants.SPACING_LARGE, vertical = LayoutConstants.SPACING_MEDIUM),
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
                    modifier = Modifier.size(LayoutConstants.ICON_EXTRA_LARGE)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(LayoutConstants.ICON_MEDIUM)
                    )
                }
            }
            
            // Command history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = LayoutConstants.SPACING_LARGE, vertical = LayoutConstants.SPACING_MEDIUM),
                verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
            ) {
                // Welcome message
                if (commandHistory.isEmpty()) {
                    item {
                        Text(
                            text = "Win11 Launcher Command Prompt [Version 1.0.0]\n(c) Win11 Launcher. All rights reserved.\n",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = LayoutConstants.SPACING_MEDIUM)
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
                    .padding(horizontal = LayoutConstants.SPACING_LARGE, vertical = LayoutConstants.SPACING_MEDIUM),
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
                                
                                // Handle commands
                                coroutineScope.launch {
                                    // Handle clear/cls commands immediately
                                    if (command.lowercase().trim() in listOf("clear", "cls")) {
                                        commandHistory = emptyList()
                                        isProcessing = false
                                        return@launch
                                    }
                                    
                                    // Handle regular commands (including async AI commands)
                                    if (command.startsWith("ai ") || command.startsWith("ask ")) {
                                        // Handle AI commands with streaming effect
                                        val prompt = if (command.startsWith("ai ")) {
                                            command.substring(3).trim()
                                        } else {
                                            command.substring(4).trim()
                                        }
                                        
                                        if (prompt.isNotEmpty()) {
                                            // Add command entry immediately with loading state
                                            val tempEntry = CommandEntry(
                                                command = command,
                                                output = "Processing...",
                                                timestamp = System.currentTimeMillis(),
                                                isProcessing = true
                                            )
                                            commandHistory = commandHistory + tempEntry
                                            
                                            // Get AI response
                                            val aiResponse = viewModel.processAICommand(prompt)
                                            
                                            // Replace with streaming entry
                                            val streamingEntry = CommandEntry(
                                                command = command,
                                                output = "",
                                                timestamp = System.currentTimeMillis(),
                                                isStreaming = true
                                            )
                                            commandHistory = commandHistory.dropLast(1) + streamingEntry
                                            
                                            // Simulate streaming by showing words one by one
                                            val words = aiResponse.split(" ")
                                            var currentText = ""
                                            
                                            for (i in words.indices) {
                                                currentText += if (i == 0) words[i] else " ${words[i]}"
                                                
                                                val updatedEntry = streamingEntry.copy(
                                                    output = currentText,
                                                    isStreaming = i < words.size - 1
                                                )
                                                commandHistory = commandHistory.dropLast(1) + updatedEntry
                                                
                                                // Add delay between words to simulate streaming
                                                delay(50)
                                            }
                                        } else {
                                            commandHistory = commandHistory + CommandEntry(
                                                command = command,
                                                output = "Error: Please provide a prompt after '${command.split(" ")[0]}' command",
                                                timestamp = System.currentTimeMillis()
                                            )
                                        }
                                    } else {
                                        // Handle regular commands
                                        val result = executeCommand(command, context, viewModel)
                                        
                                        commandHistory = commandHistory + CommandEntry(
                                            command = command,
                                            output = result,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }
                                    isProcessing = false
                                }
                                // Don't hide keyboard - keep it visible for next command
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
                modifier = Modifier.padding(start = LayoutConstants.SPACING_MEDIUM),
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
                            .size(LayoutConstants.COMMAND_PROMPT_PROGRESS_SIZE)
                            .padding(start = LayoutConstants.SPACING_MEDIUM),
                        color = Color(0xFF4CAF50),
                        strokeWidth = LayoutConstants.COMMAND_PROMPT_PROGRESS_STROKE
                    )
                }
                
                // Show streaming indicator
                if (entry.isStreaming) {
                    Text(
                        text = "▋",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = LayoutConstants.SPACING_SMALL)
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
    val isProcessing: Boolean = false,
    val isStreaming: Boolean = false
)

private suspend fun executeCommand(command: String, context: android.content.Context, viewModel: CommandPromptViewModel): String {
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

AI Commands:
ai <prompt> - Ask AI a question (e.g., ai What is Android?)
ask <prompt> - Same as ai command

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
                command.startsWith("ai ") -> {
                    "AI commands are handled separately with streaming support"
                }
                command.startsWith("ask ") -> {
                    "AI commands are handled separately with streaming support"
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

@HiltViewModel
class CommandPromptViewModel @Inject constructor(
    private val aiService: AIService
) : ViewModel() {
    
    suspend fun processAICommand(prompt: String): String {
        return try {
            val response = aiService.generateResponse(prompt)
            if (response.success) {
                response.response
            } else {
                "AI Error: ${response.response}"
            }
        } catch (e: Exception) {
            "AI Error: ${e.message}"
        }
    }
}

