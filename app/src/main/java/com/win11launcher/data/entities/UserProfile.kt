package com.win11launcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String = "default", // Single profile for now, could support multiple profiles later
    val username: String = "User",
    val displayName: String = "",
    val profilePicturePath: String = "", // Local file path to profile picture
    val profilePictureUri: String = "", // URI for externally stored images
    val backgroundImagePath: String = "", // Custom wallpaper path
    val themeColor: String = "#0078D4", // User's preferred accent color
    val bio: String = "", // Optional user bio/description
    val email: String = "", // Optional email
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null,
    val isDefault: Boolean = true
)

@Entity(tableName = "user_customizations")
data class UserCustomization(
    @PrimaryKey
    val profileId: String = "default",
    val startMenuLayout: String = "DEFAULT", // DEFAULT, COMPACT, GRID
    val taskbarPosition: String = "BOTTOM", // BOTTOM, TOP, LEFT, RIGHT
    val showUserPictureInStartMenu: Boolean = true,
    val showUsernameInStartMenu: Boolean = true,
    val enableAnimations: Boolean = true,
    val enableSounds: Boolean = false,
    val autoHideTaskbar: Boolean = false,
    val transparencyEffects: Boolean = true,
    val fontSize: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val iconSize: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val cornerRadius: Int = 8, // Corner radius for UI elements
    val accentColor: String = "#0078D4",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// For storing profile picture and other files
@Entity(tableName = "user_files")
data class UserFile(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val fileName: String,
    val originalFileName: String,
    val filePath: String,
    val fileType: FileType,
    val fileSize: Long,
    val mimeType: String = "",
    val checksum: String = "", // For file integrity
    val isCompressed: Boolean = false,
    val compressionRatio: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class FileType {
    PROFILE_PICTURE,
    BACKGROUND_IMAGE,
    CUSTOM_ICON,
    WALLPAPER,
    DOCUMENT,
    OTHER
}