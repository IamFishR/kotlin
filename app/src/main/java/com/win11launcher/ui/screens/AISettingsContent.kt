package com.win11launcher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import com.win11launcher.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsContent(
    viewModel: SettingsViewModel = hiltViewModel(),
    aiService: AIService,
    aiMemoryManager: AIMemoryManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // AI Settings State
    var aiModelStatus by remember { mutableStateOf("Checking...") }
    var modelLoadingProgress by remember { mutableStateOf(0f) }
    var isModelLoaded by remember { mutableStateOf(false) }
    var memoryStats by remember { mutableStateOf<com.win11launcher.services.MemoryStatistics?>(null) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    
    // Settings from ViewModel - using simple state management for now
    var aiEnabled by remember { mutableStateOf(true) }
    var aiMemoryEnabled by remember { mutableStateOf(true) }
    var aiContextLength by remember { mutableIntStateOf(20) }
    var aiResponseSpeed by remember { mutableStateOf("balanced") }
    var aiPersonality by remember { mutableStateOf("helpful") }
    var aiDataRetention by remember { mutableIntStateOf(30) }
    
    // Load AI status on startup
    LaunchedEffect(Unit) {
        try {
            val initResult = aiService.initializeModel()
            isModelLoaded = initResult.success
            aiModelStatus = if (initResult.success) {
                "Gemma 3N Model Loaded"
            } else {
                initResult.error ?: "Model not available"
            }
            
            memoryStats = aiMemoryManager.getMemoryStatistics("USER_DEFAULT")
        } catch (e: Exception) {
            aiModelStatus = "Error: ${e.message}"
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
        AIStatusCard(
            modelStatus = aiModelStatus,
            isModelLoaded = isModelLoaded,
            memoryStats = memoryStats,
            onModelInfoClick = { showModelDialog = true },
            onMemoryInfoClick = { showMemoryDialog = true }
        )
        
        // AI Model Settings
        SettingsGroup(title = "AI Model", description = "Configure AI assistant settings") {
            SettingsItem(
                title = "AI Assistant",
                description = if (aiEnabled) "Enabled" else "Disabled",
                onClick = { aiEnabled = !aiEnabled }
            )
            
            if (aiEnabled) {
                SettingsItem(
                    title = "Response Speed",
                    description = aiResponseSpeed.replaceFirstChar { it.uppercase() },
                    onClick = {
                        // Show speed selection dialog
                    }
                )
                
                SettingsItem(
                    title = "AI Personality",
                    description = aiPersonality.replaceFirstChar { it.uppercase() },
                    onClick = {
                        // Show personality selection dialog
                    }
                )
            }
        }
        
        // Memory & Context Settings
        if (aiEnabled) {
            SettingsGroup(title = "Memory & Context", description = "Configure AI memory and context settings") {
                SettingsItem(
                    title = "AI Memory",
                    description = if (aiMemoryEnabled) "Remembers conversations" else "No memory",
                    onClick = { aiMemoryEnabled = !aiMemoryEnabled }
                )
                
                if (aiMemoryEnabled) {
                    SettingsItem(
                        title = "Context Length",
                        description = "$aiContextLength messages",
                        onClick = {
                            // Show context length slider dialog
                        }
                    )
                    
                    SettingsItem(
                        title = "Memory Cleanup",
                        description = "Manage AI memory and reflections",
                        onClick = {
                            scope.launch {
                                try {
                                    aiMemoryManager.performMemoryMaintenance("USER_DEFAULT")
                                    // Show success message
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        }
                    )
                }
            }
        }
        
        // Privacy & Data Settings
        if (aiEnabled) {
            SettingsGroup(title = "Privacy & Data", description = "Manage AI data and privacy settings") {
                SettingsItem(
                    title = "Data Retention",
                    description = "$aiDataRetention days",
                    onClick = {
                        // Show retention period dialog
                    }
                )
                
                SettingsItem(
                    title = "Clear All AI Data",
                    description = "Remove all conversations and memories",
                    onClick = {
                        // Show confirmation dialog
                    }
                )
            }
        }
        
        // AI Commands Reference
        SettingsGroup(title = "AI Commands", description = "Available AI commands and usage examples") {
            SettingsItem(
                title = "Command Reference",
                description = "View all available AI commands and examples",
                onClick = { /* Show command reference */ }
            )
        }
        
        // AI Performance Metrics
        if (aiEnabled && isModelLoaded) {
            SettingsGroup(title = "Performance", description = "AI performance metrics and statistics") {
                SettingsItem(
                    title = "Performance Metrics",
                    description = "View AI performance statistics and memory usage",
                    onClick = { /* Show performance details */ }
                )
            }
        }
    }
    
    // Model Info Dialog
    if (showModelDialog) {
        AIModelInfoDialog(
            onDismiss = { showModelDialog = false },
            modelStatus = aiModelStatus,
            isLoaded = isModelLoaded
        )
    }
    
    // Memory Info Dialog
    if (showMemoryDialog && memoryStats != null) {
        AIMemoryInfoDialog(
            onDismiss = { showMemoryDialog = false },
            memoryStats = memoryStats!!
        )
    }
}

@Composable
fun AIStatusCard(
    modelStatus: String,
    isModelLoaded: Boolean,
    memoryStats: com.win11launcher.services.MemoryStatistics?,
    onModelInfoClick: () -> Unit,
    onMemoryInfoClick: () -> Unit
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
                    imageVector = if (isModelLoaded) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isModelLoaded) Color(0xFF4CAF50) else Color(0xFFFFA500)
                )
            }
            
            // Model Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Model",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = modelStatus,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                
                TextButton(onClick = onModelInfoClick) {
                    Text("Details", color = Color(0xFF0078D4))
                }
            }
            
            // Memory Status
            if (memoryStats != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Memory",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${memoryStats.longTermMemoryCount} memories, ${memoryStats.reflectionCount} insights",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    
                    TextButton(onClick = onMemoryInfoClick) {
                        Text("Manage", color = Color(0xFF0078D4))
                    }
                }
            }
        }
    }
}

@Composable
fun AICommandReference() {
    val commands = listOf(
        "ask" to "Ask AI questions with context and memory",
        "memory" to "Manage AI memory (show, search, stats, cleanup)",
        "interpret" to "Convert natural language to commands",
        "analyze" to "AI-powered system analysis",
        "optimize" to "Get intelligent optimization suggestions",
        "suggest" to "Smart command recommendations",
        "script" to "AI automation script examples"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        commands.forEach { (command, description) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = command,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0078D4),
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "💡 Use 'commands --category=ai' to see all AI commands with examples",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AIPerformanceMetrics(
    memoryStats: com.win11launcher.services.MemoryStatistics?
) {
    if (memoryStats == null) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricRow(
            label = "Memory Efficiency",
            value = "${(memoryStats.averageImportance * 100).toInt()}%",
            icon = Icons.Default.TrendingUp
        )
        
        MetricRow(
            label = "Long-term Memories",
            value = memoryStats.longTermMemoryCount.toString(),
            icon = Icons.Default.Storage
        )
        
        MetricRow(
            label = "Generated Insights",
            value = memoryStats.reflectionCount.toString(),
            icon = Icons.Default.Lightbulb
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0078D4),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// TODO: Implement proper AI settings integration with ViewModel
// For now using local state to avoid compilation issues