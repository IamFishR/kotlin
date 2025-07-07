package com.win11launcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_customizations")
data class UserCustomization(
    @PrimaryKey
    val profileId: String,
    val startMenuLayout: String = "DEFAULT",
    val taskbarPosition: String = "BOTTOM",
    val showUserPictureInStartMenu: Boolean = true,
    val showUsernameInStartMenu: Boolean = true,
    val enableAnimations: Boolean = true,
    val enableSounds: Boolean = false,
    val autoHideTaskbar: Boolean = false,
    val transparencyEffects: Boolean = true,
    val fontSize: String = "MEDIUM",
    val iconSize: String = "MEDIUM",
    val cornerRadius: Int = 8,
    val accentColor: String = "#0078D4",
    val createdAt: Long,
    val updatedAt: Long
)