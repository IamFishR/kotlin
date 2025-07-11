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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.win11launcher.data.repositories.CommandLineRepository
import com.win11launcher.command.CommandExecutionEngine
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
    
    // Command history navigation
    var commandHistoryList by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }
    var tempCommand by remember { mutableStateOf("") }
    
    // Load recent commands from database
    LaunchedEffect(Unit) {
        try {
            val recentCommands = viewModel.getRecentCommands(50)
            commandHistoryList = recentCommands
        } catch (e: Exception) {
            // Handle error silently - fall back to empty list
        }
    }
    
    // Function to handle command history navigation
    fun navigateHistory(direction: Int) {
        if (commandHistoryList.isEmpty()) return
        
        when (direction) {
            -1 -> { // Up arrow - go to previous command
                if (historyIndex == -1) {
                    tempCommand = currentCommand
                    historyIndex = commandHistoryList.size - 1
                } else if (historyIndex > 0) {
                    historyIndex--
                }
                if (historyIndex >= 0) {
                    currentCommand = commandHistoryList[historyIndex]
                }
            }
            1 -> { // Down arrow - go to next command
                if (historyIndex >= 0) {
                    historyIndex++
                    if (historyIndex >= commandHistoryList.size) {
                        historyIndex = -1
                        currentCommand = tempCommand
                    } else {
                        currentCommand = commandHistoryList[historyIndex]
                    }
                }
            }
        }
    }
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
            .fillMaxWidth(0.98f) // Use almost full width with minimal spacing
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
                    onValueChange = { 
                        currentCommand = it
                        historyIndex = -1 // Reset history navigation when typing
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionUp -> {
                                        navigateHistory(-1)
                                        true
                                    }
                                    Key.DirectionDown -> {
                                        navigateHistory(1)
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        },
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
                                
                                // Add command to history (exclude clear/cls commands)
                                if (command.lowercase().trim() !in listOf("clear", "cls")) {
                                    commandHistoryList = commandHistoryList + command
                                }
                                historyIndex = -1
                                tempCommand = ""
                                
                                // Handle commands
                                coroutineScope.launch {
                                    val startTime = System.currentTimeMillis()
                                    
                                    // Handle clear/cls commands immediately
                                    if (command.lowercase().trim() in listOf("clear", "cls")) {
                                        commandHistory = emptyList()
                                        isProcessing = false
                                        
                                        // Save clear command to database
                                        val executionTime = System.currentTimeMillis() - startTime
                                        viewModel.saveCommandHistory(
                                            command = command,
                                            commandType = "SYSTEM",
                                            subCommand = "clear",
                                            executionTimeMs = executionTime,
                                            success = true,
                                            output = "Screen cleared"
                                        )
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
                                            
                                            // Save AI command to database (handled in processAICommand)
                                            val executionTime = System.currentTimeMillis() - startTime
                                            viewModel.saveCommandHistory(
                                                command = command,
                                                commandType = "AI",
                                                subCommand = if (command.startsWith("ai")) "ai" else "ask",
                                                arguments = prompt,
                                                executionTimeMs = executionTime,
                                                success = true,
                                                output = aiResponse
                                            )
                                        } else {
                                            val errorMsg = "Error: Please provide a prompt after '${command.split(" ")[0]}' command"
                                            commandHistory = commandHistory + CommandEntry(
                                                command = command,
                                                output = errorMsg,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            
                                            // Save error to database
                                            val executionTime = System.currentTimeMillis() - startTime
                                            viewModel.saveCommandHistory(
                                                command = command,
                                                commandType = "AI",
                                                subCommand = if (command.startsWith("ai")) "ai" else "ask",
                                                executionTimeMs = executionTime,
                                                success = false,
                                                output = errorMsg
                                            )
                                        }
                                    } else {
                                        // Handle regular commands using command execution engine
                                        val result = try {
                                            val commandResult = viewModel.executeNewCommand(context, command)
                                            
                                            // Special handling for clear command
                                            if (commandResult == "CLEAR_SCREEN") {
                                                commandHistory = emptyList()
                                                return@launch
                                            }
                                            
                                            commandResult
                                        } catch (e: Exception) {
                                            "Error: ${e.message}"
                                        }
                                        
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
                
                // Navigation buttons for command history (mobile-friendly)
                if (commandHistoryList.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(start = LayoutConstants.SPACING_SMALL),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Up arrow button
                        IconButton(
                            onClick = { navigateHistory(-1) },
                            enabled = !isProcessing,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous command",
                                tint = if (isProcessing) Color.Gray else Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // Down arrow button
                        IconButton(
                            onClick = { navigateHistory(1) },
                            enabled = !isProcessing,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next command",
                                tint = if (isProcessing) Color.Gray else Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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









@HiltViewModel
class CommandPromptViewModel @Inject constructor(
    private val aiService: AIService,
    private val commandLineRepository: CommandLineRepository,
    private val commandExecutionEngine: CommandExecutionEngine
) : ViewModel() {
    
    private val sessionId = commandLineRepository.generateSessionId()
    
    suspend fun processAICommand(prompt: String): String {
        val startTime = System.currentTimeMillis()
        return try {
            val response = aiService.generateResponse(prompt)
            val processingTime = System.currentTimeMillis() - startTime
            
            // Save AI conversation to database
            commandLineRepository.insertAIConversation(
                sessionId = sessionId,
                prompt = prompt,
                response = if (response.success) response.response else "AI Error: ${response.response}",
                modelUsed = "Gemma",
                processingTimeMs = processingTime,
                conversationType = "COMMAND_CHAT"
            )
            
            if (response.success) {
                response.response
            } else {
                "AI Error: ${response.response}"
            }
        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            
            // Save error to database
            commandLineRepository.insertAIConversation(
                sessionId = sessionId,
                prompt = prompt,
                response = "AI Error: ${e.message}",
                modelUsed = "Gemma",
                processingTimeMs = processingTime,
                conversationType = "COMMAND_CHAT"
            )
            
            "AI Error: ${e.message}"
        }
    }
    
    suspend fun saveCommandHistory(
        command: String,
        commandType: String,
        subCommand: String? = null,
        arguments: String? = null,
        executionTimeMs: Long,
        success: Boolean,
        output: String
    ) {
        val outputPreview = if (output.length > 200) {
            output.substring(0, 200) + "..."
        } else {
            output
        }
        
        val commandId = commandLineRepository.insertCommandHistory(
            command = command,
            commandType = commandType,
            subCommand = subCommand,
            arguments = arguments,
            sessionId = sessionId,
            executionTimeMs = executionTimeMs,
            success = success,
            outputPreview = outputPreview
        )
        
        // If output is large, save it separately
        if (output.length > 200) {
            val outputId = commandLineRepository.insertCommandOutput(
                commandId = commandId,
                fullOutput = output,
                outputType = "TEXT"
            )
            // Update command history with output reference
            // Note: This would require an update method in the repository
        }
        
        // Update command usage statistics
        commandLineRepository.updateCommandUsage(
            command = command,
            category = commandType,
            executionTimeMs = executionTimeMs,
            success = success
        )
    }
    
    suspend fun getRecentCommands(limit: Int = 50): List<String> {
        return commandLineRepository.getRecentCommands(limit)
    }
    
    suspend fun getCommandSuggestions(pattern: String): List<String> {
        return commandExecutionEngine.getCommandSuggestions(pattern)
    }
    
    suspend fun executeNewCommand(context: android.content.Context, command: String): String {
        return try {
            val result = commandExecutionEngine.executeCommand(context, command, sessionId)
            result.output
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    fun getCommandHelp(commandName: String): String {
        return commandExecutionEngine.getCommandHelp(commandName)
    }
    
    fun getAllCommands(): List<com.win11launcher.command.CommandDefinition> {
        return commandExecutionEngine.getAllCommands()
    }
}

