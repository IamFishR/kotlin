package com.win11launcher.ui.screens.noteshub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.win11launcher.data.entities.TrackingRule
import com.win11launcher.data.entities.Folder
import com.win11launcher.data.models.FilterType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    rules: List<TrackingRule>,
    folders: List<Folder>,
    onRuleToggle: (String, Boolean) -> Unit,
    onRuleEdit: (String) -> Unit,
    onRuleDelete: (String) -> Unit,
    onCreateNewRule: () -> Unit,
    onRuleDetails: (String) -> Unit,
    onViewNotes: () -> Unit = {},
    onAllNotifications: () -> Unit = {},
) {
    val activeRules = rules.filter { it.isActive }
    val inactiveRules = rules.filter { !it.isActive }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp) // Extra bottom padding for taskbar
        ) {
        // Header with just the stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeRules.size} active • ${inactiveRules.size} inactive",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Total Rules",
                value = rules.size.toString(),
                icon = Icons.AutoMirrored.Filled.Rule,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            StatsCard(
                title = "Notes Created",
                value = rules.sumOf { it.notesCapturedCount }.toString(),
                icon = Icons.AutoMirrored.Filled.Note,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            
            StatsCard(
                title = "Success Rate",
                value = calculateSuccessRate(rules),
                icon = Icons.Default.Analytics,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onViewNotes,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View notes",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Notes")
            }
            
            OutlinedButton(
                onClick = onAllNotifications,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "All notifications",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("All Notifications")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rules list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeRules.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(activeRules) { rule ->
                    RuleCard(
                        rule = rule,
                        folder = folders.find { it.id == rule.destinationFolderId },
                        onToggle = { onRuleToggle(rule.id, !rule.isActive) },
                        onEdit = { onRuleEdit(rule.id) },
                        onDelete = { onRuleDelete(rule.id) },
                        onDetails = { onRuleDetails(rule.id) }
                    )
                }
            }
            
            if (inactiveRules.isNotEmpty()) {
                item {
                    Text(
                        text = "Inactive Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(inactiveRules) { rule ->
                    RuleCard(
                        rule = rule,
                        folder = folders.find { it.id == rule.destinationFolderId },
                        onToggle = { onRuleToggle(rule.id, !rule.isActive) },
                        onEdit = { onRuleEdit(rule.id) },
                        onDelete = { onRuleDelete(rule.id) },
                        onDetails = { onRuleDetails(rule.id) },
                        isInactive = true
                    )
                }
            }
            
            if (rules.isEmpty()) {
                item {
                    EmptyStateCard(onCreateNewRule = onCreateNewRule)
                }
            }
        }
        }
        
        // Always visible floating action button at bottom right
        if (rules.isNotEmpty()) {
            FloatingActionButton(
                onClick = onCreateNewRule,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 96.dp), // Above taskbar
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new rule",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleCard(
    rule: TrackingRule,
    folder: Folder?,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    isInactive: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with rule name and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInactive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (rule.description.isNotEmpty()) {
                        Text(
                            text = rule.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (rule.isActive) "Disable" else "Enable") },
                            onClick = {
                                onToggle()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (rule.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Details") },
                            onClick = {
                                onDetails()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rule details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filter type
                RuleDetailChip(
                    label = "Filter",
                    value = getFilterTypeDisplay(rule.filterType),
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Destination folder
                folder?.let {
                    RuleDetailChip(
                        label = "Folder",
                        value = it.name,
                        color = Color(android.graphics.Color.parseColor(it.color))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Notes Created",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rule.notesCapturedCount.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInactive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Column {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = calculateRuleSuccessRate(rule),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInactive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Column {
                    Text(
                        text = "Last Triggered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatLastTriggered(rule.lastTriggeredAt),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInactive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleDetailChip(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun EmptyStateCard(
    onCreateNewRule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Rule,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tracking rules yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Create your first rule to start automatically converting notifications into organized notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCreateNewRule) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Rule")
            }
        }
    }
}

private fun getFilterTypeDisplay(filterType: String): String {
    return when (FilterType.valueOf(filterType)) {
        FilterType.ALL -> "All"
        FilterType.KEYWORD_INCLUDE -> "Include"
        FilterType.KEYWORD_EXCLUDE -> "Exclude"
        FilterType.REGEX -> "Regex"
    }
}

private fun calculateSuccessRate(rules: List<TrackingRule>): String {
    val totalMatches = rules.sumOf { it.totalMatchesCount }
    val totalNotes = rules.sumOf { it.notesCapturedCount }
    
    return if (totalMatches > 0) {
        "${(totalNotes * 100 / totalMatches).toInt()}%"
    } else {
        "N/A"
    }
}

private fun calculateRuleSuccessRate(rule: TrackingRule): String {
    return if (rule.totalMatchesCount > 0) {
        "${(rule.notesCapturedCount * 100 / rule.totalMatchesCount).toInt()}%"
    } else {
        "N/A"
    }
}

private fun formatLastTriggered(timestamp: Long?): String {
    if (timestamp == null) return "Never"
    
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}