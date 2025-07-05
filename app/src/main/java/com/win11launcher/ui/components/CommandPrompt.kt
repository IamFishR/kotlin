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
            .clip(RoundedCornerShape(8.dp)),
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
                    text = "C:\\> ",
                    color = Color.White,
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
                                val result = executeCommand(command)
                                commandHistory = commandHistory + CommandEntry(
                                    command = command,
                                    output = result,
                                    timestamp = System.currentTimeMillis()
                                )
                                currentCommand = ""
                                isProcessing = false
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
            text = "C:\\> ${entry.command}",
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        
        // Command output
        if (entry.output.isNotBlank()) {
            Text(
                text = entry.output,
                color = Color(0xFFCCCCCC),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private data class CommandEntry(
    val command: String,
    val output: String,
    val timestamp: Long
)

private fun executeCommand(command: String): String {
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
"""
        }
        "clear", "cls" -> {
            "Screen cleared." // Note: actual clearing would be handled by the UI
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