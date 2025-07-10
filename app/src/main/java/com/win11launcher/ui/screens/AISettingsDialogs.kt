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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.win11launcher.services.MemoryStatistics
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIModelInfoDialog(
    onDismiss: () -> Unit,
    modelStatus: String,
    isLoaded: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Model Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                Divider(color = Color.Gray)
                
                // Model Status
                InfoSection(
                    title = "Model Status",
                    items = listOf(
                        "Status" to modelStatus,
                        "Loaded" to if (isLoaded) "Yes" else "No",
                        "Type" to "Gemma 3N (Instruction Tuned)",
                        "Quantization" to "INT4 (4-bit)"
                    )
                )
                
                // Model Specifications
                if (isLoaded) {
                    InfoSection(
                        title = "Model Specifications",
                        items = listOf(
                            "Model Size" to "4.41 GB",
                            "Parameters" to "~3 Billion",
                            "Context Window" to "8,192 tokens",
                            "Framework" to "MediaPipe LLM",
                            "Inference" to "On-device"
                        )
                    )
                } else {
                    InfoSection(
                        title = "Model Requirements",
                        items = listOf(
                            "File Path" to "/storage/emulated/0/Models/gemma-3n-E4B-it-int4.task",
                            "File Size" to "4.41 GB",
                            "Permission" to "Storage access required",
                            "Memory" to "6+ GB RAM recommended",
                            "Storage" to "5+ GB free space"
                        )
                    )
                }
                
                // Capabilities
                InfoSection(
                    title = "AI Capabilities",
                    items = listOf(
                        "Natural Language" to "Question answering, explanations",
                        "Command Generation" to "Convert speech to commands",
                        "System Analysis" to "Performance insights",
                        "Memory Management" to "Context-aware conversations",
                        "Pattern Recognition" to "Usage optimization"
                    )
                )
                
                // Setup Instructions (if not loaded)
                if (!isLoaded) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF404040))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF0078D4)
                                )
                                Text(
                                    text = "Setup Instructions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Text(
                                text = "To enable full AI features:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = "1. Download the Gemma 3N model file\n" +
                                      "2. Place it in /storage/emulated/0/Models/\n" +
                                      "3. Grant storage permissions\n" +
                                      "4. Restart the app",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color(0xFF0078D4))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMemoryInfoDialog(
    onDismiss: () -> Unit,
    memoryStats: MemoryStatistics
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Memory Statistics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                Divider(color = Color.Gray)
                
                // Memory Overview
                InfoSection(
                    title = "Memory Overview",
                    items = listOf(
                        "Long-term Memories" to memoryStats.longTermMemoryCount.toString(),
                        "Reflections Generated" to memoryStats.reflectionCount.toString(),
                        "Average Importance" to "${(memoryStats.averageImportance * 100).toInt()}%",
                        "Memory Efficiency" to "Optimized"
                    )
                )
                
                // Memory Types Distribution
                if (memoryStats.memoryTypeDistribution.isNotEmpty()) {
                    InfoSection(
                        title = "Memory Types",
                        items = memoryStats.memoryTypeDistribution.map { stat ->
                            stat.memory_type to stat.count.toString()
                        }
                    )
                }
                
                // Reflection Types
                if (memoryStats.reflectionTypeDistribution.isNotEmpty()) {
                    InfoSection(
                        title = "Insights Generated",
                        items = memoryStats.reflectionTypeDistribution.map { stat ->
                            stat.reflection_type to stat.count.toString()
                        }
                    )
                }
                
                // Memory Management Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF404040))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = Color(0xFF0078D4)
                            )
                            Text(
                                text = "How AI Memory Works",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Text(
                            text = "• Recent conversations stored in short-term memory\n" +
                                  "• Important messages promoted to long-term storage\n" +
                                  "• AI generates insights from conversation patterns\n" +
                                  "• Context survives app restarts\n" +
                                  "• Automatic cleanup prevents memory bloat",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Management Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Clear memories */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Memories", color = Color.White)
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0078D4)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF404040))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsSelectionDialog(
    title: String,
    options: List<String>,
    currentSelection: String,
    onSelectionChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentSelection,
                            onClick = { onSelectionChanged(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF0078D4),
                                unselectedColor = Color.Gray
                            )
                        )
                        Text(
                            text = option.replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}