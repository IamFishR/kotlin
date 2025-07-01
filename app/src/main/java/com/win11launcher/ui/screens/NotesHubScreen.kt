package com.win11launcher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ruleCreationState by viewModel.ruleCreationState.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle(emptyList())
    val rules by viewModel.rules.collectAsStateWithLifecycle(emptyList())
    val notes by viewModel.notes.collectAsStateWithLifecycle(emptyList())
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = {
                Text(
                    text = when (uiState.currentScreen) {
                        Screen.RULE_MANAGEMENT -> "Notes Hub"
                        Screen.APP_SELECTION -> "Create Rule - Step 1"
                        Screen.CONTENT_FILTERING -> "Create Rule - Step 2"
                        Screen.DESTINATION -> "Create Rule - Step 3"
                        Screen.NOTES_VIEW -> "My Notes"
                        Screen.RULE_DETAILS -> "Rule Details"
                    }
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        when (uiState.currentScreen) {
                            Screen.RULE_MANAGEMENT -> onNavigateBack()
                            else -> viewModel.navigateToPreviousCreationStep()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
                // TODO: Implement notes view screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Notes View - Coming Soon")
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