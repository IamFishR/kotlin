package com.win11launcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val checksum: String = "",
    val isCompressed: Boolean = false,
    val compressionRatio: Float = 1.0f,
    val createdAt: Long,
    val lastAccessedAt: Long,
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