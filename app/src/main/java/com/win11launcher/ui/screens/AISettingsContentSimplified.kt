package com.win11launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.win11launcher.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsContentSimplified(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // AI Model Status Check
    var modelStatus by remember { mutableStateOf("Checking...") }
    var isModelAvailable by remember { mutableStateOf(false) }
    var showAICommandsDialog by remember { mutableStateOf(false) }
    
    // Check for AI model on startup
    LaunchedEffect(Unit) {
        try {
            val modelPath = "/storage/emulated/0/Models/gemma-3n-E4B-it-int4.task"
            val modelFile = File(modelPath)
            isModelAvailable = modelFile.exists()
            modelStatus = if (isModelAvailable) {
                "Gemma 3N Model Available (${String.format("%.2f GB", modelFile.length() / (1024.0 * 1024.0 * 1024.0))})"
            } else {
                "AI Model Not Found"
            }
        } catch (e: Exception) {
            modelStatus = "Unable to check model status"
            isModelAvailable = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // AI System Status Card
        AISystemStatusCard(
            modelStatus = modelStatus,
            isModelAvailable = isModelAvailable
        )
        
        // AI Commands Information
        SettingsGroup(title = "AI Commands", description = "Available AI commands and their usage") {
            SettingsItem(
                title = "ask",
                description = "Ask AI questions with context and memory",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "memory",
                description = "Manage AI memory (show, search, stats, cleanup)",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "interpret",
                description = "Convert natural language to commands",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "analyze",
                description = "AI-powered system analysis",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "optimize",
                description = "Get intelligent optimization suggestions",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "suggest",
                description = "Smart command recommendations",
                onClick = { showAICommandsDialog = true }
            )
            
            SettingsItem(
                title = "script",
                description = "AI automation script examples",
                onClick = { showAICommandsDialog = true }
            )
        }
        
        // AI Features Information
        SettingsGroup(title = "AI Features", description = "Available AI capabilities and features") {
            SettingsItem(
                title = "Context-Aware Conversations",
                description = if (isModelAvailable) "AI remembers your conversations across app sessions" else "Requires AI model",
                onClick = { }
            )
            
            SettingsItem(
                title = "Memory Management", 
                description = "Smart context storage with automatic cleanup",
                onClick = { }
            )
            
            SettingsItem(
                title = "Natural Language Processing",
                description = if (isModelAvailable) "Convert plain English to executable commands" else "Requires AI model",
                onClick = { }
            )
            
            SettingsItem(
                title = "Intelligent Suggestions",
                description = "Smart command recommendations based on context",
                onClick = { }
            )
            
            SettingsItem(
                title = "Pattern Recognition",
                description = if (isModelAvailable) "AI learns from your usage patterns" else "Requires AI model",
                onClick = { }
            )
        }
        
        // AI Model Setup Information
        if (!isModelAvailable) {
            SettingsGroup(title = "Setup Instructions", description = "How to enable full AI features") {
                SettingsItem(
                    title = "Enable Full AI Features",
                    description = "Download the Gemma 3N model file and place it in /storage/emulated/0/Models/",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Model Requirements",
                    description = "4.41 GB file size, 6+ GB RAM recommended, storage permissions required",
                    onClick = { }
                )
            }
        }
        
        // AI Memory Statistics (Simplified)
        SettingsGroup(title = "AI Memory", description = "Memory management and statistics") {
            SettingsItem(
                title = "Memory Management",
                description = "Context survives app restarts, automatic cleanup prevents memory bloat",
                onClick = { }
            )
            
            SettingsItem(
                title = "Memory Limits",
                description = "Maximum 50 recent messages + 8,000 tokens per conversation",
                onClick = { }
            )
        }
    }
    
    // AI Commands Dialog
    if (showAICommandsDialog) {
        AICommandsDialog(
            onDismiss = { showAICommandsDialog = false }
        )
    }
}

@Composable
fun AISystemStatusCard(
    modelStatus: String,
    isModelAvailable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF404040))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI System Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Icon(
                    imageVector = if (isModelAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isModelAvailable) Color.Green else Color(0xFFFFA500)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFF0078D4),
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = modelStatus,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (isModelAvailable) {
                            "Full AI features available"
                        } else {
                            "Basic AI features only"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICommandsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "AI Commands Reference",
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val commands = listOf(
                    Triple("ask \"<question>\"", "Ask AI with context", "ask \"How to save battery?\""),
                    Triple("memory show", "View AI memory status", "memory stats"),
                    Triple("interpret \"<request>\"", "Natural language to commands", "interpret \"show battery status\""),
                    Triple("analyze", "AI system analysis", "analyze --category=battery"),
                    Triple("optimize", "Get optimization tips", "optimize --category=performance"),
                    Triple("suggest", "Smart recommendations", "suggest --context=battery_low"),
                    Triple("script generate", "AI automation examples", "script examples")
                )
                
                commands.forEach { (command, description, example) ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = command,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0078D4)
                        )
                        Text(
                            text = description,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Example: $example",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Text(
                    text = "💡 Use 'commands --category=ai' for complete command list",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF0078D4))
            }
        },
        containerColor = Color(0xFF2D2D2D),
        textContentColor = Color.White
    )
}

