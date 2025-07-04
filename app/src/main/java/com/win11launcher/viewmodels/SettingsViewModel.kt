package com.win11launcher.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.data.repositories.PermissionAnalytics
import com.win11launcher.data.repositories.SettingsRepository
import com.win11launcher.data.repositories.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _permissionStates = MutableStateFlow<List<PermissionState>>(emptyList())
    val permissionStates: StateFlow<List<PermissionState>> = _permissionStates.asStateFlow()
    
    private val _appSettings = MutableStateFlow<List<AppSetting>>(emptyList())
    val appSettings: StateFlow<List<AppSetting>> = _appSettings.asStateFlow()
    
    private val _permissionAnalytics = MutableStateFlow<PermissionAnalytics?>(null)
    val permissionAnalytics: StateFlow<PermissionAnalytics?> = _permissionAnalytics.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _userCustomization = MutableStateFlow<UserCustomization?>(null)
    val userCustomization: StateFlow<UserCustomization?> = _userCustomization.asStateFlow()
    
    init {
        loadData()
        loadProfileData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load permission states
            settingsRepository.getAllPermissionStates().collect { permissions ->
                _permissionStates.value = permissions
            }
        }
        
        viewModelScope.launch {
            // Load app settings
            settingsRepository.getAllSettings().collect { settings ->
                _appSettings.value = settings
            }
        }
        
        viewModelScope.launch {
            // Load permission analytics
            try {
                val analytics = settingsRepository.getPermissionAnalytics()
                _permissionAnalytics.value = analytics
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load permission analytics: ${e.message}"
                )
            }
        }
        
        // Check and update all permissions
        refreshPermissions()
    }
    
    fun refreshPermissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                settingsRepository.checkAndUpdateAllPermissions()
                val analytics = settingsRepository.getPermissionAnalytics()
                _permissionAnalytics.value = analytics
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to refresh permissions: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updatePermissionNotes(permissionName: String, notes: String) {
        viewModelScope.launch {
            try {
                settingsRepository.updatePermissionNotes(permissionName, notes)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update permission notes: ${e.message}"
                )
            }
        }
    }
    
    fun setSetting(key: String, value: String, category: String = "general", description: String = "") {
        viewModelScope.launch {
            try {
                settingsRepository.setSetting(key, value, category, description)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }
    
    fun setSettingBoolean(key: String, value: Boolean, category: String = "general", description: String = "") {
        viewModelScope.launch {
            try {
                settingsRepository.setSettingBoolean(key, value, category, description)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }
    
    fun toggleAutoCheckPermissions() {
        viewModelScope.launch {
            try {
                val currentValue = settingsRepository.isAutoCheckPermissionsEnabled()
                settingsRepository.setAutoCheckPermissions(!currentValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to toggle auto check permissions: ${e.message}"
                )
            }
        }
    }
    
    fun togglePermissionNotifications() {
        viewModelScope.launch {
            try {
                val currentValue = settingsRepository.isPermissionNotificationsEnabled()
                settingsRepository.setPermissionNotifications(!currentValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to toggle permission notifications: ${e.message}"
                )
            }
        }
    }
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setThemeMode(mode)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update theme mode: ${e.message}"
                )
            }
        }
    }
    
    fun toggleLauncherAutoStart() {
        viewModelScope.launch {
            try {
                val currentValue = settingsRepository.isLauncherAutoStartEnabled()
                settingsRepository.setLauncherAutoStart(!currentValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to toggle launcher auto start: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Helper methods to get specific setting values
    suspend fun getAutoCheckPermissions(): Boolean {
        return settingsRepository.isAutoCheckPermissionsEnabled()
    }
    
    suspend fun getPermissionNotifications(): Boolean {
        return settingsRepository.isPermissionNotificationsEnabled()
    }
    
    suspend fun getThemeMode(): String {
        return settingsRepository.getThemeMode()
    }
    
    suspend fun getLauncherAutoStart(): Boolean {
        return settingsRepository.isLauncherAutoStartEnabled()
    }
    
    fun getSettingsByCategory(category: String): List<AppSetting> {
        return _appSettings.value.filter { it.category == category }
    }
    
    fun getPermissionsByRequired(required: Boolean): List<PermissionState> {
        return _permissionStates.value.filter { it.isRequired == required }
    }
    
    fun getGrantedPermissions(): List<PermissionState> {
        return _permissionStates.value.filter { it.isGranted }
    }
    
    fun getMissingRequiredPermissions(): List<PermissionState> {
        return _permissionStates.value.filter { it.isRequired && !it.isGranted }
    }
    
    // Profile management methods
    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                // Initialize profile if it doesn't exist
                userProfileRepository.initializeDefaultProfile()
                
                // Load user profile
                userProfileRepository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load profile: ${e.message}"
                )
            }
        }
        
        viewModelScope.launch {
            try {
                // Load user customization
                userProfileRepository.getUserCustomization().collect { customization ->
                    _userCustomization.value = customization
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load customization: ${e.message}"
                )
            }
        }
    }
    
    fun updateUsername(username: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateUsername(username = username)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update username: ${e.message}"
                )
            }
        }
    }
    
    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateDisplayName(displayName = displayName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update display name: ${e.message}"
                )
            }
        }
    }
    
    fun updateBio(bio: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateBio(bio = bio)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update bio: ${e.message}"
                )
            }
        }
    }
    
    fun updateThemeColor(color: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateThemeColor(color = color)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update theme color: ${e.message}"
                )
            }
        }
    }
    
    fun updateProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = userProfileRepository.saveProfilePicture(imageUri)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to save profile picture: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update profile picture: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateCustomization(
        showUserPictureInStartMenu: Boolean? = null,
        showUsernameInStartMenu: Boolean? = null,
        enableAnimations: Boolean? = null,
        accentColor: String? = null
    ) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateCustomization(
                    showUserPictureInStartMenu = showUserPictureInStartMenu,
                    showUsernameInStartMenu = showUsernameInStartMenu,
                    enableAnimations = enableAnimations,
                    accentColor = accentColor
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update customization: ${e.message}"
                )
            }
        }
    }
    
    // Get current profile data
    fun getCurrentUserProfile(): UserProfile? = _userProfile.value
    fun getCurrentUserCustomization(): UserCustomization? = _userCustomization.value
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)