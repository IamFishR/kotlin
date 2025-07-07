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

