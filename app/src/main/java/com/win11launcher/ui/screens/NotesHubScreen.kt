package com.win11launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.win11launcher.ui.screens.noteshub.*
import com.win11launcher.viewmodels.NotesHubViewModel
import com.win11launcher.viewmodels.NotesHubScreen as Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesHubScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotesHubViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ruleCreationState by viewModel.ruleCreationState.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle(emptyList())
    val rules by viewModel.rules.collectAsStateWithLifecycle(emptyList())
    val notes by viewModel.notes.collectAsStateWithLifecycle(emptyList())
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val notesViewState by viewModel.notesViewState.collectAsStateWithLifecycle()
    val smartSuggestions by viewModel.smartSuggestions.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp) // Account for taskbar
    ) {
        // Top app bar with Windows-style controls
        TopAppBar(
            title = {
                Text(
                    text = when (uiState.currentScreen) {
                        Screen.RULE_MANAGEMENT -> "Notes Hub"
                        Screen.SMART_SUGGESTIONS -> "Smart Suggestions"
                        Screen.SMART_NOTIFICATIONS -> "Smart Notifications"
                        Screen.SIMPLE_NOTIFICATIONS -> "All Notifications"
                        Screen.APP_SELECTION -> "Create Rule - Step 1"
                        Screen.CONTENT_FILTERING -> "Create Rule - Step 2"
                        Screen.DESTINATION -> "Create Rule - Step 3"
                        Screen.NOTES_VIEW -> "My Notes"
                        Screen.NOTE_DETAIL -> "Note Details"
                        Screen.RULE_DETAILS -> "Rule Details"
                    }
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        when (uiState.currentScreen) {
                            Screen.RULE_MANAGEMENT -> onNavigateBack()
                            Screen.SMART_SUGGESTIONS -> viewModel.navigateToScreen(Screen.RULE_MANAGEMENT)
                            Screen.SMART_NOTIFICATIONS -> viewModel.navigateToScreen(Screen.RULE_MANAGEMENT)
                            Screen.SIMPLE_NOTIFICATIONS -> viewModel.navigateToScreen(Screen.RULE_MANAGEMENT)
                            Screen.NOTES_VIEW -> viewModel.navigateToScreen(Screen.RULE_MANAGEMENT)
                            Screen.NOTE_DETAIL -> viewModel.navigateToScreen(Screen.NOTES_VIEW)
                            else -> viewModel.navigateToPreviousCreationStep()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                WindowsControls(
                    onMinimize = {
                        // TODO: Implement minimize functionality
                    },
                    onMaximize = {
                        // TODO: Implement maximize functionality
                    },
                    onClose = onNavigateBack
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Windows-style menu bar
        WindowsMenuBar(
            onFileMenuAction = { action ->
                when (action) {
                    "new_rule" -> viewModel.startRuleCreation()
                    "export" -> {
                        // TODO: Implement export functionality
                    }
                    "exit" -> onNavigateBack()
                }
            },
            onEditMenuAction = { action ->
                when (action) {
                    "search" -> {
                        // TODO: Implement search functionality
                    }
                    "preferences" -> {
                        // TODO: Implement preferences
                    }
                }
            },
            onViewMenuAction = { action ->
                when (action) {
                    "notes" -> viewModel.navigateToNotesView()
                    "rules" -> viewModel.navigateToScreen(Screen.RULE_MANAGEMENT)
                    "suggestions" -> viewModel.navigateToScreen(Screen.SMART_SUGGESTIONS)
                    "notifications" -> viewModel.navigateToScreen(Screen.SMART_NOTIFICATIONS)
                }
            },
            onToolsMenuAction = { action ->
                when (action) {
                    "smart_suggestions" -> viewModel.navigateToScreen(Screen.SMART_SUGGESTIONS)
                    "analytics" -> {
                        // TODO: Implement analytics
                    }
                }
            }
        )
        
        // Screen content
        when (uiState.currentScreen) {
            Screen.RULE_MANAGEMENT -> {
                RuleManagementScreen(
                    rules = rules,
                    folders = folders,
                    onRuleToggle = viewModel::toggleRule,
                    onRuleEdit = { ruleId ->
                        // TODO: Implement rule editing
                    },
                    onRuleDelete = viewModel::deleteRule,
                    onCreateNewRule = viewModel::startRuleCreation,
                    onRuleDetails = { ruleId ->
                        // TODO: Navigate to rule details
                    },
                    onViewNotes = viewModel::navigateToNotesView,
                    onSmartSuggestions = { viewModel.navigateToScreen(Screen.SMART_SUGGESTIONS) },
                    onSmartNotifications = { viewModel.navigateToScreen(Screen.SIMPLE_NOTIFICATIONS) },
                    suggestionsCount = smartSuggestions.size
                )
            }
            
            Screen.SMART_SUGGESTIONS -> {
                SmartSuggestionsScreen(
                    viewModel = viewModel,
                    onSuggestionApplied = { suggestionId ->
                        // Suggestion applied successfully
                    },
                    onSuggestionDismissed = { suggestionId ->
                        // Suggestion dismissed
                    }
                )
            }
            
            Screen.SMART_NOTIFICATIONS -> {
                SmartNotificationScreen(
                    onNavigateBack = { viewModel.navigateToScreen(Screen.RULE_MANAGEMENT) }
                )
            }
            
            Screen.SIMPLE_NOTIFICATIONS -> {
                SimpleNotificationsScreen(
                    onNavigateBack = { viewModel.navigateToScreen(Screen.RULE_MANAGEMENT) },
                    onTrackNotification = { notification ->
                        viewModel.startRuleCreationFromNotification(notification)
                    }
                )
            }
            
            Screen.APP_SELECTION -> {
                AppSelectionScreen(
                    availableApps = uiState.availableApps,
                    selectedApps = ruleCreationState.selectedApps,
                    onAppSelectionChanged = viewModel::updateSelectedApps,
                    onContinue = viewModel::navigateToNextCreationStep,
                    onBack = viewModel::navigateToPreviousCreationStep
                )
            }
            
            Screen.CONTENT_FILTERING -> {
                ContentFilteringScreen(
                    selectedApps = ruleCreationState.selectedApps,
                    filterType = ruleCreationState.filterType,
                    onFilterTypeChanged = viewModel::updateFilterType,
                    keywords = ruleCreationState.keywords,
                    onKeywordsChanged = viewModel::updateKeywords,
                    excludeKeywords = ruleCreationState.excludeKeywords,
                    onExcludeKeywordsChanged = viewModel::updateExcludeKeywords,
                    regexPattern = ruleCreationState.regexPattern,
                    onRegexPatternChanged = viewModel::updateRegexPattern,
                    caseSensitive = ruleCreationState.caseSensitive,
                    onCaseSensitiveChanged = viewModel::updateCaseSensitive,
                    onContinue = viewModel::navigateToNextCreationStep,
                    onBack = viewModel::navigateToPreviousCreationStep
                )
            }
            
            Screen.DESTINATION -> {
                DestinationScreen(
                    availableFolders = folders,
                    selectedFolderId = ruleCreationState.selectedFolderId,
                    onFolderSelected = viewModel::updateSelectedFolder,
                    newFolderName = ruleCreationState.newFolderName,
                    onNewFolderNameChanged = { name ->
                        viewModel.updateNewFolderDetails(
                            name,
                            ruleCreationState.newFolderDescription,
                            ruleCreationState.selectedFolderColor,
                            ruleCreationState.selectedFolderIcon
                        )
                    },
                    newFolderDescription = ruleCreationState.newFolderDescription,
                    onNewFolderDescriptionChanged = { description ->
                        viewModel.updateNewFolderDetails(
                            ruleCreationState.newFolderName,
                            description,
                            ruleCreationState.selectedFolderColor,
                            ruleCreationState.selectedFolderIcon
                        )
                    },
                    selectedFolderColor = ruleCreationState.selectedFolderColor,
                    onFolderColorSelected = { color ->
                        viewModel.updateNewFolderDetails(
                            ruleCreationState.newFolderName,
                            ruleCreationState.newFolderDescription,
                            color,
                            ruleCreationState.selectedFolderIcon
                        )
                    },
                    selectedFolderIcon = ruleCreationState.selectedFolderIcon,
                    onFolderIconSelected = { icon ->
                        viewModel.updateNewFolderDetails(
                            ruleCreationState.newFolderName,
                            ruleCreationState.newFolderDescription,
                            ruleCreationState.selectedFolderColor,
                            icon
                        )
                    },
                    autoTags = ruleCreationState.autoTags,
                    onAutoTagsChanged = viewModel::updateAutoTags,
                    enableAutoNaming = ruleCreationState.enableAutoNaming,
                    onAutoNamingChanged = viewModel::updateAutoNaming,
                    onCreateFolder = viewModel::createFolder,
                    onFinish = {
                        viewModel.createRule()
                    },
                    onBack = viewModel::navigateToPreviousCreationStep
                )
            }
            
            Screen.NOTES_VIEW -> {
                NotesViewScreen(
                    notes = filteredNotes,
                    folders = folders,
                    selectedFolderId = notesViewState.selectedFolderId,
                    onFolderSelected = viewModel::updateNotesFolder,
                    searchQuery = notesViewState.searchQuery,
                    onSearchQueryChanged = viewModel::updateNotesSearch,
                    onNoteClick = { note ->
                        viewModel.selectNote(note.id)
                        viewModel.navigateToScreen(Screen.NOTE_DETAIL)
                    },
                    onBack = { viewModel.navigateToScreen(Screen.RULE_MANAGEMENT) }
                )
            }
            
            Screen.NOTE_DETAIL -> {
                notesViewState.selectedNoteId?.let { noteId ->
                    val selectedNote = notes.find { it.id == noteId }
                    selectedNote?.let { note ->
                        val folder = folders.find { it.id == note.folderId }
                        NoteDetailScreen(
                            note = note,
                            folder = folder,
                            onBack = { viewModel.navigateToScreen(Screen.NOTES_VIEW) },
                            onDelete = {
                                viewModel.deleteNote(note.id)
                                viewModel.navigateToScreen(Screen.NOTES_VIEW)
                            },
                            onArchive = {
                                viewModel.archiveNote(note.id)
                                viewModel.navigateToScreen(Screen.NOTES_VIEW)
                            }
                        )
                    }
                }
            }
            
            Screen.RULE_DETAILS -> {
                // TODO: Implement rule details screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Rule Details - Coming Soon")
                }
            }
        }
    }
}

@Composable
private fun WindowsMenuBar(
    onFileMenuAction: (String) -> Unit,
    onEditMenuAction: (String) -> Unit,
    onViewMenuAction: (String) -> Unit,
    onToolsMenuAction: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuBarItem(
            text = "File",
            menuItems = listOf(
                MenuBarDropdownItem("New Rule", Icons.Default.Add, "new_rule"),
                MenuBarDropdownItem("Export Data", Icons.Default.Download, "export"),
                MenuBarDropdownItem("Exit", Icons.Default.Close, "exit")
            ),
            onMenuItemClick = onFileMenuAction
        )
        
        MenuBarItem(
            text = "Edit",
            menuItems = listOf(
                MenuBarDropdownItem("Search", Icons.Default.Search, "search"),
                MenuBarDropdownItem("Preferences", Icons.Default.Settings, "preferences")
            ),
            onMenuItemClick = onEditMenuAction
        )
        
        MenuBarItem(
            text = "View",
            menuItems = listOf(
                MenuBarDropdownItem("Notes", Icons.Default.Visibility, "notes"),
                MenuBarDropdownItem("Rules", Icons.AutoMirrored.Filled.Rule, "rules"),
                MenuBarDropdownItem("Suggestions", Icons.Default.Lightbulb, "suggestions"),
                MenuBarDropdownItem("Smart Notifications", Icons.Default.Notifications, "notifications")
            ),
            onMenuItemClick = onViewMenuAction
        )
        
        MenuBarItem(
            text = "Tools",
            menuItems = listOf(
                MenuBarDropdownItem("Smart Suggestions", Icons.Default.Lightbulb, "smart_suggestions"),
                MenuBarDropdownItem("Analytics", Icons.Default.Analytics, "analytics")
            ),
            onMenuItemClick = onToolsMenuAction
        )
    }
}

@Composable
private fun MenuBarItem(
    text: String,
    menuItems: List<MenuBarDropdownItem>,
    onMenuItemClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.text) },
                    onClick = {
                        onMenuItemClick(item.action)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun WindowsControls(
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit
) {
    Row {
        // Minimize button
        IconButton(
            onClick = onMinimize,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Minimize,
                contentDescription = "Minimize",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Maximize button
        IconButton(
            onClick = onMaximize,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CropSquare,
                contentDescription = "Maximize",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private data class MenuBarDropdownItem(
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: String
)