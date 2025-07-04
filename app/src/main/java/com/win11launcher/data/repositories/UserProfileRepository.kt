package com.win11launcher.data.repositories

import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.data.entities.UserFile
import com.win11launcher.data.entities.FileType
import com.win11launcher.utils.ProfileImageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val profileImageManager: ProfileImageManager
) {
    
    // Profile operations
    fun getUserProfile(profileId: String = "default"): Flow<UserProfile?> {
        return userProfileDao.getUserProfileFlow(profileId)
    }
    
    suspend fun getUserProfileSync(profileId: String = "default"): UserProfile? {
        return userProfileDao.getUserProfile(profileId)
    }
    
    suspend fun updateUsername(profileId: String = "default", username: String) {
        userProfileDao.updateUsername(profileId, username)
    }
    
    suspend fun updateDisplayName(profileId: String = "default", displayName: String) {
        userProfileDao.updateDisplayName(profileId, displayName)
    }
    
    suspend fun updateBio(profileId: String = "default", bio: String) {
        userProfileDao.updateBio(profileId, bio)
    }
    
    suspend fun updateThemeColor(profileId: String = "default", color: String) {
        userProfileDao.updateThemeColor(profileId, color)
    }
    
    suspend fun updateProfilePicture(profileId: String = "default", imagePath: String) {
        userProfileDao.updateProfilePicture(profileId, imagePath)
    }
    
    suspend fun updateLastLogin(profileId: String = "default") {
        userProfileDao.updateLastLogin(profileId)
    }
    
    // Profile picture operations
    suspend fun saveProfilePicture(
        uri: Uri,
        profileId: String = "default"
    ): Result<String> {
        return try {
            val result = profileImageManager.saveProfileImage(uri, profileId)
            if (result.isSuccess) {
                val profileImage = result.getOrThrow()
                
                // Save to database
                val userFile = UserFile(
                    id = profileImage.id,
                    profileId = profileId,
                    fileName = profileImage.fileName,
                    originalFileName = profileImage.fileName,
                    filePath = profileImage.filePath,
                    fileType = FileType.PROFILE_PICTURE,
                    fileSize = profileImage.fileSize,
                    mimeType = "image/jpeg",
                    createdAt = profileImage.createdAt,
                    lastAccessedAt = profileImage.createdAt
                )
                
                // Deactivate old profile pictures
                userProfileDao.deactivateOldFilesOfType(profileId, FileType.PROFILE_PICTURE, userFile.id)
                
                // Insert new profile picture
                userProfileDao.insertUserFile(userFile)
                
                // Update profile with new picture path
                updateProfilePicture(profileId, profileImage.filePath)
                
                Result.success(profileImage.filePath)
            } else {
                result.exceptionOrNull()?.let { Result.failure(it) } ?: Result.failure(Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentProfilePicture(profileId: String = "default"): UserFile? {
        return userProfileDao.getCurrentProfilePicture(profileId)
    }
    
    // Customization operations
    fun getUserCustomization(profileId: String = "default"): Flow<UserCustomization?> {
        return userProfileDao.getUserCustomizationFlow(profileId)
    }
    
    suspend fun updateUserCustomization(customization: UserCustomization) {
        userProfileDao.updateUserCustomization(customization)
    }
    
    suspend fun updateCustomization(
        profileId: String = "default",
        showUserPictureInStartMenu: Boolean? = null,
        showUsernameInStartMenu: Boolean? = null,
        enableAnimations: Boolean? = null,
        accentColor: String? = null,
        startMenuLayout: String? = null,
        taskbarPosition: String? = null
    ) {
        val currentCustomization = userProfileDao.getUserCustomization(profileId)
        if (currentCustomization != null) {
            val updatedCustomization = currentCustomization.copy(
                showUserPictureInStartMenu = showUserPictureInStartMenu ?: currentCustomization.showUserPictureInStartMenu,
                showUsernameInStartMenu = showUsernameInStartMenu ?: currentCustomization.showUsernameInStartMenu,
                enableAnimations = enableAnimations ?: currentCustomization.enableAnimations,
                accentColor = accentColor ?: currentCustomization.accentColor,
                startMenuLayout = startMenuLayout ?: currentCustomization.startMenuLayout,
                taskbarPosition = taskbarPosition ?: currentCustomization.taskbarPosition,
                updatedAt = System.currentTimeMillis()
            )
            userProfileDao.updateUserCustomization(updatedCustomization)
        }
    }
    
    // File operations
    fun getUserFiles(profileId: String = "default"): Flow<List<UserFile>> {
        return userProfileDao.getAllUserFiles(profileId)
    }
    
    fun getUserFilesByType(profileId: String = "default", fileType: FileType): Flow<List<UserFile>> {
        return userProfileDao.getUserFilesByType(profileId, fileType)
    }
    
    suspend fun deleteUserFile(fileId: String) {
        userProfileDao.deleteUserFile(fileId)
    }
    
    suspend fun getTotalFileSizeForUser(profileId: String = "default"): Long {
        return userProfileDao.getTotalFileSizeForUser(profileId) ?: 0L
    }
    
    // Analytics and helper methods
    suspend fun getUserFileCount(profileId: String = "default"): Int {
        return userProfileDao.getUserFileCount(profileId)
    }
    
    suspend fun cleanupOldFiles(profileId: String = "default", cutoffTime: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)) {
        userProfileDao.cleanupOldFiles(cutoffTime)
    }
    
    // Complete profile data
    suspend fun getCompleteUserProfile(profileId: String = "default"): UserProfile? {
        return userProfileDao.getCompleteUserProfile(profileId)
    }
    
    // Profile initialization
    suspend fun initializeDefaultProfile() {
        val existingProfile = getUserProfileSync("default")
        if (existingProfile == null) {
            val defaultProfile = UserProfile(
                id = "default",
                username = "User",
                displayName = "",
                themeColor = "#0078D4",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDefault = true
            )
            userProfileDao.insertUserProfile(defaultProfile)
            
            val defaultCustomization = UserCustomization(
                profileId = "default",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            userProfileDao.insertUserCustomization(defaultCustomization)
        }
    }
}

