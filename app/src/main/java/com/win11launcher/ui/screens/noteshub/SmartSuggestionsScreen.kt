package com.win11launcher.ui.screens.noteshub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.win11launcher.data.entities.*
import com.win11launcher.viewmodels.NotesHubViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSuggestionsScreen(
    viewModel: NotesHubViewModel,
    onSuggestionApplied: (String) -> Unit = {},
    onSuggestionDismissed: (String) -> Unit = {}
) {
    val suggestions by viewModel.smartSuggestions.collectAsState()
    val isLoading by viewModel.isLoadingSuggestions.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("ALL") }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp)
    ) {
        // Stats Cards Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                StatsCard(
                    title = "Smart Suggestions",
                    value = suggestions.size.toString(),
                    icon = Icons.Default.Lightbulb,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                StatsCard(
                    title = "High Priority",
                    value = suggestions.count { it.priority == SuggestionPriority.HIGH }.toString(),
                    icon = Icons.Default.PriorityHigh,
                    color = MaterialTheme.colorScheme.error
                )
            }
            item {
                StatsCard(
                    title = "General",
                    value = suggestions.count { it.category == SuggestionCategory.GENERAL }.toString(),
                    icon = Icons.Default.Category,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "ALL",
                    onClick = { selectedCategory = "ALL" },
                    label = { Text("All") }
                )
            }
            
            val categories = suggestions.map { it.category }.distinct()
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(getCategoryDisplayName(category)) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Suggestions List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val filteredSuggestions = if (selectedCategory == "ALL") {
                suggestions
            } else {
                suggestions.filter { it.category == selectedCategory }
            }
            
            if (filteredSuggestions.isEmpty()) {
                EmptyState(selectedCategory)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSuggestions) { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            onApply = {
                                scope.launch {
                                    viewModel.applySuggestion(suggestion.id)
                                    onSuggestionApplied(suggestion.id)
                                }
                            },
                            onDismiss = {
                                scope.launch {
                                    viewModel.dismissSuggestion(suggestion.id)
                                    onSuggestionDismissed(suggestion.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionCard(
    suggestion: SmartSuggestion,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with category badge and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(
                        category = suggestion.category
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityIndicator(priority = suggestion.priority)
                }
                ConfidenceScore(score = suggestion.confidenceScore)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title and description
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Expected benefit
            suggestion.expectedBenefit?.let { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Rule")
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String
) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = getCategoryDisplayName(category),
                fontSize = 12.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
private fun PriorityIndicator(priority: Int) {
    val (icon, color, text) = when (priority) {
        SuggestionPriority.HIGH -> Triple(Icons.Default.PriorityHigh, MaterialTheme.colorScheme.error, "High")
        SuggestionPriority.MEDIUM -> Triple(Icons.Default.Remove, MaterialTheme.colorScheme.primary, "Medium")
        else -> Triple(Icons.Default.KeyboardArrowDown, MaterialTheme.colorScheme.outline, "Low")
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ConfidenceScore(score: Float) {
    val percentage = (score * 100).toInt()
    val color = when {
        percentage >= 80 -> MaterialTheme.colorScheme.tertiary
        percentage >= 60 -> MaterialTheme.colorScheme.primary  
        else -> MaterialTheme.colorScheme.outline
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${percentage}%",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyState(category: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (category == "ALL") "No smart suggestions available" else "No $category suggestions",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Use the app more to get intelligent suggestions!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// Helper functions
private fun getCategoryDisplayName(category: String): String {
    return when (category) {
        SuggestionCategory.FINANCE -> "Finance"
        SuggestionCategory.INVESTMENT -> "Investment"
        SuggestionCategory.RESEARCH -> "Research"
        SuggestionCategory.MARKET_NEWS -> "Market News"
        SuggestionCategory.PRODUCTIVITY -> "Productivity"
        SuggestionCategory.ORGANIZATION -> "Organization"
        SuggestionCategory.GENERAL -> "General"
        SuggestionCategory.COMMUNICATION -> "Communication"
        SuggestionCategory.REMINDER -> "Reminder"
        else -> category.lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        SuggestionCategory.FINANCE -> Icons.Default.AccountBalance
        SuggestionCategory.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
        SuggestionCategory.RESEARCH -> Icons.Default.Science
        SuggestionCategory.MARKET_NEWS -> Icons.Default.Newspaper
        SuggestionCategory.PRODUCTIVITY -> Icons.Default.Speed
        SuggestionCategory.ORGANIZATION -> Icons.Default.FolderOpen
        SuggestionCategory.GENERAL -> Icons.Default.Lightbulb
        SuggestionCategory.COMMUNICATION -> Icons.Default.Chat
        SuggestionCategory.REMINDER -> Icons.Default.Alarm
        else -> Icons.Default.Lightbulb
    }
}