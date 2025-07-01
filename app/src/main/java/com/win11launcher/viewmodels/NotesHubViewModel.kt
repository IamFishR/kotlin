package com.win11launcher.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.data.entities.Folder
import com.win11launcher.data.entities.Note
import com.win11launcher.data.entities.TrackingRule
import com.win11launcher.data.models.FilterCriteria
import com.win11launcher.data.models.FilterType
import com.win11launcher.data.InstalledApp
import com.win11launcher.data.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class NotesHubViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = NotesDatabase.getDatabase(application)
    private val appRepository = AppRepository(application)
    private val gson = Gson()
    
    // Database flows
    val folders = database.folderDao().getAllFolders()
    val rules = database.trackingRuleDao().getAllRules()
    val notes = database.noteDao().getAllNotes()
    
    // UI state
    private val _uiState = MutableStateFlow(NotesHubUiState())
    val uiState: StateFlow<NotesHubUiState> = _uiState.asStateFlow()
    
    // Rule creation state
    private val _ruleCreationState = MutableStateFlow(RuleCreationState())
    val ruleCreationState: StateFlow<RuleCreationState> = _ruleCreationState.asStateFlow()
    
    init {
        loadInstalledApps()
        initializeDefaultData()
    }
    
    private fun loadInstalledApps() {
        viewModelScope.launch {
            appRepository.loadInstalledApps()
            // Get the apps from the state
            val apps = appRepository.installedApps.value
            _uiState.value = _uiState.value.copy(availableApps = apps)
        }
    }
    
    private fun initializeDefaultData() {
        viewModelScope.launch {
            // Create default folder if it doesn't exist
            val defaultFolder = database.folderDao().getDefaultFolder()
            if (defaultFolder == null) {
                val folder = Folder(
                    id = "default",
                    name = "General",
                    description = "Default folder for all notes",
                    color = "#2196F3",
                    icon = "folder",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDefault = true
                )
                database.folderDao().insertFolder(folder)
            }
        }
    }
    
    // Rule creation methods
    fun startRuleCreation() {
        _ruleCreationState.value = RuleCreationState()
        _uiState.value = _uiState.value.copy(currentScreen = NotesHubScreen.APP_SELECTION)
    }
    
    fun updateSelectedApps(packageName: String, isSelected: Boolean) {
        val currentSelection = _ruleCreationState.value.selectedApps.toMutableSet()
        if (isSelected) {
            currentSelection.add(packageName)
        } else {
            currentSelection.remove(packageName)
        }
        _ruleCreationState.value = _ruleCreationState.value.copy(selectedApps = currentSelection)
    }
    
    fun updateFilterType(filterType: FilterType) {
        _ruleCreationState.value = _ruleCreationState.value.copy(filterType = filterType)
    }
    
    fun updateKeywords(keywords: List<String>) {
        _ruleCreationState.value = _ruleCreationState.value.copy(keywords = keywords)
    }
    
    fun updateExcludeKeywords(excludeKeywords: List<String>) {
        _ruleCreationState.value = _ruleCreationState.value.copy(excludeKeywords = excludeKeywords)
    }
    
    fun updateRegexPattern(pattern: String) {
        _ruleCreationState.value = _ruleCreationState.value.copy(regexPattern = pattern)
    }
    
    fun updateCaseSensitive(caseSensitive: Boolean) {
        _ruleCreationState.value = _ruleCreationState.value.copy(caseSensitive = caseSensitive)
    }
    
    fun updateSelectedFolder(folderId: String) {
        _ruleCreationState.value = _ruleCreationState.value.copy(selectedFolderId = folderId)
    }
    
    fun updateNewFolderDetails(name: String, description: String, color: String, icon: String) {
        _ruleCreationState.value = _ruleCreationState.value.copy(
            newFolderName = name,
            newFolderDescription = description,
            selectedFolderColor = color,
            selectedFolderIcon = icon
        )
    }
    
    fun updateAutoTags(tags: List<String>) {
        _ruleCreationState.value = _ruleCreationState.value.copy(autoTags = tags)
    }
    
    fun updateAutoNaming(enabled: Boolean) {
        _ruleCreationState.value = _ruleCreationState.value.copy(enableAutoNaming = enabled)
    }
    
    fun createFolder() {
        viewModelScope.launch {
            val state = _ruleCreationState.value
            if (state.newFolderName.isNotBlank()) {
                val folder = Folder(
                    id = UUID.randomUUID().toString(),
                    name = state.newFolderName,
                    description = state.newFolderDescription,
                    color = state.selectedFolderColor,
                    icon = state.selectedFolderIcon,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                database.folderDao().insertFolder(folder)
                
                // Select the newly created folder
                _ruleCreationState.value = state.copy(
                    selectedFolderId = folder.id,
                    newFolderName = "",
                    newFolderDescription = ""
                )
            }
        }
    }
    
    fun createRule() {
        viewModelScope.launch {
            val state = _ruleCreationState.value
            
            if (state.selectedApps.isNotEmpty() && state.selectedFolderId != null) {
                val filterCriteria = when (state.filterType) {
                    FilterType.KEYWORD_INCLUDE -> FilterCriteria(
                        keywords = state.keywords,
                        caseSensitive = state.caseSensitive
                    )
                    FilterType.KEYWORD_EXCLUDE -> FilterCriteria(
                        excludeKeywords = state.excludeKeywords,
                        caseSensitive = state.caseSensitive
                    )
                    FilterType.REGEX -> FilterCriteria(
                        regexPattern = state.regexPattern,
                        caseSensitive = state.caseSensitive
                    )
                    FilterType.ALL -> FilterCriteria()
                }
                
                val rule = TrackingRule(
                    id = UUID.randomUUID().toString(),
                    name = generateRuleName(state),
                    description = generateRuleDescription(state),
                    sourcePackages = gson.toJson(state.selectedApps.toList()),
                    filterType = state.filterType.name,
                    filterCriteria = gson.toJson(filterCriteria),
                    destinationFolderId = state.selectedFolderId,
                    autoTags = gson.toJson(state.autoTags),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                database.trackingRuleDao().insertRule(rule)
                
                // Reset rule creation state and navigate back
                _ruleCreationState.value = RuleCreationState()
                _uiState.value = _uiState.value.copy(currentScreen = NotesHubScreen.RULE_MANAGEMENT)
            }
        }
    }
    
    private fun generateRuleName(state: RuleCreationState): String {
        val appCount = state.selectedApps.size
        val filterTypeText = when (state.filterType) {
            FilterType.ALL -> "All notifications"
            FilterType.KEYWORD_INCLUDE -> "Keyword filter"
            FilterType.KEYWORD_EXCLUDE -> "Exclude filter"
            FilterType.REGEX -> "Pattern filter"
        }
        
        return if (appCount == 1) {
            val packageName = state.selectedApps.first()
            val appName = _uiState.value.availableApps.find { it.packageName == packageName }?.name ?: packageName
            "$appName - $filterTypeText"
        } else {
            "$appCount apps - $filterTypeText"
        }
    }
    
    private fun generateRuleDescription(state: RuleCreationState): String {
        val appNames = state.selectedApps.mapNotNull { packageName ->
            _uiState.value.availableApps.find { it.packageName == packageName }?.name
        }
        
        val appsText = when {
            appNames.size <= 3 -> appNames.joinToString(", ")
            else -> "${appNames.take(2).joinToString(", ")} and ${appNames.size - 2} others"
        }
        
        val filterText = when (state.filterType) {
            FilterType.ALL -> "all notifications"
            FilterType.KEYWORD_INCLUDE -> "notifications containing: ${state.keywords.joinToString(", ")}"
            FilterType.KEYWORD_EXCLUDE -> "notifications except those containing: ${state.excludeKeywords.joinToString(", ")}"
            FilterType.REGEX -> "notifications matching pattern: ${state.regexPattern}"
        }
        
        return "Tracking $filterText from $appsText"
    }
    
    // Rule management methods
    fun toggleRule(ruleId: String, isActive: Boolean) {
        viewModelScope.launch {
            database.trackingRuleDao().updateRuleActive(ruleId, isActive)
        }
    }
    
    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            database.trackingRuleDao().deleteRuleById(ruleId)
        }
    }
    
    // Navigation methods
    fun navigateToScreen(screen: NotesHubScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }
    
    fun navigateToNextCreationStep() {
        val currentScreen = _uiState.value.currentScreen
        val nextScreen = when (currentScreen) {
            NotesHubScreen.APP_SELECTION -> NotesHubScreen.CONTENT_FILTERING
            NotesHubScreen.CONTENT_FILTERING -> NotesHubScreen.DESTINATION
            else -> currentScreen
        }
        _uiState.value = _uiState.value.copy(currentScreen = nextScreen)
    }
    
    fun navigateToPreviousCreationStep() {
        val currentScreen = _uiState.value.currentScreen
        val previousScreen = when (currentScreen) {
            NotesHubScreen.CONTENT_FILTERING -> NotesHubScreen.APP_SELECTION
            NotesHubScreen.DESTINATION -> NotesHubScreen.CONTENT_FILTERING
            else -> NotesHubScreen.RULE_MANAGEMENT
        }
        _uiState.value = _uiState.value.copy(currentScreen = previousScreen)
    }
}

data class NotesHubUiState(
    val currentScreen: NotesHubScreen = NotesHubScreen.RULE_MANAGEMENT,
    val availableApps: List<InstalledApp> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RuleCreationState(
    val selectedApps: Set<String> = emptySet(),
    val filterType: FilterType = FilterType.ALL,
    val keywords: List<String> = emptyList(),
    val excludeKeywords: List<String> = emptyList(),
    val regexPattern: String = "",
    val caseSensitive: Boolean = false,
    val selectedFolderId: String? = null,
    val newFolderName: String = "",
    val newFolderDescription: String = "",
    val selectedFolderColor: String = "#2196F3",
    val selectedFolderIcon: String = "folder",
    val autoTags: List<String> = emptyList(),
    val enableAutoNaming: Boolean = false
)

enum class NotesHubScreen {
    RULE_MANAGEMENT,
    APP_SELECTION,
    CONTENT_FILTERING,
    DESTINATION,
    NOTES_VIEW,
    RULE_DETAILS
}