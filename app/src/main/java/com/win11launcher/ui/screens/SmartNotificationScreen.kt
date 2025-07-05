package com.win11launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.win11launcher.data.entities.Note
import com.win11launcher.ui.components.AppIcon
import com.win11launcher.ui.theme.Win11Colors
import com.win11launcher.viewmodels.SmartNotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartNotificationScreen(
    viewModel: SmartNotificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val groupedNotes by viewModel.groupedNotes.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Win11Colors.SystemBackground)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Win11Colors.SystemAccent.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Win11Colors.SystemAccent
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Win11Colors.SystemAccent
                    )
                    Text(
                        text = "${uiState.totalNotes} notifications • ${groupedNotes.size} apps",
                        fontSize = 14.sp,
                        color = Win11Colors.TextSecondary
                    )
                }
                
                // Actions
                Row {
                    IconButton(
                        onClick = { viewModel.removeDuplicates() }
                    ) {
                        Icon(
                            Icons.Default.CleaningServices,
                            contentDescription = "Remove Duplicates",
                            tint = Win11Colors.SystemAccent
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshNotes() }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Win11Colors.SystemAccent
                        )
                    }
                }
            }
        }
        
        // Loading State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Win11Colors.SystemAccent)
            }
            return@Column
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groupedNotes) { appGroup ->
                AppNotificationGroup(
                    appGroup = appGroup,
                    onNotificationClick = { note ->
                        viewModel.selectNotification(note)
                    },
                    onCreateRule = { packageName, appName ->
                        viewModel.createRuleForApp(packageName, appName)
                    },
                    onRemoveDuplicatesForApp = { packageName ->
                        viewModel.removeDuplicatesForApp(packageName)
                    },
                    onDeleteNotification = viewModel::deleteNotification
                )
            }
        }
    }
    
    // Notification Detail Dialog
    uiState.selectedNotification?.let { note ->
        NotificationDetailDialog(
            note = note,
            onDismiss = { viewModel.dismissNotificationDetail() },
            onCreateRule = { 
                viewModel.createRuleForNotification(note)
                viewModel.dismissNotificationDetail()
            },
            onDeleteNotification = {
                viewModel.deleteNotification(note)
                viewModel.dismissNotificationDetail()
            }
        )
    }
}

@Composable
fun AppNotificationGroup(
    appGroup: AppNotificationGroup,
    onNotificationClick: (Note) -> Unit,
    onCreateRule: (String, String) -> Unit,
    onRemoveDuplicatesForApp: (String) -> Unit,
    onDeleteNotification: (Note) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Win11Colors.SystemBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    packageName = appGroup.packageName,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appGroup.appName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Win11Colors.TextPrimary
                    )
                    Text(
                        text = "${appGroup.notes.size} notifications",
                        fontSize = 12.sp,
                        color = Win11Colors.TextSecondary
                    )
                }
                
                // App Actions
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Win11Colors.SystemAccent
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Create Rule") },
                            leadingIcon = { Icon(Icons.Default.Rule, contentDescription = null) },
                            onClick = {
                                onCreateRule(appGroup.packageName, appGroup.appName)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Duplicates") },
                            leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                            onClick = {
                                onRemoveDuplicatesForApp(appGroup.packageName)
                                showMenu = false
                            }
                        )
                    }
                }
                
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Win11Colors.SystemAccent
                    )
                }
            }
            
            // Notification List
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appGroup.notes.take(10).forEach { note ->
                        NotificationItem(
                            note = note,
                            onClick = { onNotificationClick(note) },
                            onDelete = { onDeleteNotification(note) }
                        )
                    }
                    
                    if (appGroup.notes.size > 10) {
                        Text(
                            text = "... and ${appGroup.notes.size - 10} more",
                            fontSize = 12.sp,
                            color = Win11Colors.TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Win11Colors.SystemBackground.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Win11Colors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = note.content,
                    fontSize = 12.sp,
                    color = Win11Colors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = java.text.SimpleDateFormat("MMM dd, HH:mm").format(note.createdAt),
                    fontSize = 10.sp,
                    color = Win11Colors.TextSecondary.copy(alpha = 0.7f)
                )
            }
            
            if (note.ruleId != null) {
                Icon(
                    Icons.Default.Rule,
                    contentDescription = "Has rule",
                    tint = Win11Colors.SystemAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Win11Colors.TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Win11Colors.TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Win11Colors.TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Win11Colors.TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Win11Colors.TextSecondary
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete notification",
                    tint = Win11Colors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun NotificationDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onCreateRule: () -> Unit,
    onDeleteNotification: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = Win11Colors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "From: ${note.sourceAppName}",
                    fontSize = 12.sp,
                    color = Win11Colors.TextSecondary
                )
                
                Text(
                    text = "Time: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(note.createdAt)}",
                    fontSize = 12.sp,
                    color = Win11Colors.TextSecondary
                )
                
                if (note.tags.isNotEmpty()) {
                    Text(
                        text = "Tags: ${note.tags}",
                        fontSize = 12.sp,
                        color = Win11Colors.TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = onCreateRule,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Win11Colors.SystemAccent
                    )
                ) {
                    Text("Create Rule")
                }
                
                TextButton(
                    onClick = onDeleteNotification,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Win11Colors.TextSecondary
                )
            ) {
                Text("Close")
            }
        }
    )
}

data class AppNotificationGroup(
    val packageName: String,
    val appName: String,
    val notes: List<Note>
)