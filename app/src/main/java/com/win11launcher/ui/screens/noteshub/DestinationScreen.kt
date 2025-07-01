package com.win11launcher.ui.screens.noteshub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.win11launcher.data.entities.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationScreen(
    availableFolders: List<Folder>,
    selectedFolderId: String?,
    onFolderSelected: (String) -> Unit,
    newFolderName: String,
    onNewFolderNameChanged: (String) -> Unit,
    newFolderDescription: String,
    onNewFolderDescriptionChanged: (String) -> Unit,
    selectedFolderColor: String,
    onFolderColorSelected: (String) -> Unit,
    selectedFolderIcon: String,
    onFolderIconSelected: (String) -> Unit,
    autoTags: List<String>,
    onAutoTagsChanged: (List<String>) -> Unit,
    enableAutoNaming: Boolean,
    onAutoNamingChanged: (Boolean) -> Unit,
    onCreateFolder: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var showCreateFolder by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column {
            Text(
                text = "Destination & Organization",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose where to save notes and how to organize them",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Folder selection
        Text(
            text = "Select Destination Folder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Existing folders
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableFolders) { folder ->
                FolderOption(
                    folder = folder,
                    isSelected = selectedFolderId == folder.id,
                    onClick = { onFolderSelected(folder.id) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Create new folder button
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCreateFolder = !showCreateFolder },
            border = BorderStroke(
                width = 1.dp,
                color = if (showCreateFolder) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Create New Folder",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // New folder creation form
        if (showCreateFolder) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Folder name
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = onNewFolderNameChanged,
                        label = { Text("Folder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Folder description
                    OutlinedTextField(
                        value = newFolderDescription,
                        onValueChange = onNewFolderDescriptionChanged,
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                    
                    // Color selection
                    Column {
                        Text(
                            text = "Folder Color",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(folderColors) { color ->
                                ColorOption(
                                    color = color,
                                    isSelected = selectedFolderColor == color,
                                    onClick = { onFolderColorSelected(color) }
                                )
                            }
                        }
                    }
                    
                    // Icon selection
                    Column {
                        Text(
                            text = "Folder Icon",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(folderIcons) { iconData ->
                                IconOption(
                                    icon = iconData.second,
                                    name = iconData.first,
                                    isSelected = selectedFolderIcon == iconData.first,
                                    onClick = { onFolderIconSelected(iconData.first) }
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = onCreateFolder,
                        enabled = newFolderName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Folder")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto-organization options
        Text(
            text = "Organization Options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Auto-naming option
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = enableAutoNaming,
                    onCheckedChange = onAutoNamingChanged
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-name notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Automatically generate note titles based on content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auto-tags
        Text(
            text = "Auto-Apply Tags",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tags that will be automatically added to all notes created by this rule",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tag input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text("Enter tag...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    if (newTag.isNotBlank() && !autoTags.contains(newTag.trim())) {
                        onAutoTagsChanged(autoTags + newTag.trim())
                        newTag = ""
                    }
                },
                enabled = newTag.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add tag"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tags list
        if (autoTags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(autoTags) { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = { onAutoTagsChanged(autoTags - tag) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onFinish,
                enabled = selectedFolderId != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Rule")
            }
        }
    }
}

@Composable
private fun FolderOption(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Folder icon with color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(folder.color)))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconFromName(folder.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (folder.description.isNotEmpty()) {
                    Text(
                        text = folder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(android.graphics.Color.parseColor(color)))
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun IconOption(
    icon: ImageVector,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private val folderColors = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0",
    "#00BCD4", "#795548", "#607D8B", "#E91E63", "#3F51B5"
)

private val folderIcons = listOf(
    "folder" to Icons.Default.Folder,
    "work" to Icons.Default.Work,
    "star" to Icons.Default.Star,
    "bookmark" to Icons.Default.Bookmark,
    "label" to Icons.Default.Label,
    "category" to Icons.Default.Category,
    "archive" to Icons.Default.Archive,
    "assignment" to Icons.Default.Assignment
)

private fun getIconFromName(iconName: String): ImageVector {
    return folderIcons.find { it.first == iconName }?.second ?: Icons.Default.Folder
}